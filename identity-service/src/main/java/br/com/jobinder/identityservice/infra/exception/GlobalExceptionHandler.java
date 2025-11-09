package br.com.jobinder.identityservice.infra.exception;

import br.com.jobinder.identityservice.infra.exception.serviceprofile.ServiceProfileAlreadyExistsException;
import br.com.jobinder.identityservice.infra.exception.serviceprofile.ServiceProfileNotFoundException;
import br.com.jobinder.identityservice.infra.exception.user.InvalidPasswordException;
import br.com.jobinder.identityservice.infra.exception.user.PhoneNumberInvalidException;
import br.com.jobinder.identityservice.infra.exception.user.UserAlreadyExistsException;
import br.com.jobinder.identityservice.infra.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Handle user not found error
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    // Handle user already exists error
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    // Handle invalid phone number error
    @ExceptionHandler(PhoneNumberInvalidException.class)
    public ResponseEntity<Map<String, String>> handlePhoneNumberInvalid(PhoneNumberInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    // Handle service profile not found error
    @ExceptionHandler(ServiceProfileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProfileNotFound(ServiceProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    // Handle service profile already exists error
    @ExceptionHandler(ServiceProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleProfileAlreadyExists(ServiceProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    // Handle invalid password error
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}