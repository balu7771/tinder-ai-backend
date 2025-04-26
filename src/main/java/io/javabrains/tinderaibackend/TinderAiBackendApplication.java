package io.javabrains.tinderaibackend;

import io.javabrains.tinderaibackend.profiles.Gender;
import io.javabrains.tinderaibackend.profiles.Profile;
import io.javabrains.tinderaibackend.profiles.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TinderAiBackendApplication implements CommandLineRunner {

	@Autowired
	private ProfileRepository profileRepository;

	public static void main(String[] args) {
		SpringApplication.run(TinderAiBackendApplication.class, args
		);
	}

	public void run(String... args){
		Profile profile = new Profile(
				"1",
				"Balaji",
				"Mudipalli",
				42,
				"Indian",
				Gender.MALE,
				"Managing Engineer",
				"foo.jpg",
				"Happy"
		);

		profileRepository.save(profile);
		profileRepository.findAll().forEach(System.out::println);
	}

}
