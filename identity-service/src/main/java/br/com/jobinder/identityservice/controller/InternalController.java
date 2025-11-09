package br.com.jobinder.identityservice.controller;

import br.com.jobinder.identityservice.dto.internal.InternalUserAuthDTO;
import br.com.jobinder.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalController {

    @Autowired
    private UserService userService;

    @GetMapping("/{phone}")
    public ResponseEntity<InternalUserAuthDTO> getUserAuthDetails(@PathVariable String phone) {
        var userDetails = userService.findAuthDetailsByPhone(phone);
        return ResponseEntity.ok(userDetails);
    }
}