package org.example.security;

import org.example.TestUtils;
import org.example.security.controller.dto.LoginDetails;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.service.AuthService;
import org.example.security.service.JwtService;
import org.example.security.service.TokenBlackListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final LoginRequest LOGIN_REQUEST = TestUtils.getLoginRequest();
    private static final UserDetails USER_DETAILS = TestUtils.getTraineeUserDetails();

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlackListService tokenBlackListService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ReturnLoginDetails_CredentialsValid() {
        String token = "token";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(USER_DETAILS);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(USER_DETAILS)).thenReturn(token);

        LoginDetails result = authService.login(LOGIN_REQUEST);
        assertEquals(token, result.token());
        verify(jwtService, times(1)).generateToken(USER_DETAILS);
    }

    @Test
    void login_ThrowBadCredentialsException_InvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(LOGIN_REQUEST));
        assertTrue(exception.getMessage().contains("Invalid username or password"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ThrowLockedException_AccountLocked() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("Account is temporarily locked due to too many failed login attempts"));

        LockedException exception = assertThrows(LockedException.class,
                () -> authService.login(LOGIN_REQUEST));
        assertTrue(exception.getMessage().contains("locked"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ThrowDisabledException_AccountInactive() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("The account is inactive"));

        DisabledException exception = assertThrows(DisabledException.class,
                () -> authService.login(LOGIN_REQUEST));
        assertTrue(exception.getMessage().contains("inactive"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_BlackListToken_ValidHeader() {
        String header = "Bearer jwt-token";

        authService.logout(header);
        verify(tokenBlackListService, times(1)).blackListToken("jwt-token");
    }
}
