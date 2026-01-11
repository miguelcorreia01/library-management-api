package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.user.ChangePasswordRequest;
import org.library.dto.user.UpdateProfileRequest;
import org.library.dto.user.UserProfileResponse;
import org.library.exception.BadRequestException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== GET MY PROFILE TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyProfileSuccess() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "John Doe",
                "john@email.com",
                "USER",
                true
        );

        when(userService.getMyProfile()).thenReturn(response);

        mockMvc.perform(
                        get("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getMyProfile();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMyProfileSuccess_AdminRole() throws Exception {
        // ADMIN role should also work since SecurityConfig allows hasAnyRole("USER", "ADMIN")
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "Admin User",
                "admin@email.com",
                "ADMIN",
                true
        );

        when(userService.getMyProfile()).thenReturn(response);

        mockMvc.perform(
                        get("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Admin User"))
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).getMyProfile();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyProfileNotFound() throws Exception {
        when(userService.getMyProfile())
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        get("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).getMyProfile();
    }

    // ========== UPDATE MY PROFILE TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileSuccess() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");

        UserProfileResponse response = new UserProfileResponse(
                1L,
                "Updated Name",
                "john@email.com",
                "USER",
                true
        );

        when(userService.updateMyProfile(any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("john@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileInvalidRequest_BlankName() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName(""); // Blank name - invalid

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileInvalidRequest_NullName() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName(null);

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileInvalidRequest_MissingName() throws Exception {
        String emptyRequest = "{}";

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyRequest)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileNotFound() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");

        when(userService.updateMyProfile(any(UpdateProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(userService).updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateMyProfileWithWhitespaceOnly() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("   "); // Whitespace only - should be treated as blank

        mockMvc.perform(
                        put("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateMyProfile(any(UpdateProfileRequest.class));
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("newPassword123");

        doNothing().when(userService).changeMyPassword(any(ChangePasswordRequest.class));

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());

        verify(userService).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        doThrow(new BadRequestException("Current password is incorrect"))
                .when(userService).changeMyPassword(any(ChangePasswordRequest.class));

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_BlankCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(""); // Blank - invalid
        request.setNewPassword("newPassword123");

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_BlankNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword(""); // Blank - invalid

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_ShortNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("12345");

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_NullCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(null);
        request.setNewPassword("newPassword123");

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_NullNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword(null);

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordInvalidRequest_MissingFields() throws Exception {
        String emptyRequest = "{}";

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyRequest)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordUserNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("newPassword123");

        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).changeMyPassword(any(ChangePasswordRequest.class));

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(userService).changeMyPassword(any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePasswordValidMinimumLength() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("123456"); // Exactly 6 characters - valid

        doNothing().when(userService).changeMyPassword(any(ChangePasswordRequest.class));

        mockMvc.perform(
                        put("/api/user/profile/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());

        verify(userService).changeMyPassword(any(ChangePasswordRequest.class));
    }
}