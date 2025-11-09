package br.com.jobinder.identityservice.domain.serviceprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, UUID> {
    Optional<ServiceProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}