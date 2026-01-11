package org.library.service;

import org.library.dto.user.*;
import org.library.entities.Role;
import org.library.entities.User;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // User Profile Management Methods

    public UserProfileResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return toUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        User saved = userRepository.save(user);

        return toUserProfileResponse(saved);
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Admin User Management Methods

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse promoteUserToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new ConflictException("User is already an admin");
        }

        user.setRole(Role.ADMIN);
        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse demoteAdminToUser(Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ConflictException("User is not an admin");
        }

        // Prevent self-demotion
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot demote yourself");
        }

        user.setRole(Role.USER);
        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isActive()) {
            throw new ConflictException("User is already active");
        }

        user.setActive(true);
        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new ConflictException("User is already deactivated");
        }

        // Prevent self-deactivation
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot deactivate yourself");
        }

        user.setActive(false);
        User saved = userRepository.save(user);

        return toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prevent self-deletion
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot delete yourself");
        }

        userRepository.delete(user);
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive()
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive()
        );
    }
}