package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    private final SwaggerConfigurationProperties swaggerConfigurationProperties;

    public SwaggerConfig(SwaggerConfigurationProperties swaggerConfigurationProperties){
        this.swaggerConfigurationProperties = swaggerConfigurationProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(swaggerConfigurationProperties.uiPath())
                .addResourceLocations(swaggerConfigurationProperties.uiResourcePath());
        registry.addResourceHandler(swaggerConfigurationProperties.apiDocsUrl())
                .addResourceLocations(swaggerConfigurationProperties.apiDocsResourcePath());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(
                swaggerConfigurationProperties.htmlUrl(),
                swaggerConfigurationProperties.redirectUrl()
        );
    }
}
