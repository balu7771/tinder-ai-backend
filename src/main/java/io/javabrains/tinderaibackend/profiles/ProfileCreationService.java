package io.javabrains.tinderaibackend.profiles;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static io.javabrains.tinderaibackend.Utils.generateMyersBriggsTypes;
import static io.javabrains.tinderaibackend.Utils.selfieTypes;
import static org.yaml.snakeyaml.nodes.Tag.STR;

@Service
public class ProfileCreationService {

    private static final String STABLE_DIFFUSION_URL = "https://fe97a6a77c5448ab52.gradio.live/sdapi/v1/txt2img";

    private final ChatClient chatClient;
    private final ProfileRepository profileRepository;
    private final HttpClient httpClient;
    private final HttpRequest.Builder stableDiffusionRequestBuilder;
    private final List<Profile> generatedProfiles = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    @Value("${startup-actions.initializeProfiles:false}")
    private Boolean initializeProfiles;

    @Value("${tinderai.lookingForGender:Female}")
    private String lookingForGender;

    @Value("#{${tinderai.character.user}}")
    private Map<String, String> userProfileProperties;

    public ProfileCreationService(ChatClient chatClient, ProfileRepository profileRepository) {
        this.chatClient = chatClient;
        this.profileRepository = profileRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(STABLE_DIFFUSION_URL));
    }

    private static <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public void createProfiles(int numberOfProfiles) {
        if (!Boolean.TRUE.equals(this.initializeProfiles)) {
            return;
        }

        List<Integer> ages = new ArrayList<>();
        for (int i = 20; i <= 35; i++) {
            ages.add(i);
        }
        List<String> ethnicities = List.of("White", "Black", "Asian", "Indian", "Hispanic");
        List<String> myersBriggsTypes = generateMyersBriggsTypes();
        String gender = this.lookingForGender;

        while (this.generatedProfiles.size() < numberOfProfiles) {
            int age = getRandomElement(ages);
            String ethnicity = getRandomElement(ethnicities);
            String personalityType = getRandomElement(myersBriggsTypes);

            String promptText = "";
                    //STR."Create a Tinder profile persona of an \{personalityType} \{age} year old \{ethnicity} \{gender} including the first name, last name, Myers Briggs Personality type and a tinder bio. Save the profile using the saveProfile function.";
            System.out.println(promptText);

            Prompt prompt = new Prompt(promptText);
            String content = chatClient.prompt(prompt).call().content();

            System.out.println(content);
        }

        saveProfilesToJson(this.generatedProfiles);
    }

    private void saveProfilesToJson(List<Profile> profiles) {
        try {
            Gson gson = new Gson();
            List<Profile> existingProfiles = new ArrayList<>();
            File profilesFile = new File(PROFILES_FILE_PATH);
            if (profilesFile.exists()) {
                existingProfiles = gson.fromJson(new FileReader(profilesFile), new TypeToken<List<Profile>>() {}.getType());
            }

            profiles.addAll(existingProfiles);
            List<Profile> profilesWithImages = new ArrayList<>();
            for (Profile profile : profiles) {
                if (StringUtils.isBlank(profile.imageUrl())) {
                    profile = generateProfileImage(profile);
                }
                profilesWithImages.add(profile);
            }

            try (FileWriter writer = new FileWriter(PROFILES_FILE_PATH)) {
                gson.toJson(profilesWithImages, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving profiles to JSON", e);
        }
    }

    private Profile generateProfileImage(Profile profile) {
        String uuid = StringUtils.defaultIfBlank(profile.id(), UUID.randomUUID().toString());
        profile = new Profile(
                uuid,
                profile.firstName(),
                profile.lastName(),
                profile.age(),
                profile.ethnicity(),
                profile.gender(),
                profile.bio(),
                uuid + ".jpg",
                profile.myersBriggsPersonalityType()
        );

        String selfieType = getRandomElement(selfieTypes());
        String positivePrompt = "";
                //STR."Selfie of a \{profile.age()} year old \{profile.myersBriggsPersonalityType()} \{profile.ethnicity()} \{profile.gender()}, \{selfieType}, photorealistic, ultra-detailed, 4k DSLR.";
        String negativePrompt = "multiple faces, lowres, text, error, cropped, worst quality, low quality, jpeg artifacts, ugly, duplicate, morbid, mutilated, out of frame, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, mutation, deformed, blurry, dehydrated, bad anatomy, bad proportions, extra limbs, cloned face, disfigured, gross proportions, malformed limbs, missing arms, missing legs, extra arms, extra legs, fused fingers, too many fingers, long neck, username, watermark, signature";

        String requestBody = "";
           /* {
              "prompt": "\{positivePrompt}",
              "negative_prompt": "\{negativePrompt}",
              "steps": 40
            }
        """;*/

        System.out.println("");
                //STR."Creating image for \{profile.firstName()} \{profile.lastName()} (\{profile.ethnicity()})");

        try {
            HttpRequest request = this.stableDiffusionRequestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            ImageResponse imageResponse = gson.fromJson(response.body(), ImageResponse.class);

            if (imageResponse.images() != null && !imageResponse.images().isEmpty()) {
                saveImageBytes(profile.imageUrl(), Base64.getDecoder().decode(imageResponse.images().getFirst()));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error generating profile image", e);
        }

        return profile;
    }

    private void saveImageBytes(String filename, byte[] imageBytes) throws IOException {
        Path directory = Paths.get("src/main/resources/static/images/");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        try (FileOutputStream fos = new FileOutputStream(directory.resolve(filename).toFile())) {
            fos.write(imageBytes);
        }
    }

    @Bean
    @Description("Save the Tinder profile information")
    public Function<Profile, Boolean> saveProfile() {
        return profile -> {
            System.out.println("Saving profile via function call:");
            System.out.println(profile);
            this.generatedProfiles.add(profile);
            return true;
        };
    }

    public void saveProfilesToDB() {
        Gson gson = new Gson();
        try {
            List<Profile> profiles = gson.fromJson(new FileReader(PROFILES_FILE_PATH), new TypeToken<List<Profile>>() {}.getType());
            profileRepository.deleteAll();
            profileRepository.saveAll(profiles);
        } catch (IOException e) {
            throw new RuntimeException("Error reading profiles from file", e);
        }

        Profile profile = new Profile(
                userProfileProperties.get("id"),
                userProfileProperties.get("firstName"),
                userProfileProperties.get("lastName"),
                Integer.parseInt(userProfileProperties.get("age")),
                userProfileProperties.get("ethnicity"),
                Gender.valueOf(userProfileProperties.get("gender")),
                userProfileProperties.get("bio"),
                userProfileProperties.get("imageUrl"),
                userProfileProperties.get("myersBriggsPersonalityType")
        );

        profileRepository.save(profile);
    }

    private record ImageResponse(List<String> images) {}
}