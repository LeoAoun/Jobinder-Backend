package br.com.jobinder.identityservice.service;

import br.com.jobinder.identityservice.domain.serviceprofile.ServiceProfile;
import br.com.jobinder.identityservice.domain.serviceprofile.ServiceProfileRepository;
import br.com.jobinder.identityservice.domain.user.UserRepository;
import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileCreateDTO;
import br.com.jobinder.identityservice.dto.serviceprofile.ServiceProfileResponseDTO;
import br.com.jobinder.identityservice.infra.exception.serviceprofile.ServiceProfileAlreadyExistsException;
import br.com.jobinder.identityservice.infra.exception.serviceprofile.ServiceProfileNotFoundException;
import br.com.jobinder.identityservice.infra.exception.user.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceProfileService {

    @Autowired
    private ServiceProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private SpecialtyService specialtyService;

    // Map entity to DTO
    private ServiceProfileResponseDTO toResponseDto(ServiceProfile profile) {
        return new ServiceProfileResponseDTO(
                profile.getId(),
                profile.getUser().getId(),
                profile.getSpecialty().getName(),
                profile.getLocation().getCity(),
                profile.getLocation().getState(),
                profile.getServiceImageUrl(),
                profile.getServicesPerformed(),
                profile.getRating(),
                profile.getAvailability(),
                profile.getDescription(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    @Transactional
    public ServiceProfileResponseDTO createServiceProfile(ServiceProfileCreateDTO dto, UUID authenticatedUserId) {
        // Check if the user exists (in case the token is valid but the user was deleted)
        var user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        // Check if the user already has a service profile
        if (profileRepository.existsByUserId(authenticatedUserId)) {
            throw new ServiceProfileAlreadyExistsException("User already has a profile.");
        }

        // Find or create Location and Specialty
        var location = locationService.findOrCreateLocation(dto.city(), dto.state());
        var specialty = specialtyService.findOrCreateSpecialty(dto.specialtyName());

        var newProfile = new ServiceProfile(
                null,
                user,
                specialty,
                location,
                dto.serviceImageUrl(),
                0,
                0.0f,
                dto.availability(),
                dto.description(),
                null,
                null
        );

        var savedProfile = profileRepository.save(newProfile);

        return toResponseDto(savedProfile);
    }

    @PreAuthorize("authentication.name == #userId.toString() or hasRole('ADMIN')")
    public ServiceProfileResponseDTO getServiceProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(this::toResponseDto)
                .orElseThrow(() -> new ServiceProfileNotFoundException("Service Profile not found for user with ID: " + userId));
    }

    public UUID findUserIdByServiceProfileId(UUID profileId) {
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ServiceProfileNotFoundException("Profile not found with ID: " + profileId));
        return profile.getUser().getId();
    }

    public List<ServiceProfileResponseDTO> getAllServiceProfilesDTO() {
        var profiles = profileRepository.findAll();
        return profiles.stream().map(this::toResponseDto).toList();
    }
}