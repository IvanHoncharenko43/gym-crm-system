package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.example.security.controller.dto.LoginRequest;
import org.example.security.controller.dto.LoginDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new LoginDetails(token);
    }

    public void logout(String header){
        String token = header.substring(BEARER_PREFIX.length());
        tokenBlackListService.blackListToken(token);
    }
}
