package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.user.UpdateUserRequest;
import org.library.dto.user.UserResponse;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== GET ALL USERS TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsersSuccess() throws Exception {
        UserResponse user1 = new UserResponse(1L, "John Doe", "john@email.com", "USER", true);
        UserResponse user2 = new UserResponse(2L, "Jane Smith", "jane@email.com", "ADMIN", true);
        UserResponse user3 = new UserResponse(3L, "Bob Johnson", "bob@email.com", "USER", false);
        List<UserResponse> users = Arrays.asList(user1, user2, user3);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(
                        get("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@email.com"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].name").value("Bob Johnson"))
                .andExpect(jsonPath("$[2].role").value("USER"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsersEmpty() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllUsersForbidden() throws Exception {
        mockMvc.perform(
                        get("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByIdSuccess() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(userId, "John Doe", "john@email.com", "USER", true);

        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByIdNotFound() throws Exception {
        Long userId = 999L;

        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        get("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserByIdForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        get("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(anyLong());
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserSuccess() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        UserResponse response = new UserResponse(userId, "Updated Name", "john@email.com", "USER", true);

        when(userService.updateUser(userId, request)).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("john@email.com"));

        verify(userService).updateUser(userId, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserNotFound() throws Exception {
        Long userId = 999L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userService.updateUser(userId, request))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(userService).updateUser(userId, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserInvalidRequest_BlankName() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(""); // Blank name - invalid

        mockMvc.perform(
                        put("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserInvalidRequest_NullName() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(null);

        mockMvc.perform(
                        put("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateUserForbidden() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        mockMvc.perform(
                        put("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    // ========== PROMOTE USER TO ADMIN TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPromoteUserToAdminSuccess() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(userId, "John Doe", "john@email.com", "ADMIN", true);

        when(userService.promoteUserToAdmin(userId)).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/users/{userId}/promote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).promoteUserToAdmin(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPromoteUserToAdminNotFound() throws Exception {
        Long userId = 999L;

        when(userService.promoteUserToAdmin(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/promote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).promoteUserToAdmin(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPromoteUserToAdminAlreadyAdmin() throws Exception {
        Long userId = 1L;

        when(userService.promoteUserToAdmin(userId))
                .thenThrow(new ConflictException("User is already an admin"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/promote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());

        verify(userService).promoteUserToAdmin(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testPromoteUserToAdminForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        put("/api/admin/users/{userId}/promote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).promoteUserToAdmin(anyLong());
    }

    // ========== DEMOTE ADMIN TO USER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminToUserSuccess() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(userId, "John Doe", "john@email.com", "USER", true);

        when(userService.demoteAdminToUser(userId)).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/users/{userId}/demote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).demoteAdminToUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminToUserNotFound() throws Exception {
        Long userId = 999L;

        when(userService.demoteAdminToUser(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/demote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).demoteAdminToUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminToUserNotAdmin() throws Exception {
        Long userId = 1L;

        when(userService.demoteAdminToUser(userId))
                .thenThrow(new ConflictException("User is not an admin"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/demote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());

        verify(userService).demoteAdminToUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminToUserCannotDemoteSelf() throws Exception {
        Long userId = 1L;

        when(userService.demoteAdminToUser(userId))
                .thenThrow(new BadRequestException("You cannot demote yourself"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/demote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verify(userService).demoteAdminToUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDemoteAdminToUserForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        put("/api/admin/users/{userId}/demote", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).demoteAdminToUser(anyLong());
    }

    // ========== ACTIVATE USER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateUserSuccess() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(userId, "John Doe", "john@email.com", "USER", true);

        when(userService.activateUser(userId)).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/users/{userId}/activate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(userService).activateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateUserNotFound() throws Exception {
        Long userId = 999L;

        when(userService.activateUser(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/activate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).activateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateUserAlreadyActive() throws Exception {
        Long userId = 1L;

        when(userService.activateUser(userId))
                .thenThrow(new ConflictException("User is already active"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/activate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());

        verify(userService).activateUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testActivateUserForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        put("/api/admin/users/{userId}/activate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).activateUser(anyLong());
    }

    // ========== DEACTIVATE USER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUserSuccess() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(userId, "John Doe", "john@email.com", "USER", false);

        when(userService.deactivateUser(userId)).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/users/{userId}/deactivate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(userService).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUserNotFound() throws Exception {
        Long userId = 999L;

        when(userService.deactivateUser(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/deactivate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUserAlreadyDeactivated() throws Exception {
        Long userId = 1L;

        when(userService.deactivateUser(userId))
                .thenThrow(new ConflictException("User is already deactivated"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/deactivate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());

        verify(userService).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUserCannotDeactivateSelf() throws Exception {
        Long userId = 1L;

        when(userService.deactivateUser(userId))
                .thenThrow(new BadRequestException("You cannot deactivate yourself"));

        mockMvc.perform(
                        put("/api/admin/users/{userId}/deactivate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verify(userService).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeactivateUserForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        put("/api/admin/users/{userId}/deactivate", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).deactivateUser(anyLong());
    }

    // ========== DELETE USER TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserSuccess() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(
                        delete("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserNotFound() throws Exception {
        Long userId = 999L;

        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(
                        delete("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserCannotDeleteSelf() throws Exception {
        Long userId = 1L;

        doThrow(new BadRequestException("You cannot delete yourself"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(
                        delete("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteUserForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        delete("/api/admin/users/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyLong());
    }
}