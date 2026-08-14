package org.example.user;

import org.example.config.SecurityConfig;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.AccessForbiddenException;
import org.example.exception.EntityNotFoundException;
import org.example.security.service.JwtService;
import org.example.security.service.TokenBlackListService;
import org.example.user.controller.UserController;
import org.example.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.example.TestUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerIT {

    private final static UserDetails USER_DETAILS = getTraineeUserDetails();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return200_RequestIsValid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");
        doNothing().when(userService).changePassword(TRAINEE_ID, request, USER_DETAILS);

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(userService, times(1)).changePassword(TRAINEE_ID, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return400AndProblemDetail_OldPasswordIsBlank() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "newPassword1234!");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return400AndProblemDetail_NewPasswordIsBlank() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword1234", "");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return400AndProblemDetail_NewPasswordIsInvalid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "short");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    void changePassword_Return401AndProblemDetail_UserIsUnauthenticated() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Failure"))
                .andExpect(jsonPath("$.detail").value("Authentication failed during accessing the resource"));
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return403AndProblemDetail_UserIsNotOwner() throws Exception {
        Long id = 99L;
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");
        doThrow(new AccessForbiddenException("Authorization failed"))
                .when(userService).changePassword(id, request, USER_DETAILS);

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Authorization Failure"))
                .andExpect(jsonPath("$.detail").value("Authorization failed"));
        verify(userService, times(1)).changePassword(id, request, USER_DETAILS);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, password = TRAINEE_PASSWORD , roles = {"TRAINEE"})
    void changePassword_Return404AndProblemDetail_UserNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).changePassword(TRAINEE_ID, request, USER_DETAILS);

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found"));
        verify(userService, times(1)).changePassword(TRAINEE_ID, request, USER_DETAILS);
    }
}
