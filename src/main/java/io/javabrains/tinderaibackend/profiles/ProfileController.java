package io.javabrains.tinderaibackend.profiles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Profile", description = "Profile management APIs")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    @Operation(
        summary = "Get a random profile",
        description = "Returns a randomly selected profile from the database"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successful operation", 
                    content = @Content(schema = @Schema(implementation = Profile.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/profiles/random")
    public Profile getRandomProfile(){
       return profileRepository.getRandonProfile();
    }
}
