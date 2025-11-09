package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileCreateDTO;
import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileResponseDTO;
import br.com.jobinder.identityservice.service.ServiceProfileService;
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
public class ServiceProfileController {

    @Autowired
    private ServiceProfileService serviceProfileService;

    @PostMapping
    public ResponseEntity<ServiceProfileResponseDTO> createServiceProfile(@RequestBody @Valid ServiceProfileCreateDTO createDto, UriComponentsBuilder uriBuilder, Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());
        var profileResponse = serviceProfileService.createServiceProfile(createDto, authenticatedUserId);
        var uri = uriBuilder.path("/api/v1/profiles/{id}").buildAndExpand(profileResponse.serviceProfileId()).toUri();
        return ResponseEntity.created(uri).body(profileResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ServiceProfileResponseDTO> getServiceProfileByUserId(@PathVariable UUID userId) {
        var profileResponse = serviceProfileService.getServiceProfileByUserId(userId);
        return ResponseEntity.ok(profileResponse);
    }

    @GetMapping("/{profileId}/user")
    public ResponseEntity<UUID> getUserIdByServiceProfileId(@PathVariable UUID serviceProfileId) {
        var userId = serviceProfileService.findUserIdByServiceProfileId(serviceProfileId);
        return ResponseEntity.ok(userId);
    }

    /*
      Administrative Endpoints
      These endpoints are intended for administrative use only.
    */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<Iterable<ServiceProfileResponseDTO>> getAllServiceProfilesDTO() {
        var profiles = serviceProfileService.getAllServiceProfilesDTO();
        return ResponseEntity.ok(profiles);
    }
}