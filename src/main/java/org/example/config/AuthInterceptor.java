package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.user.controller.dto.UserCredentials;
import org.example.utils.BasicAuthDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final BasicAuthDecoder basicAuthDecoder;

    private final Set<String> bypassPostPaths;

    public AuthInterceptor(BasicAuthDecoder basicAuthDecoder,
                           @Value("${auth.bypass.post.paths}") String[] bypassPostPaths){
        this.basicAuthDecoder = basicAuthDecoder;
        this.bypassPostPaths = bypassPostPaths != null && bypassPostPaths.length > 0
                ? new HashSet<>(Arrays.asList(bypassPostPaths)) : Collections.emptySet();
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
