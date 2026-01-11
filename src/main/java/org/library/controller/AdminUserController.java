package org.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.dto.user.*;
import org.library.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Admin endpoints for managing users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all users in the system")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a specific user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Updates a user's information")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PutMapping("/{userId}/promote")
    @Operation(summary = "Promote user to admin", description = "Promotes a regular user to admin role")
    public ResponseEntity<UserResponse> promoteUserToAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.promoteUserToAdmin(userId));
    }

    @PutMapping("/{userId}/demote")
    @Operation(summary = "Demote admin to user", description = "Demotes an admin back to regular user role")
    public ResponseEntity<UserResponse> demoteAdminToUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.demoteAdminToUser(userId));
    }

    @PutMapping("/{userId}/activate")
    @Operation(summary = "Activate user", description = "Activates a deactivated user account")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deactivateUser(userId));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user account")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}