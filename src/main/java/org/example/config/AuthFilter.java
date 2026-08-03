package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.example.user.controller.dto.UserCredentials;
import org.example.utils.BasicAuthDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Provider
@Component
public class AuthFilter implements ContainerRequestFilter {

    private final BasicAuthDecoder basicAuthDecoder;
    private final Set<String> bypassPostPaths;

    @Context
    private HttpServletRequest httpServletRequest;

    public AuthFilter(BasicAuthDecoder basicAuthDecoder,
                      @Value("${auth.bypass.post.paths}") String[] bypassPostPaths){
        this.basicAuthDecoder = basicAuthDecoder;
        this.bypassPostPaths = bypassPostPaths != null && bypassPostPaths.length > 0
                ? new HashSet<>(Arrays.asList(bypassPostPaths)) : Collections.emptySet();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = httpServletRequest.getRequestURI();
        String method = requestContext.getMethod();
        if (method.equalsIgnoreCase("POST") && bypassPostPaths.contains(path)) {
            return;
        }
        String authHeader = requestContext.getHeaderString("Authorization");
        UserCredentials credentials = basicAuthDecoder.decode(authHeader);
        httpServletRequest.setAttribute("userCredentials", credentials);
    }
}
