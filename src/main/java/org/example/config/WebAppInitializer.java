package org.example.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("contextConfigLocation", "java-config-only");
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, SwaggerConfig.class, OpenApiMetadataConfig.class,
                JerseyConfig.class);
        context.setServletContext(servletContext);

        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
        context.refresh();
        ServerConfigurationProperties serverProperties = context.getBean(ServerConfigurationProperties.class);

        ServletContainer jerseyContainer = new ServletContainer(context.getBean(JerseyConfig.class));
        ServletRegistration.Dynamic jerseyRegistration = servletContext.addServlet("jersey", jerseyContainer);
        jerseyRegistration.setLoadOnStartup(1);
        jerseyRegistration.addMapping(serverProperties.apiMapping());

        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic springRegistration = servletContext.addServlet("dispatcher", servlet);
        springRegistration.setLoadOnStartup(1);
        springRegistration.addMapping(serverProperties.servletMapping());
    }
}
