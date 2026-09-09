package org.example.crm.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.crm.config.ClientConfig;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.config.TokenPopulationInterceptor;
import org.example.crm.config.TraceIdPopulationInterceptor;
import org.example.crm.core.ClientITConfig;
import org.example.crm.core.filter.TraceIdFilter;
import org.example.crm.exception.DownstreamClientErrorException;
import org.example.crm.trainer.client.TrainerWorkloadClient;
import org.example.crm.trainer.client.request.TrainerMonthlyWorkloadClientRequest;
import org.example.crm.trainer.client.response.TrainerWorkloadClientResponse;
import org.example.crm.user.controller.dto.FullName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.crm.TestUtils.TRAINER_USERNAME;
import static org.example.crm.core.ClientITConfig.WORKLOAD_PORT;
import static org.example.crm.core.ClientITConfig.WORKLOAD_SERVICE_ID;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(
        classes = {
                ClientConfig.class,
                TokenPopulationInterceptor.class,
                TraceIdPopulationInterceptor.class,
                RequestHeaderContextResolver.class,
                ClientITConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(properties = {
        "app.client.services.workload-id=trainer-workload-service"
})
public class TrainerWorkloadClientIT {

    private static final String BASE_URL = "http://" + WORKLOAD_SERVICE_ID + ":" + WORKLOAD_PORT;
    private static final String WORKLOAD_URI_TEMPLATE =
            BASE_URL + "/api/v1/trainers/workloads?username={username}&year={year}&month={month}";

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainerWorkloadClient trainerWorkloadClient;

    @BeforeEach
    void setUp() {
        mockServer.reset();
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

        TrainerMonthlyWorkloadClientRequest request = new TrainerMonthlyWorkloadClientRequest(
                TRAINER_USERNAME, 2026, 8);
        TrainerWorkloadClientResponse clientResponse = new TrainerWorkloadClientResponse(
                TRAINER_USERNAME, new FullName("John", "Doe"), true, 2026, 8, 480);

        mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(header(TraceIdFilter.TRACE_ID_HEADER, "trace-123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(clientResponse), MediaType.APPLICATION_JSON));

        TrainerWorkloadClientResponse response = trainerWorkloadClient.getWorkload(
                request.username(), request.year(), request.month());

        assertThat(response.username()).isEqualTo(TRAINER_USERNAME);
        assertThat(response.fullName()).isEqualTo(new FullName("John", "Doe"));
        assertThat(response.isActive()).isTrue();
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(8);
        assertThat(response.trainingSummaryDurationMinutes()).isEqualTo(480);
        mockServer.verify();
    }

    @Test
    void getWorkload_ThrowsDownstreamClientErrorException_OnNonRetryable4xx() throws Exception {
        TrainerMonthlyWorkloadClientRequest request = new TrainerMonthlyWorkloadClientRequest(
                TRAINER_USERNAME, 2026, 8);
        String detail = "Trainer not found";

        mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(
                                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail))));
        assertThatThrownBy(() -> trainerWorkloadClient.getWorkload(request.username(), request.year(), request.month()))
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
    void getWorkload_ThrowsResourceAccessException_ConnectionFailsOnEveryAttempt() {
        TrainerMonthlyWorkloadClientRequest request = new TrainerMonthlyWorkloadClientRequest(
                TRAINER_USERNAME, 2026, 8);

        mockServer.expect(requestToUriTemplate(WORKLOAD_URI_TEMPLATE, TRAINER_USERNAME, 2026, 8))
                .andExpect(method(HttpMethod.GET))
                .andRespond(req -> {
                    throw new IOException("connection failure");
                });

        assertThatThrownBy(() -> trainerWorkloadClient.getWorkload(request.username(), request.year(), request.month()))
                .isInstanceOf(ResourceAccessException.class);
        mockServer.verify();
    }
}
