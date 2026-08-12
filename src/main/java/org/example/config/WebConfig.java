package org.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableConfigurationProperties({
        AuthConfigurationProperties.class,
        InterceptorConfigurationProperties.class,
        DbPrivilegesConfigurationProperties.class
})
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final InterceptorConfigurationProperties interceptorConfigurationProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(interceptorConfigurationProperties.includePath());
    }
}
