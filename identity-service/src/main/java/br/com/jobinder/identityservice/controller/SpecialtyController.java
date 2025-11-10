package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.dto.specialty.SpecialtyDTO;
import br.com.jobinder.identityservice.service.SpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specialties")
@Tag(name = "Specialties", description = "Endpoints for viewing service specialties")
public class SpecialtyController {

    @Autowired
    private SpecialtyService specialtyService;

    @Operation(summary = "List all available specialties",
            description = "Retrieves a public list of all service specialties available in the system for filtering or selection.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialties retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpecialtyDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<SpecialtyDTO>> getAllSpecialties() {
        var specialties = specialtyService.findAll();
        return ResponseEntity.ok(specialties);
    }
}