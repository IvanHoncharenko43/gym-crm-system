package org.example.core.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.user.dto.UserCredentials;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final BasicAuthDecoder basicAuthDecoder;

    public AuthInterceptor(BasicAuthDecoder basicAuthDecoder){
        this.basicAuthDecoder = basicAuthDecoder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") &&
                (path.equals("/api/v1/trainees") || path.equals("/api/v1/trainers"))) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        UserCredentials credentials = basicAuthDecoder.decode(authHeader);
        request.setAttribute("userCredentials", credentials);
        return true;
    }
}
