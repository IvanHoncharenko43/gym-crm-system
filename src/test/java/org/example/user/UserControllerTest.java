package org.example.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.config.AuthInterceptor;
import org.example.config.InterceptorConfigurationProperties;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.EntityNotFoundException;
import org.example.user.controller.UserController;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        InterceptorConfigurationProperties interceptorConfigurationProperties() {
            return new InterceptorConfigurationProperties("/**");
        }
    }

    private final static UserCredentials CREDENTIALS = getTraineeCredentials();

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp(){
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void changePassword_Return200_RequestIsValid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");
        doNothing().when(userService).changePassword(TRAINEE_ID, request, CREDENTIALS);

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isOk());
        verify(userService, times(1)).changePassword(TRAINEE_ID, request, CREDENTIALS);
    }

    @Test
    void changePassword_Return400AndProblemDetail_OldPasswordIsBlank() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "newPassword1234!");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void changePassword_Return400AndProblemDetail_NewPasswordIsInvalid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "short");

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.invalidFields").isNotEmpty());
    }

    @Test
    void changePassword_Return404AndProblemDetail_UserNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINEE_PASSWORD, "newPassword1234!");
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).changePassword(TRAINEE_ID, request, CREDENTIALS);

        mockMvc.perform(put("/api/v1/users/{id}/profile/password-change", TRAINEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userCredentials", CREDENTIALS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entity Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found"));
        verify(userService, times(1)).changePassword(TRAINEE_ID, request, CREDENTIALS);
    }
}
