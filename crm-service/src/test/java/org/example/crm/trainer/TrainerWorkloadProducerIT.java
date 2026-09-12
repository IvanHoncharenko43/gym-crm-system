package org.example.crm.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.trainer.messaging.TrainerWorkloadProducer;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.user.controller.dto.FullName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;

@SpringBootTest(classes = {
        TrainerWorkloadProducer.class,
        RequestHeaderContextResolver.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@ActiveProfiles("kafka-it")
@EmbeddedKafka(partitions = 1, topics = TrainerWorkloadProducerIT.TOPIC)
class TrainerWorkloadProducerIT {

    static final String TOPIC = "trainer-workload-update-event";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private TrainerWorkloadProducer producerService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void publishTrainerWorkloadUpdateEvent_SendSerializedEvent_NoRequestHeader() throws Exception {
        TopicPartition partition = new TopicPartition(TOPIC, 0);
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                "John.Doe", new FullName("John", "Doe"), true, LocalDate.of(2026, 5, 12), 90);

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, true, TOPIC);
            long offsetBefore = consumer.position(partition);
            producerService.publishTrainerWorkloadUpdateEvent(event);
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(10));
            long offsetAfter = consumer.position(partition);

            assertThat(offsetAfter - offsetBefore).isEqualTo(1);
            assertThat(record.topic()).isEqualTo(TOPIC);
            assertThat(OBJECT_MAPPER.readValue(record.value(), TrainerWorkloadUpdateEvent.class)).isEqualTo(event);
            assertThat(record.headers().lastHeader(TRACE_ID_KEY)).isNull();
        }
    }

    @Test
    void publishTrainerWorkloadUpdateEvent_AddTraceIdToHeaderAndSendSerializedEvent_TraceIdInRequestHeaders() throws Exception {
        String traceId = "trace-123";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TRACE_ID_KEY, traceId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        TopicPartition partition = new TopicPartition(TOPIC, 0);

        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                "Jane.Smith", new FullName("Jane", "Smith"), true, LocalDate.of(2026, 6, 3), 60);

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, true, TOPIC);
            long offsetBefore = consumer.position(partition);
            producerService.publishTrainerWorkloadUpdateEvent(event);
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(10));
            long offsetAfter = consumer.position(partition);

            assertThat(offsetAfter - offsetBefore).isEqualTo(1);
            Header traceHeader = record.headers().lastHeader(TRACE_ID_KEY);
            assertThat(traceHeader).isNotNull();
            assertThat(new String(traceHeader.value(), StandardCharsets.UTF_8)).isEqualTo(traceId);
            assertThat(record.topic()).isEqualTo(TOPIC);
            assertThat(OBJECT_MAPPER.readValue(record.value(), TrainerWorkloadUpdateEvent.class)).isEqualTo(event);
        }
    }
}
