package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.domain.user.User;
import br.com.jobinder.identityservice.dto.user.UserChangePasswordDTO;
import br.com.jobinder.identityservice.dto.user.UserCreateDTO;
import br.com.jobinder.identityservice.dto.user.UserResponseDTO;
import br.com.jobinder.identityservice.dto.user.UserUpdateDTO;
import br.com.jobinder.identityservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users in the Identity Service")
public class UserController {

    @Autowired
    private final UserService userService;

    /*
      Public Endpoints
      These endpoints are accessible without authentication.
    */
    @Operation(
            summary = "Register a new User",
            description = "Create a new user account with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (invalid input data)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "409", description = "Conflict (user already exists)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody @Valid UserCreateDTO createDTO, UriComponentsBuilder uriBuilder) {
        var userResponse = userService.registerUser(createDTO);
        var uri = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(userResponse.id()).toUri();
        return ResponseEntity.created(uri).body(userResponse);
    }

    @Operation(summary = "Search User by ID",
            description = "Retrieve user details by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserDTOById(@PathVariable UUID id) {
        var userResponse = userService.findUserDTOById(id);
        return ResponseEntity.ok(userResponse);
    }

    /*
      Authenticated User Endpoints
      These endpoints require the user to be authenticated.
    */
    @Operation(summary = "Get my profile",
            description = "Retrieve the profile of the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        var userResponse = userService.findUserDTOById(authenticatedUser.getId());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Update my profile",
            description = "Update the profile (first/last name) of the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (invalid input data)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMe(@RequestBody @Valid UserUpdateDTO updateDTO, Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        var updatedUser = userService.updateUser(authenticatedUser.getId(), updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Change my password",
            description = "Update the password for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., old password incorrect)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
    })
    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changeMyPassword(@RequestBody @Valid UserChangePasswordDTO passwordDTO, Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        userService.changePassword(authenticatedUser.getId(), passwordDTO);
        return ResponseEntity.noContent().build();
    }

    /*
      Administrative Endpoints
      These endpoints are intended for administrative use only.
    */
    @Operation(summary = "[Admin] List all Users", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (invalid or missing token)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers() {
        var users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "[Admin] Get a User by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (invalid or missing token)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/admin/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }
}