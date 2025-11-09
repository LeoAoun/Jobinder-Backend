package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileCreateDTO;
import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileResponseDTO;
import br.com.jobinder.identityservice.service.ServiceProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-profiles")
@Tag(name = "Service Profiles", description = "Endpoints for managing user service profiles")
public class ServiceProfileController {

    @Autowired
    private ServiceProfileService serviceProfileService;

    /*
      Public and Authenticated User Endpoints
      These endpoints are accessible to authenticated users.
    */
    @Operation(summary = "Create a new Service Profile",
            description = "Creates a new service profile for the currently authenticated user. A user can only have one profile.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (invalid input data)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized (invalid or missing token)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict (user already has a profile)",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ServiceProfileResponseDTO> createServiceProfile(@RequestBody @Valid ServiceProfileCreateDTO createDto, UriComponentsBuilder uriBuilder, Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());
        var profileResponse = serviceProfileService.createServiceProfile(createDto, authenticatedUserId);
        var uri = uriBuilder.path("/api/v1/profiles/{id}").buildAndExpand(profileResponse.serviceProfileId()).toUri();
        return ResponseEntity.created(uri).body(profileResponse);
    }

    @Operation(summary = "Get a Service Profile by User ID",
            description = "Retrieves a profile using the User's ID. Regular users can only retrieve their own profile. Admins can retrieve any.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (user trying to access another user's profile)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Service Profile not found for this user",
                    content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ServiceProfileResponseDTO> getServiceProfileByUserId(@PathVariable UUID userId) {
        var profileResponse = serviceProfileService.getServiceProfileByUserId(userId);
        return ResponseEntity.ok(profileResponse);
    }

    @Operation(summary = "Find the User ID associated with a Service Profile ID",
            description = "Retrieves the owner's User ID based on a Service Profile ID. This is a public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User ID found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "404", description = "Service Profile not found",
                    content = @Content)
    })
    @GetMapping("/{profileId}/user")
    public ResponseEntity<UUID> getUserIdByServiceProfileId(@PathVariable UUID serviceProfileId) {
        var userId = serviceProfileService.findUserIdByServiceProfileId(serviceProfileId);
        return ResponseEntity.ok(userId);
    }

    /*
      Administrative Endpoints
      These endpoints are intended for administrative use only.
    */
    @Operation(summary = "[Admin] List all Service Profiles",
            description = "Retrieves a list of all service profiles in the system. (Requires ADMIN role)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of profiles retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServiceProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<Iterable<ServiceProfileResponseDTO>> getAllServiceProfilesDTO() {
        var profiles = serviceProfileService.getAllServiceProfilesDTO();
        return ResponseEntity.ok(profiles);
    }
}