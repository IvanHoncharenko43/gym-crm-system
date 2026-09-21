package org.example.macrocycle.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MacrocycleGenerationProperties.class)
public class AsyncConfig {

    private final MacrocycleGenerationProperties properties;

    @Bean(name = "macrocycleTaskExecutor")
    public ThreadPoolTaskExecutor macrocycleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.generation().corePoolSize());
        executor.setMaxPoolSize(properties.generation().maxPoolSize());
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("macrocycle-gen-");
        executor.initialize();
        return executor;
    }
}
