package org.example.crm.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import org.example.crm.config.ClientConfig;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.config.TokenPopulationInterceptor;
import org.example.crm.config.TraceIdPopulationInterceptor;
import org.example.crm.core.filter.TraceIdFilter;
import org.example.crm.core.service.GymMapper;
import org.example.crm.exception.DownstreamClientErrorException;
import org.example.crm.exception.DownstreamUnavailableException;
import org.example.crm.trainer.client.response.TrainerWorkloadClientResponse;
import org.example.crm.trainer.controller.request.TrainerMonthlyWorkloadRequest;
import org.example.crm.trainer.controller.response.TrainerWorkloadSummary;
import org.example.crm.trainer.service.TrainerWorkloadService;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.utils.PasswordGenerator;
import org.example.crm.utils.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(
        classes = {
                ClientConfig.class,
                TrainerWorkloadService.class,
                GymMapper.class,
                TokenPopulationInterceptor.class,
                TraceIdPopulationInterceptor.class,
                RequestHeaderContextResolver.class,
                TrainerWorkloadServiceIT.WorkloadTestConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ImportAutoConfiguration({CircuitBreakerAutoConfiguration.class, RetryAutoConfiguration.class})
@TestPropertySource(properties = {
        "app.client.services.workload-id=trainer-workload-service",
        "resilience4j.retry.instances.trainerWorkloadService.wait-duration=5ms"
})
class TrainerWorkloadServiceIT {

    private static final String WORKLOAD_SERVICE_ID = "trainer-workload-service";
    private static final int WORKLOAD_PORT = 80;
    private static final String BASE_URL = "http://" + WORKLOAD_SERVICE_ID + ":" + WORKLOAD_PORT;
    private static final String WORKLOAD_URI_TEMPLATE =
            BASE_URL + "/api/v1/trainers/workloads?username={username}&year={year}&month={month}";
    private static final String TRAINER_USERNAME = "John.Doe1";

    @MockitoBean
    private UsernameGenerator usernameGenerator;

    @MockitoBean
    private PasswordGenerator passwordGenerator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        mockServer.reset();
        circuitBreakerRegistry.circuitBreaker("trainerWorkloadService").reset();
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getWorkload_ReturnsTrainerWorkloadSummary_RequestIsValid() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token");
        servletRequest.setAttribute(TraceIdFilter.TRACE_ID_KEY, "trace-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        TrainerMonthlyWorkloadRequest request = new TrainerMonthlyWorkloadRequest(TRAINER_USERNAME, 2026, 8);
        TrainerWorkloadClientResponse clientResponse = new TrainerWorkloadClientResponse(
                TRAINER_USERNAME, new FullName("John", "Doe"), true, 2026, 8, 480);

        mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(header(TraceIdFilter.TRACE_ID_HEADER, "trace-123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(clientResponse), MediaType.APPLICATION_JSON));

        TrainerWorkloadSummary summary = trainerWorkloadService.getWorkload(request);

        assertThat(summary.username()).isEqualTo(TRAINER_USERNAME);
        assertThat(summary.fullName()).isEqualTo(new FullName("John", "Doe"));
        assertThat(summary.isActive()).isTrue();
        assertThat(summary.year()).isEqualTo(2026);
        assertThat(summary.month()).isEqualTo(8);
        assertThat(summary.trainingSummaryDurationMinutes()).isEqualTo(480);
        mockServer.verify();
    }

    @Test
    void getWorkload_ThrowsDownstreamClientErrorException_OnNonRetryable4xx() throws Exception {
        TrainerMonthlyWorkloadRequest request = new TrainerMonthlyWorkloadRequest(TRAINER_USERNAME, 2026, 8);
        String detail = "Trainer not found";

        mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(
                                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail))));
        assertThatThrownBy(() -> trainerWorkloadService.getWorkload(request))
                .isInstanceOf(DownstreamClientErrorException.class)
                .satisfies(ex -> {
                    DownstreamClientErrorException exception = (DownstreamClientErrorException) ex;
                    assertThat(exception.getServiceName()).isEqualTo(WORKLOAD_SERVICE_ID);
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getDetail()).isEqualTo(detail);
                });
        mockServer.verify();
    }

    @Test
    void getWorkload_ThrowsDownstreamUnavailableException_ConnectionFailsOnEveryAttempt() {
        TrainerMonthlyWorkloadRequest request = new TrainerMonthlyWorkloadRequest(TRAINER_USERNAME, 2026, 8);

        for (int i = 0; i < 3; i++) {
            mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(req -> {
                        throw new IOException("connection failure");
                    });
        }

        assertThatThrownBy(() -> trainerWorkloadService.getWorkload(request))
                .isInstanceOf(DownstreamUnavailableException.class)
                .satisfies(ex -> {
                    DownstreamUnavailableException exception = (DownstreamUnavailableException) ex;
                    assertThat(exception.getServiceId()).isEqualTo(WORKLOAD_SERVICE_ID);
                    assertThat(exception.getCause()).isInstanceOf(ResourceAccessException.class);
                });
        mockServer.verify();
    }

    @Test
    void getWorkload_ThrowsDownstreamUnavailableException_CircuitBreakerIsOpen() {
        circuitBreakerRegistry.circuitBreaker("trainerWorkloadService").transitionToOpenState();
        TrainerMonthlyWorkloadRequest request = new TrainerMonthlyWorkloadRequest(TRAINER_USERNAME, 2026, 8);

        assertThatThrownBy(() -> trainerWorkloadService.getWorkload(request))
                .isInstanceOf(DownstreamUnavailableException.class);
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class WorkloadTestConfig {

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
}
