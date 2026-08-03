package org.example.core.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.user.controller.dto.UserCredentials;
import org.example.utils.BasicAuthDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final BasicAuthDecoder basicAuthDecoder;

    private final List<String> bypassPostPaths;

    public AuthInterceptor(BasicAuthDecoder basicAuthDecoder,
                           @Value("#{'${auth.bypass.post.paths}'.split(',')}") List<String> bypassPostPaths){
        this.basicAuthDecoder = basicAuthDecoder;
        this.bypassPostPaths = bypassPostPaths;
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
