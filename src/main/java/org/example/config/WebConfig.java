package org.example.config;

import org.example.core.service.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final InterceptorConfigurationProperties interceptorConfigurationProperties;

    public WebConfig(AuthInterceptor authInterceptor, InterceptorConfigurationProperties interceptorConfigurationProperties){
        this.authInterceptor = authInterceptor;
        this.interceptorConfigurationProperties = interceptorConfigurationProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(interceptorConfigurationProperties.includePath())
                .excludePathPatterns(interceptorConfigurationProperties.excludePath());
    }
}
