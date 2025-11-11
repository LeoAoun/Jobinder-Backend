package br.com.jobinder.chatservice.controller;

import br.com.jobinder.chatservice.dto.message.MessageDTO;
import br.com.jobinder.chatservice.service.MessageService;
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
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Endpoints for retrieving chat messages")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Get messages by Conversation ID",
            description = "Retrieves all messages for a specific conversation, sorted by sent date. " +
                    "Users can only retrieve messages from conversations they are part of. Admins can retrieve any.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not part of this conversation)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversation not found",
                    content = @Content)
    })
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(@PathVariable UUID conversationId) {
        List<MessageDTO> messages = messageService.findMessagesDTOByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }
}