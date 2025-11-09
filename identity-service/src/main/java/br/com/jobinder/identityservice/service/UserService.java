package br.com.jobinder.identityservice.service;

import br.com.jobinder.identityservice.domain.user.User;
import br.com.jobinder.identityservice.domain.user.UserRepository;
import br.com.jobinder.identityservice.dto.internal.InternalUserAuthDTO;
import br.com.jobinder.identityservice.dto.user.UserChangePasswordDTO;
import br.com.jobinder.identityservice.dto.user.UserCreateDTO;
import br.com.jobinder.identityservice.dto.user.UserResponseDTO;
import br.com.jobinder.identityservice.dto.user.UserUpdateDTO;
import br.com.jobinder.identityservice.infra.exception.user.InvalidPasswordException;
import br.com.jobinder.identityservice.infra.exception.user.PhoneNumberInvalidException;
import br.com.jobinder.identityservice.infra.exception.user.UserAlreadyExistsException;
import br.com.jobinder.identityservice.infra.exception.user.UserNotFoundException;
import com.google.i18n.phonenumbers.NumberParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Thread-safe singleton instance of PhoneNumberUtil
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Transactional
    public UserResponseDTO registerUser(UserCreateDTO createDTO) {
        PhoneNumber phoneNumber;

        // Try to parse the phone number
        try {
            phoneNumber = phoneUtil.parse(createDTO.nationalNumber(), createDTO.countryCode());
        } catch (NumberParseException e) {
            throw new PhoneNumberInvalidException("Invalid phone number format: " + e.getMessage());
        }

        // Check if the number is valid for the given region
        if (!phoneUtil.isValidNumber(phoneNumber)) {
            throw new PhoneNumberInvalidException("Invalid phone number for the region " + createDTO.countryCode());
        }

        // Format the number to E.164 standard
        String e164FormattedPhone = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        // Check if a user with the same phone number already exists
        if (userRepository.existsByPhone(e164FormattedPhone)) {
            throw new UserAlreadyExistsException("A user with this phone number already exists.");
        }

        // Encode the password using BCrypt
        var encodedPassword = passwordEncoder.encode(createDTO.password());

        var user = new User(
                null,
                e164FormattedPhone,
                createDTO.firstName(),
                createDTO.lastName(),
                encodedPassword,
                createDTO.role(),
                null,
                null
        );

        // Save the user to the database
        var savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getPhone(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    public UserResponseDTO findUserDTOById(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return new UserResponseDTO(
                user.getId(),
                user.getPhone(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserUpdateDTO updateDTO) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Update first name if provided
        if (updateDTO.firstName() != null && !updateDTO.firstName().isBlank()) {
            user.setFirstName(updateDTO.firstName());
        }

        // Update last name if provided
        if (updateDTO.lastName() != null && !updateDTO.lastName().isBlank()) {
            user.setLastName(updateDTO.lastName());
        }

        var savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getPhone(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    @Transactional
    public void changePassword(UUID userId, UserChangePasswordDTO passwordDTO) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(passwordDTO.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password does not match.");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public InternalUserAuthDTO findAuthDetailsByPhone(String phone) {
        var user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phone));

        return new InternalUserAuthDTO(
                user.getId(),
                user.getPhone(),
                user.getPassword(),
                user.getRole().name()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
}
