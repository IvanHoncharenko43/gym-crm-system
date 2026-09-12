package org.example.workload.messaging;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.example.workload.controller.dto.FullName;
import org.example.workload.repository.MonthWorkload;
import org.example.workload.repository.TrainerWorkloadDocument;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.workload.TestUtils.getTrainerWorkloadRequest;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        TrainerWorkloadConsumerIT.TOPIC,
        TrainerWorkloadConsumerIT.DLT_TOPIC
})
@ActiveProfiles("kafka-it")
class TrainerWorkloadConsumerIT {

    static final String TOPIC = "trainer-workload-update-event";
    static final String DLT_TOPIC = "trainer-workload-update-event-dlt";
    private static final String CONSUMER_GROUP = "workload-consumer-it-group";
    private static final TopicPartition TOPIC_PARTITION = new TopicPartition(TOPIC, 0);
    private static final TopicPartition DLT_PARTITION = new TopicPartition(DLT_TOPIC, 0);

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private AdminClient kafkaAdminClient;

    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.3.8").withReuse(true);

    static { MONGO.start(); }

    @Test
    void consumeTrainerWorkloadUpdate_CreateNewTrainerWorkload_NewTrainer() throws Exception {
        String username = uniqueUsername("consumer.create");
        int yearOfTheWorkload = 2026;
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(
                username, new FullName("John", "Doe"), true, LocalDate.of(yearOfTheWorkload, Month.MAY, 12), 90);

        long offsetBefore = committedOffset(TOPIC_PARTITION);
        kafkaTemplate.send(TOPIC, event.username(), event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadDocument persisted = trainerWorkloadRepository.findByUsernameAndYear(username, event.trainingDate().getYear())
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            assertThat(persisted.getUsername()).isEqualTo(username);
            assertThat(persisted.getFirstName()).isEqualTo("John");
            assertThat(persisted.getLastName()).isEqualTo("Doe");
            assertThat(persisted.isStatus()).isTrue();
            MonthWorkload may = persisted.getMonths().stream()
                    .filter(m -> m.getMonth() == event.trainingDate().getMonth())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(event.trainingDate().getMonth() + " not created"));
            assertThat(may.getTrainingSummaryDurationMinutes()).isEqualTo(90);
        });
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(TOPIC_PARTITION) - offsetBefore).isEqualTo(1));
    }

    @Test
    void consumeTrainerWorkloadUpdate_AddNewMonthToExistingYear_SecondEventDifferentMonth() throws Exception {
        String username = uniqueUsername("consumer.addmonth");
        int yearOfTheWorkloads = 2026;
        TrainerWorkloadUpdateEvent mayEvent = getTrainerWorkloadRequest(username, LocalDate.of(yearOfTheWorkloads, Month.MAY, 12), 90);
        TrainerWorkloadUpdateEvent juneEvent = getTrainerWorkloadRequest(username, LocalDate.of(yearOfTheWorkloads, Month.JUNE, 3), 60);

        long offsetBeforeMay = committedOffset(TOPIC_PARTITION);
        kafkaTemplate.send(TOPIC, mayEvent.username(), mayEvent);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(trainerWorkloadRepository.findByUsernameAndYear(username, yearOfTheWorkloads)).isPresent());
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(TOPIC_PARTITION) - offsetBeforeMay).isEqualTo(1));

        long offsetBeforeJune = committedOffset(TOPIC_PARTITION);
        kafkaTemplate.send(TOPIC, juneEvent.username(), juneEvent);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadDocument persisted = trainerWorkloadRepository.findByUsernameAndYear(username, yearOfTheWorkloads)
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            assertThat(persisted.getMonths()).hasSize(2);
            assertThat(persisted.getMonths().stream().map(MonthWorkload::getMonth))
                    .containsExactlyInAnyOrder(Month.MAY, Month.JUNE);
            int juneDuration = persisted.getMonths().stream()
                    .filter(m -> m.getMonth() == Month.JUNE)
                    .findFirst()
                    .orElseThrow()
                    .getTrainingSummaryDurationMinutes();
            assertThat(juneDuration).isEqualTo(60);
        });
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(TOPIC_PARTITION) - offsetBeforeJune).isEqualTo(1));
    }

    @Test
    void consumeTrainerWorkloadUpdate_RoutesToDlt_OnUndeserializableMessage() {
        String username = uniqueUsername("consumer.dlt");
        String invalidMessage = "invalid-json-for-the-event";

        try (Consumer<String, String> dltConsumer = createDltConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltConsumer, true, DLT_TOPIC);
            long offsetBefore = dltConsumer.position(DLT_PARTITION);

            kafkaTemplate.send(TOPIC, username, invalidMessage);
            ConsumerRecord<String, String> dltRecord =
                    KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(10));
            long offsetAfter = dltConsumer.position(DLT_PARTITION);

            assertThat(offsetAfter - offsetBefore).isEqualTo(1);
            assertThat(dltRecord).isNotNull();
            assertThat(dltRecord.key()).isEqualTo(username);
            assertThat(dltRecord.value()).isEqualTo(invalidMessage);
            Header originalTopicHeader = dltRecord.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC);
            assertThat(originalTopicHeader).isNotNull();
            assertThat(new String(originalTopicHeader.value(), StandardCharsets.UTF_8)).isEqualTo(TOPIC);
        }
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    private long committedOffset(TopicPartition partition) throws Exception {
        OffsetAndMetadata offsetAndMetadata = kafkaAdminClient.listConsumerGroupOffsets(CONSUMER_GROUP)
                .partitionsToOffsetAndMetadata()
                .get(10, TimeUnit.SECONDS)
                .get(partition);
        return offsetAndMetadata != null ? offsetAndMetadata.offset() : 0L;
    }

    private Consumer<String, String> createDltConsumer() {
        return consumerFactory.createConsumer();
    }
}
