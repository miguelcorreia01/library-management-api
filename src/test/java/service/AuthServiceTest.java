package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.auth.LoginRequest;
import org.library.dto.auth.RegisterRequest;
import org.library.dto.auth.JwtResponse;
import org.library.entities.Role;
import org.library.entities.User;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.repository.UserRepository;
import org.library.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private org.library.service.JwtService jwtService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(org.library.service.JwtService.class);

        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@email.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John");
        savedUser.setEmail(request.getEmail());
        savedUser.setPassword("hashedPassword");
        savedUser.setRole(Role.USER);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(request.getEmail(), "USER")).thenReturn("fake-jwt-token");

        JwtResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("John", response.getName());
        assertEquals("john@email.com", response.getEmail());
        assertEquals("USER", response.getRole());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(request.getEmail(), "USER");
    }

    @Test
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@email.com");
        request.setPassword("password123");

        User existingUser = new User();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(ConflictException.class, () -> authService.register(request));
        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@email.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@email.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);
        user.setActive(true);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken("john@email.com", "USER")).thenReturn("fake-jwt-token");

        JwtResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@email.com", response.getEmail());
        assertEquals("USER", response.getRole());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtService).generateToken("john@email.com", "USER");
    }

    @Test
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@email.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@email.com");
        user.setPassword("hashedPassword");
        user.setActive(true);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@email.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLoginDeactivatedAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@email.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@email.com");
        user.setPassword("hashedPassword");
        user.setActive(false);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Account is deactivated", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }
}