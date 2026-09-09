package org.example.crm.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

@TestConfiguration
@EnableAspectJAutoProxy
public class ClientITConfig {
    public static final String WORKLOAD_SERVICE_ID = "trainer-workload-service";
    public static final int WORKLOAD_PORT = 80;

    private MockRestServiceServer mockRestServiceServer;

    @Bean
    RestClient.Builder restClientBuilder() {
        RestClient.Builder builder = RestClient.builder();
        this.mockRestServiceServer = MockRestServiceServer.bindTo(builder).build();
        return builder;
    }

    @Bean
    MockRestServiceServer mockRestServiceServer(RestClient.Builder restClientBuilder) {
        return this.mockRestServiceServer;
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    DiscoveryClient discoveryClient() {
        ServiceInstance instance = new DefaultServiceInstance(
                "trainer-workload-service-1", WORKLOAD_SERVICE_ID, WORKLOAD_SERVICE_ID, WORKLOAD_PORT, false);
        return new DiscoveryClient() {
            @Override
            public String description() {
                return "Test discovery client";
            }

            @Override
            public List<ServiceInstance> getInstances(String serviceId) {
                return List.of(instance);
            }

            @Override
            public List<String> getServices() {
                return List.of(WORKLOAD_SERVICE_ID);
            }
        };
    }
}
