package org.example.workload.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.example.workload.controller.dto.FullName;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.repository.MonthWorkloadDocument;
import org.example.workload.repository.TrainerWorkloadDocument;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ConsumeTrainerWorkloadSteps {

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroup;
    private TopicPartition topicPartition;
    private TopicPartition dltPartition;

    @Value("${app.kafka.topics.trainer-workload-update}")
    private String topic;

    @Value("${app.kafka.topics.trainer-workload-update-dlt}")
    private String dltTopic;

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

    private TrainerWorkloadUpdateEvent updateEvent;
    private String invalidUpdateEvent;
    private Long offsetBeforeSendingEvent;
    private Long dltOffsetBeforeSendingEvent;
    private Consumer<String, String> dltConsumer;
    private String scenarioUsername;

    @PostConstruct
    void initTopics(){
        topicPartition = new TopicPartition(topic, 0);
        dltPartition = new TopicPartition(dltTopic, 0);
    }

    @Given("a trainer workload update event for base username {string}, year {int} and month {string} with duration {int} minutes")
    public void a_trainer_workload_update_event(String baseUsername, int year, String monthString, int durationMinutes){
        Month month = Month.valueOf(monthString.toUpperCase());
        if(scenarioUsername == null) {
            scenarioUsername = uniqueUsername(baseUsername);
        }
        updateEvent = new TrainerWorkloadUpdateEvent(scenarioUsername, new FullName("John", "Doe"),
                true, LocalDate.of(year, month.getValue(), 1), durationMinutes);
    }

    @Given("a workload data for trainer with base username {string} already exists for year {int} and month {string} with duration {int} minutes")
    public void a_trainer_workload_update_event_already_exists(String baseUsername, int year, String monthString, int durationMinutes) throws Exception {
        Month month = Month.valueOf(monthString.toUpperCase());
        scenarioUsername = uniqueUsername(baseUsername);
        TrainerWorkloadUpdateEvent event = new TrainerWorkloadUpdateEvent(scenarioUsername, new FullName("John", "Doe"),
                true, LocalDate.of(year, month.getValue(), 1), durationMinutes);

        long offsetBefore = committedOffset(topicPartition);
        kafkaTemplate.send(topic, scenarioUsername, event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(trainerWorkloadRepository.findByUsernameAndYear(scenarioUsername, year)).isPresent());
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(topicPartition) - offsetBefore).isEqualTo(1));
    }

    @Given("a trainer workload update event for base username {string} with invalid JSON format")
    public void a_trainer_workload_update_event_for_base_username_with_invalid_JSON_format(String baseUsername){
        scenarioUsername = uniqueUsername(baseUsername);
        invalidUpdateEvent = "invalid-json-for-the-event";
    }

    @When("a trainer workload update event is sent")
    public void a_trainer_workload_update_event_is_sent() throws Exception {
        offsetBeforeSendingEvent = committedOffset(topicPartition);
        if (invalidUpdateEvent != null) {
            dltConsumer = createDltConsumer();
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltConsumer, true, dltTopic);
            dltOffsetBeforeSendingEvent = dltConsumer.position(dltPartition);
            kafkaTemplate.send(topic, this.scenarioUsername, invalidUpdateEvent);
        } else {
            kafkaTemplate.send(topic, this.scenarioUsername, updateEvent);
        }
    }

    @Then("a new workload is created for given username")
    public void a_new_workload_is_created_for_given_username(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TrainerWorkloadDocument persisted = trainerWorkloadRepository.findByUsernameAndYear(scenarioUsername, updateEvent.trainingDate().getYear())
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            assertThat(persisted.getUsername()).isEqualTo(scenarioUsername);
            assertThat(persisted.getFirstName()).isEqualTo("John");
            assertThat(persisted.getLastName()).isEqualTo("Doe");
            assertThat(persisted.isStatus()).isTrue();
            MonthWorkloadDocument may = persisted.getMonths().stream()
                    .filter(m -> m.getMonth() == updateEvent.trainingDate().getMonth())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(updateEvent.trainingDate().getMonth() + " not created"));
            assertThat(may.getTrainingSummaryDurationMinutes()).isEqualTo(updateEvent.trainingSummaryDurationMinutes());
        });
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(topicPartition) - offsetBeforeSendingEvent).isEqualTo(1));
    }

    @Then("an existing workload for given username with month {string} and year {int} data is updated")
    public void an_existing_workload_for_given_username_with_month_and_year_data_is_updated(String existingMonthString, int year){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Month existingMonth = Month.valueOf(existingMonthString.toUpperCase());

            TrainerWorkloadDocument persisted = trainerWorkloadRepository.findByUsernameAndYear(scenarioUsername, year)
                    .orElseThrow(() -> new AssertionError("Trainer workload was not persisted"));
            assertThat(persisted.getMonths()).hasSize(2);
            assertThat(persisted.getMonths().stream().map(MonthWorkloadDocument::getMonth))
                    .containsExactlyInAnyOrder(existingMonth, updateEvent.trainingDate().getMonth());
            int juneDuration = persisted.getMonths().stream()
                    .filter(m -> m.getMonth() == updateEvent.trainingDate().getMonth())
                    .findFirst()
                    .orElseThrow()
                    .getTrainingSummaryDurationMinutes();
            assertThat(juneDuration).isEqualTo(updateEvent.trainingSummaryDurationMinutes());
        });
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(topicPartition) - offsetBeforeSendingEvent).isEqualTo(1));
    }

    @Then("the invalid message is routed to the trainer workload update Dead Letter Topic")
    public void the_invalid_message_is_routed_to_the_trainer_workload_update_dlt(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(committedOffset(topicPartition) - offsetBeforeSendingEvent).isEqualTo(1));
        try {
            ConsumerRecord<String, String> dltRecord =
                    KafkaTestUtils.getSingleRecord(dltConsumer, dltTopic, Duration.ofSeconds(10));
            Long dltOffsetAfterSendingEvent = dltConsumer.position(dltPartition);

            assertThat(dltOffsetAfterSendingEvent - dltOffsetBeforeSendingEvent).isEqualTo(1);
            assertThat(dltRecord).isNotNull();
            assertThat(dltRecord.key()).isEqualTo(scenarioUsername);
            assertThat(dltRecord.value()).isEqualTo(invalidUpdateEvent);
            Header originalTopicHeader = dltRecord.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC);
            assertThat(originalTopicHeader).isNotNull();
            assertThat(new String(originalTopicHeader.value(), StandardCharsets.UTF_8)).isEqualTo(topic);
        } finally {
            dltConsumer.close();
        }
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "." + System.nanoTime();
    }

    private long committedOffset(TopicPartition partition) throws Exception {
        OffsetAndMetadata offsetAndMetadata = kafkaAdminClient.listConsumerGroupOffsets(consumerGroup)
                .partitionsToOffsetAndMetadata()
                .get(10, TimeUnit.SECONDS)
                .get(partition);
        return offsetAndMetadata != null ? offsetAndMetadata.offset() : 0L;
    }

    private Consumer<String, String> createDltConsumer() {
        return consumerFactory.createConsumer();
    }
}
