package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.example.exception.TooManyLoginAttemptsException;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.controller.dto.LoginDetails;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final static String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlackListService tokenBlackListService;

    public LoginDetails login(LoginRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return new LoginDetails(token);
        }
        catch (LockedException e){
            throw new TooManyLoginAttemptsException("Account is temporarily locked due to too many failed login attempts");
        } catch (DisabledException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public void logout(String header){
        String token = header.substring(BEARER_PREFIX.length());
        tokenBlackListService.blackListToken(token);
    }
}
