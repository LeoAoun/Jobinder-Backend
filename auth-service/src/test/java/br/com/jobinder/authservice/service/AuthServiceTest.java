package br.com.jobinder.authservice.service;

import br.com.jobinder.authservice.client.IdentityServiceClient;
import br.com.jobinder.authservice.dto.InternalUserAuthDTO;
import br.com.jobinder.authservice.dto.LoginRequestDTO;
import br.com.jobinder.authservice.dto.LoginResponseDTO;
import br.com.jobinder.authservice.infra.exception.InvalidCredentialsException;
import br.com.jobinder.authservice.infra.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IdentityServiceClient identityServiceClient;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should return LoginResponseDTO when credentials are valid")
    void login_WithValidCredentials_ShouldReturnDTO() {
        var userId = UUID.randomUUID();
        var loginRequest = new LoginRequestDTO("11999999999", "password123");
        var userDTO = new InternalUserAuthDTO(userId, "11999999999", "$2a$10$hashedPasswordValue", "USER");
        var fakeToken = "fake.jwt.token";

        when(identityServiceClient.getUserAuthDetails("11999999999")).thenReturn(userDTO);
        when(passwordEncoder.matches("password123", "$2a$10$hashedPasswordValue")).thenReturn(true);
        when(tokenProvider.generateToken(userDTO)).thenReturn(fakeToken);

        LoginResponseDTO response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(fakeToken);
        assertThat(response.type()).isEqualTo("Bearer");

        verify(identityServiceClient, times(1)).getUserAuthDetails("11999999999");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedPasswordValue");
        verify(tokenProvider, times(1)).generateToken(userDTO);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when the password does not match")
    void login_WithIncorrectPassword_ShouldThrowException() {
        var userId = UUID.randomUUID();
        var loginRequest = new LoginRequestDTO("11999999999", "wrongPassword");
        var userDTO = new InternalUserAuthDTO(userId, "11999999999", "$2a$10$hashedPasswordValue", "USER");

        when(identityServiceClient.getUserAuthDetails("11999999999")).thenReturn(userDTO);
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPasswordValue")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid phone number or password.");

        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw exception when the user is not found")
    void login_WithNonExistentUser_ShouldThrowException() {
        var loginRequest = new LoginRequestDTO("nonexistentPhone", "password123");

        when(identityServiceClient.getUserAuthDetails("nonexistentPhone"))
                .thenThrow(new RuntimeException("User not found"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }
}
