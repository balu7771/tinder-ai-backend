package io.javabrains.tinderaibackend.conversations;

import io.javabrains.tinderaibackend.profiles.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Tag(name = "Conversation", description = "Conversation management APIs")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final ProfileRepository profileRepository;

    public ConversationController(ConversationRepository conversationRepository,
                                  ProfileRepository profileRepository) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
    }

    ;

    /*@PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request) {

        profileRepository.findById(request.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "unable to find the profile with ID " + request.profileId()));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        conversationRepository.save(conversation);
        return conversation;
    }*/

    @Operation(
        summary = "Get a conversation by ID",
        description = "Returns a conversation based on the provided ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successful operation", 
                    content = @Content(schema = @Schema(implementation = Conversation.class))),
        @ApiResponse(responseCode = "404", description = "Conversation not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("conversations/{conversationId}")
    public Conversation getConversation(@PathVariable String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "unable to find the " + conversationId));
    }

    @Operation(
        summary = "Add a message to a conversation",
        description = "Adds a new message to an existing conversation"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message added successfully", 
                    content = @Content(schema = @Schema(implementation = Conversation.class))),
        @ApiResponse(responseCode = "404", description = "Conversation or profile not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("conversations/{conversationId}")
    public Conversation addMessageToConversation(@PathVariable String conversationId,
                                                 @RequestBody ChatMessage chatMessage) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "unable to find the " + conversationId));

        profileRepository.findById(chatMessage.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "unable to find the profile with ID " + chatMessage.authorId()));

        //TODO: Need to validate that the author of message happens to be the only profile associated.
        ChatMessage messageWithTime = new ChatMessage(
                chatMessage.messageText(),
                chatMessage.authorId(),
                LocalDateTime.now()
        );
        conversation.messages().add(messageWithTime);
        conversationRepository.save(conversation);
        return conversation;
    }


    public record CreateConversationRequest(
            String profileId
    ) {
    }
}
