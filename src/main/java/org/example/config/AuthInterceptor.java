package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.user.controller.dto.UserCredentials;
import org.example.utils.BasicAuthDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final BasicAuthDecoder basicAuthDecoder;

    private final Set<String> bypassPostPaths;

    public AuthInterceptor(BasicAuthDecoder basicAuthDecoder,
                           AuthConfigurationProperties authConfigurationProperties){
        this.basicAuthDecoder = basicAuthDecoder;
        this.bypassPostPaths = authConfigurationProperties.bypassPostPaths() != null
                ? authConfigurationProperties.bypassPostPaths() : Set.of();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") && bypassPostPaths.contains(path)) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        UserCredentials credentials = basicAuthDecoder.decode(authHeader);
        request.setAttribute("userCredentials", credentials);
        return true;
    }
}
