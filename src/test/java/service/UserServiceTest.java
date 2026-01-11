package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.user.*;
import org.library.entities.Role;
import org.library.entities.User;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.UserRepository;
import org.library.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private User currentUser;
    private User testUser;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(userRepository, passwordEncoder);

        // Setup test data
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Current User");
        currentUser.setEmail("current@email.com");
        currentUser.setPassword("hashedPassword");
        currentUser.setRole(Role.USER);
        currentUser.setActive(true);

        testUser = new User();
        testUser.setId(2L);
        testUser.setName("Test User");
        testUser.setEmail("test@email.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(Role.USER);
        testUser.setActive(true);

        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("current@email.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // User Profile Management Tests

    @Test
    void testGetMyProfileSuccess() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));

        UserProfileResponse response = userService.getMyProfile();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Current User", response.getName());
        assertEquals("current@email.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertTrue(response.isActive());

        verify(userRepository).findByEmail("current@email.com");
    }

    @Test
    void testGetMyProfileNotFound() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getMyProfile());

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("current@email.com");
    }

    @Test
    void testUpdateMyProfileSuccess() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        UserProfileResponse response = userService.updateMyProfile(request);

        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        assertEquals("current@email.com", response.getEmail());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangeMyPasswordSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changeMyPassword(request);

        verify(userRepository).findByEmail("current@email.com");
        verify(passwordEncoder).matches("oldPassword", "hashedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangeMyPasswordWrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.changeMyPassword(request));

        assertEquals("Current password is incorrect", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // Admin User Management Tests

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        user1.setEmail("user1@email.com");
        user1.setRole(Role.USER);
        user1.setActive(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");
        user2.setEmail("user2@email.com");
        user2.setRole(Role.ADMIN);
        user2.setActive(true);

        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("User 1", responses.get(0).getName());
        assertEquals("User 2", responses.get(1).getName());

        verify(userRepository).findAll();
    }

    @Test
    void testGetUserByIdSuccess() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(2L);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@email.com", response.getEmail());

        verify(userRepository).findById(2L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(999L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void testUpdateUserSuccess() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(2L, request);

        assertNotNull(response);
        assertEquals("Updated Name", response.getName());

        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testPromoteUserToAdminSuccess() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        UserResponse response = userService.promoteUserToAdmin(2L);

        assertNotNull(response);
        assertEquals(Role.ADMIN, testUser.getRole());

        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testPromoteUserToAdminAlreadyAdmin() {
        testUser.setRole(Role.ADMIN);

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.promoteUserToAdmin(2L));

        assertEquals("User is already an admin", exception.getMessage());

        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDemoteAdminToUserSuccess() {
        testUser.setRole(Role.ADMIN);

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.demoteAdminToUser(2L);

        assertNotNull(response);
        assertEquals(Role.USER, testUser.getRole());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDemoteAdminToUserNotAdmin() {
        testUser.setRole(Role.USER);

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.demoteAdminToUser(2L));

        assertEquals("User is not an admin", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDemoteAdminToUserSelfDemotion() {
        currentUser.setRole(Role.ADMIN);

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.demoteAdminToUser(1L));

        assertEquals("You cannot demote yourself", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testActivateUserSuccess() {
        testUser.setActive(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.activateUser(2L);

        assertNotNull(response);
        assertTrue(testUser.isActive());

        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testActivateUserAlreadyActive() {
        testUser.setActive(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.activateUser(2L));

        assertEquals("User is already active", exception.getMessage());

        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeactivateUserSuccess() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.deactivateUser(2L);

        assertNotNull(response);
        assertFalse(testUser.isActive());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeactivateUserAlreadyDeactivated() {
        testUser.setActive(false);

        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.deactivateUser(2L));

        assertEquals("User is already deactivated", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeactivateUserSelfDeactivation() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.deactivateUser(1L));

        assertEquals("You cannot deactivate yourself", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.deleteUser(2L);

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(2L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(999L));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUserSelfDeletion() {
        when(userRepository.findByEmail("current@email.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.deleteUser(1L));

        assertEquals("You cannot delete yourself", exception.getMessage());

        verify(userRepository).findByEmail("current@email.com");
        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }
}