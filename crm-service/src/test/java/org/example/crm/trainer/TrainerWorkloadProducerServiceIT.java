package org.example.crm.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.trainer.messaging.TrainerWorkloadProducerService;
import org.example.crm.trainer.messaging.TrainerWorkloadUpdateEvent;
import org.example.crm.user.controller.dto.FullName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;

@SpringBootTest(classes = {
        TrainerWorkloadProducerService.class,
        RequestHeaderContextResolver.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = TrainerWorkloadProducerServiceIT.TOPIC)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "app.kafka.topics.trainer-workload-update=" + TrainerWorkloadProducerServiceIT.TOPIC
})
class TrainerWorkloadProducerServiceIT {

    static final String TOPIC = "trainer-workload-update-event";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TrainerWorkloadProducerService producerService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void publishTrainerWorkloadUpdateEvent_SendSerializedEvent_NoRequestHeader() throws Exception {
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                "John.Doe", new FullName("John", "Doe"), true, LocalDate.of(2026, 5, 12), 90);

        producerService.publishTrainerWorkloadUpdateEvent(event);

        try (Consumer<String, String> consumer = createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
            ConsumerRecord<String, String> record = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> r.topic().equals(TOPIC))
                    .filter(r -> r.key().equals(event.username()))
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new AssertionError("No record found for key " + event.username()));

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

        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                "Jane.Smith", new FullName("Jane", "Smith"), true, LocalDate.of(2026, 6, 3), 60);

        producerService.publishTrainerWorkloadUpdateEvent(event);

        try (Consumer<String, String> consumer = createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
            ConsumerRecord<String, String> record = StreamSupport.stream(records.spliterator(), false)
                    .filter(r -> r.topic().equals(TOPIC))
                    .filter(r -> r.key().equals(event.username()))
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new AssertionError("No record found for key " + event.username()));

            Header traceHeader = record.headers().lastHeader(TRACE_ID_KEY);
            assertThat(traceHeader).isNotNull();
            assertThat(new String(traceHeader.value(), StandardCharsets.UTF_8)).isEqualTo(traceId);
            assertThat(record.topic()).isEqualTo(TOPIC);
            assertThat(OBJECT_MAPPER.readValue(record.value(), TrainerWorkloadUpdateEvent.class)).isEqualTo(event);
        }
    }

    private Consumer<String, String> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(embeddedKafkaBroker, "producer-it-group", false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
    }
}
