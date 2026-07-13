package org.example.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "org.example")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    static {
        configureLogging();
    }

    private static void configureLogging() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        Logger hibernateLogger = (Logger) LoggerFactory.getLogger("org.hibernate.orm");
        hibernateLogger.setLevel(Level.ERROR);
        Logger hibernateMetrics = (Logger) LoggerFactory.getLogger("org.hibernate.session.metrics");
        hibernateMetrics.setLevel(Level.ERROR);
        Logger springOrmLogger = (Logger) LoggerFactory.getLogger("org.springframework.orm");
        springOrmLogger.setLevel(Level.INFO);
    }
}
