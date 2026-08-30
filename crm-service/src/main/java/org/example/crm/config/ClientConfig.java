package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.example.crm.trainer.client.TrainerWorkloadClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ClientConfigurationProperties.class)
public class ClientConfig {

    private final ClientConfigurationProperties clientConfigurationProperties;
    private final DiscoveryClient discoveryClient;

    @Bean
    public RestClient trainerWorkloadRestClient(
            RestClient.Builder restClientBuilder,
            TokenPopulationInterceptor tokenPopulationInterceptor,
            TraceIdPopulationInterceptor traceIdPopulationInterceptor){

        List<ServiceInstance> instances = discoveryClient.getInstances(clientConfigurationProperties.workloadId());
        String resolveUrl = instances.getFirst().getUri().toString();
        return restClientBuilder
                .baseUrl(resolveUrl)
                .requestInterceptor(tokenPopulationInterceptor)
                .requestInterceptor(traceIdPopulationInterceptor)
                .defaultStatusHandler(HttpStatusCode::isError,
                        new DownstreamErrorStatusHandler(clientConfigurationProperties.workloadId()))
                .build();
    }

    @Bean
    public TrainerWorkloadClient trainerWorkloadClient(RestClient restClient){
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TrainerWorkloadClient.class);
    }
}
