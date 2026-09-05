package org.example.crm.trainer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.crm.config.KafkaTopicsConfigurationProperties;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.trainer.messaging.TrainerWorkloadProducerService;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.user.controller.dto.FullName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadProducerServiceTest {

    private static final String TOPIC = "trainer-workload-update-event";
    private static final String TRAINER_USERNAME = "John.Doe";

    @Mock
    private KafkaTemplate<String, TrainerWorkloadUpdateEvent> kafkaTemplate;

    @Mock
    private RequestHeaderContextResolver requestHeaderContextResolver;

    private TrainerWorkloadProducerService producerService;

    @BeforeEach
    void setUp() {
        producerService = new TrainerWorkloadProducerService(
                kafkaTemplate, requestHeaderContextResolver, new KafkaTopicsConfigurationProperties(TOPIC));
    }

    @Test
    void publishTrainerWorkloadUpdateEvent_AddsTraceIdHeader_TraceIdPresent() {
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                TRAINER_USERNAME, new FullName("John", "Doe"), true, LocalDate.of(2026, 5, 12), 90);
        when(requestHeaderContextResolver.getTraceIdHeader()).thenReturn(Optional.of("trace-123"));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producerService.publishTrainerWorkloadUpdateEvent(event);

        ArgumentCaptor<ProducerRecord<String, TrainerWorkloadUpdateEvent>> captor = ArgumentCaptor.captor();
        verify(kafkaTemplate).send(captor.capture());
        ProducerRecord<String, TrainerWorkloadUpdateEvent> sentRecord = captor.getValue();

        assertEquals(TOPIC, sentRecord.topic());
        assertEquals(TRAINER_USERNAME, sentRecord.key());
        assertEquals(event, sentRecord.value());
        assertArrayEquals("trace-123".getBytes(StandardCharsets.UTF_8), sentRecord.headers().lastHeader(TRACE_ID_KEY).value());
    }

    @Test
    void publishTrainerWorkloadUpdateEvent_OmitsTraceIdHeader_TraceIdAbsent() {
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                TRAINER_USERNAME, new FullName("John", "Doe"), true, LocalDate.of(2026, 5, 12), 90);
        when(requestHeaderContextResolver.getTraceIdHeader()).thenReturn(Optional.empty());
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producerService.publishTrainerWorkloadUpdateEvent(event);

        ArgumentCaptor<ProducerRecord<String, TrainerWorkloadUpdateEvent>> captor = ArgumentCaptor.captor();
        verify(kafkaTemplate).send(captor.capture());
        ProducerRecord<String, TrainerWorkloadUpdateEvent> sentRecord = captor.getValue();

        assertEquals(TOPIC, sentRecord.topic());
        assertEquals(TRAINER_USERNAME, sentRecord.key());
        assertEquals(event, sentRecord.value());
        assertNull(captor.getValue().headers().lastHeader(TRACE_ID_KEY));
    }
}
