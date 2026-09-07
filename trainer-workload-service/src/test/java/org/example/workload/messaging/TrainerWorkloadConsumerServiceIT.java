package org.example.workload.messaging;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.workload.config.KafkaConfig;
import org.example.workload.controller.dto.FullName;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.example.workload.repository.YearWorkloadEntity;
import org.example.workload.service.TrainerWorkloadService;
import org.example.workload.service.WorkloadMapper;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.workload.TestUtils.getTrainerWorkloadRequest;

@SpringBootTest(classes = {
        TrainerWorkloadConsumerService.class,
        TrainerWorkloadService.class,
        WorkloadMapper.class,
        KafkaConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration({
        KafkaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
})
@EntityScan(basePackages = "org.example.workload.repository")
@EnableJpaRepositories(basePackages = "org.example.workload.repository")
@EmbeddedKafka(partitions = 1, topics = {
        TrainerWorkloadConsumerServiceIT.TOPIC,
        TrainerWorkloadConsumerServiceIT.DLT_TOPIC
})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class TrainerWorkloadConsumerServiceIT {

    static final String TOPIC = "trainer-workload-update-event";
    static final String DLT_TOPIC = "trainer-workload-update-event-dlt";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private KafkaTemplate<Object, Object> eventKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> rawKafkaTemplate;

    @TestConfiguration
    static class RawProducerTestConfig {
        @Bean
        KafkaTemplate<String, String> rawKafkaTemplate(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:workload-consumer-it;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Test
    void consumeTrainerWorkloadUpdate_CreateNewTrainerWorkload_NewTrainer() {
        String username = uniqueUsername("consumer.create");
        int yearOfTheWorkload = 2026;
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                username, new FullName("John", "Doe"), true, LocalDate.of(yearOfTheWorkload, Month.MAY, 12), 90);

        eventKafkaTemplate.send(TOPIC, event.username(), event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadEntity persisted = trainerWorkloadRepository.findByUsername(username)
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            assertThat(persisted.getUsername()).isEqualTo(username);
            assertThat(persisted.getFirstName()).isEqualTo("John");
            assertThat(persisted.getLastName()).isEqualTo("Doe");
            assertThat(persisted.isStatus()).isTrue();
            YearWorkloadEntity year2026 = persisted.getYears().stream()
                    .filter(y -> y.getYear() == yearOfTheWorkload)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(String.format("Year %s not found", yearOfTheWorkload)));
            MonthWorkloadEntity may = year2026.getMonths().stream()
                    .filter(m -> m.getMonth() == Month.MAY)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("May not created"));
            assertThat(may.getTrainingSummaryDurationMinutes()).isEqualTo(90);
        });
    }

    @Test
    void consumeTrainerWorkloadUpdate_AddNewMonthToExistingYear_SecondEventDifferentMonth() {
        String username = uniqueUsername("consumer.addmonth");
        int yearOfTheWorkloads = 2026;
        TrainerWorkloadUpdateEvent mayEvent = getTrainerWorkloadRequest(username, LocalDate.of(yearOfTheWorkloads, Month.MAY, 12), 90);
        TrainerWorkloadUpdateEvent juneEvent = getTrainerWorkloadRequest(username, LocalDate.of(yearOfTheWorkloads, Month.JUNE, 3), 60);

        eventKafkaTemplate.send(TOPIC, mayEvent.username(), mayEvent);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(trainerWorkloadRepository.findByUsername(username)).isPresent());

        eventKafkaTemplate.send(TOPIC, juneEvent.username(), juneEvent);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadEntity persisted = trainerWorkloadRepository.findByUsername(username)
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            YearWorkloadEntity year2026 = persisted.getYears().stream()
                    .filter(y -> y.getYear() == yearOfTheWorkloads)
                    .findFirst()
                    .orElseThrow(() -> new AssertionFailure(String.format("Year %s not found", yearOfTheWorkloads)));
            assertThat(year2026.getMonths()).hasSize(2);
            assertThat(year2026.getMonths().stream().map(MonthWorkloadEntity::getMonth))
                    .containsExactlyInAnyOrder(Month.MAY, Month.JUNE);
            int juneDuration = year2026.getMonths().stream()
                    .filter(m -> m.getMonth() == Month.JUNE)
                    .findFirst()
                    .orElseThrow()
                    .getTrainingSummaryDurationMinutes();
            assertThat(juneDuration).isEqualTo(60);
        });
    }

    @Test
    void consumeTrainerWorkloadUpdate_RoutesToDlt_OnUndeserializableMessage() {
        String username = uniqueUsername("consumer.dlt");
        String invalidMessage = "invalid-json-for-the-event";
        String expectedJson = "\"" + invalidMessage + "\"";
        rawKafkaTemplate.send(TOPIC, username, invalidMessage);

        try (Consumer<String, String> dltConsumer = createDltConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltConsumer, DLT_TOPIC);
            ConsumerRecord<String, String> dltRecord =
                    KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(10));

            assertThat(dltRecord).isNotNull();
            assertThat(dltRecord.key()).isEqualTo(username);
            assertThat(dltRecord.value()).isEqualTo(expectedJson);
            Header originalTopicHeader = dltRecord.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC);
            assertThat(originalTopicHeader).isNotNull();
            assertThat(new String(originalTopicHeader.value(), StandardCharsets.UTF_8)).isEqualTo(TOPIC);
        }
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    private Consumer<String, String> createDltConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                embeddedKafkaBroker, "dlt-test-group", false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> factory =
                new DefaultKafkaConsumerFactory<>(consumerProps);
        return factory.createConsumer();
    }
}
