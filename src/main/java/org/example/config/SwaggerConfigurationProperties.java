package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record SwaggerConfigurationProperties(
        @Value("${swagger.ui.path}") String uiPath,
        @Value("${swagger.ui.resource.path}") String uiResourcePath,
        @Value("${swagger.api.docs.url}") String apiDocsUrl,
        @Value("${swagger.api.docs.resource.path}") String apiDocsResourcePath,
        @Value("${swagger.html.url}") String htmlUrl,
        @Value("${swagger.redirect.url}") String redirectUrl
) {
}
