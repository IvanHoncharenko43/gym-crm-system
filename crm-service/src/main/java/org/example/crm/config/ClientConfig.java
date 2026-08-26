package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.example.crm.trainer.controller.TrainerWorkloadClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ClientConfigurationProperties.class)
public class ClientConfig {

    private final ClientConfigurationProperties clientConfigurationProperties;
    private final DiscoveryClient discoveryClient;

    @Bean
    public TrainerWorkloadClient trainerWorkloadClient(
            RestClient.Builder restClientBuilder,
            TokenPopulationInterceptor tokenPopulationInterceptor,
            TraceIdPopulationInterceptor traceIdPopulationInterceptor){

        List<ServiceInstance> instances = discoveryClient.getInstances(clientConfigurationProperties.workloadId());
        String resolveUrl = instances.getFirst().getUri().toString();
        RestClient restClient = restClientBuilder
                .baseUrl(resolveUrl)
                .requestFactory(clientHttpRequestFactory())
                .requestInterceptor(tokenPopulationInterceptor)
                .requestInterceptor(traceIdPopulationInterceptor)
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TrainerWorkloadClient.class);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(clientConfigurationProperties.connectTimeout()));
        factory.setReadTimeout(Duration.ofSeconds(clientConfigurationProperties.readTimeout()));
        return factory;
    }
}
