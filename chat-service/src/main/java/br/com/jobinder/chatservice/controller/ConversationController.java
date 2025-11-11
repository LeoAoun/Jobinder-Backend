package br.com.jobinder.chatservice.controller;

import br.com.jobinder.chatservice.dto.conversation.ConversationDTO;
import br.com.jobinder.chatservice.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Endpoints for retrieving chat conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "Get conversations by User ID",
            description = "Retrieves all conversations for the specified user ID (client or professional). " +
                    "A user can only retrieve their own conversations. Admins can retrieve any.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConversationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (user trying to access another user's conversations)",
                    content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationDTO>> getConversationsByUserId(@PathVariable UUID userId) {
        List<ConversationDTO> conversations = conversationService.findConversationDTOsByUserId(userId);
        return ResponseEntity.ok(conversations);
    }

    @Operation(summary = "Get a specific conversation by ID",
            description = "Retrieves a specific conversation by its unique ID. " +
                    "Users can only retrieve conversations they are part of. Admins can retrieve any.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConversationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not part of this conversation)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversation not found", content = @Content) // Service lança RuntimeException
    })
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable UUID conversationId) {
        ConversationDTO conversation = conversationService.findConversationDTOById(conversationId);
        return ResponseEntity.ok(conversation);
    }

    /*
      Administrative Endpoints
      These endpoints are intended for administrative use only.
    */

    @Operation(summary = "[Admin] List all conversations",
            description = "Retrieves a list of all conversations in the system. (Requires ADMIN role)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConversationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)",
                    content = @Content)
    })
    @GetMapping("/admin/all")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        List<ConversationDTO> conversations = conversationService.findAllConversationDTOs();
        return ResponseEntity.ok(conversations);
    }
}