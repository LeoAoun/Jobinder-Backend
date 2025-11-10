package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.dto.internal.InternalUserAuthDTO;
import br.com.jobinder.identityservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/users")
@Tag(name = "Internal", description = "Endpoints for internal service-to-service communication. NOT FOR PUBLIC USE.")
public class InternalController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get User Auth Details by Phone",
            description = "Retrieves internal authentication details (ID, phone, password hash, role) for a user by their phone number. " +
                    "This endpoint is intended for internal service communication ONLY (e.g., Auth-Service).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InternalUserAuthDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (invalid or missing internal token/key)",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the provided phone number",
                    content = @Content)
    })
    @GetMapping("/{phone}")
    public ResponseEntity<InternalUserAuthDTO> getUserAuthDetails(@PathVariable String phone) {
        var userDetails = userService.findAuthDetailsByPhone(phone);
        return ResponseEntity.ok(userDetails);
    }
}