package org.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
//import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ClientConfigurationProperties.class)
public class ClientConfig {

    private final ClientConfigurationProperties clientConfigurationProperties;

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder(){
//        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
//                .withConnectTimeout(Duration.ofSeconds(2))
//                .withReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder();
//                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings));
    }

    @Bean
    public TrainerWorkloadClient trainerWorkloadClient(RestClient.Builder restClientBuilder, TokenPopulationInterceptor tokenPopulationInterceptor){
        RestClient restClient = restClientBuilder
                .baseUrl(clientConfigurationProperties.workloadUrl())
                .requestInterceptor(tokenPopulationInterceptor)
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TrainerWorkloadClient.class);
    }
}
