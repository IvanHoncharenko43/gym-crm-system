package org.example.workload.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.common.GroupState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KafkaConsumerGroupHealthIndicator implements HealthIndicator {

    private final AdminClient kafkaAdminClient;
    private final String groupId;

    public KafkaConsumerGroupHealthIndicator(AdminClient kafkaAdminClient,
                                              @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.kafkaAdminClient = kafkaAdminClient;
        this.groupId = groupId;
    }

    @Override
    public Health health() {
        try {
            ConsumerGroupDescription description = kafkaAdminClient
                    .describeConsumerGroups(List.of(groupId))
                    .describedGroups().get(groupId).get();
            GroupState state = description.groupState();
            if (state != GroupState.STABLE) {
                return Health.down()
                        .withDetail("error", "Consumer group is not STABLE")
                        .withDetail("groupId", groupId)
                        .withDetail("state", state.toString())
                        .build();
            }
            return Health.up()
                    .withDetail("groupId", groupId)
                    .withDetail("state", state.toString())
                    .withDetail("memberCount", description.members().size())
                    .build();
        } catch (Exception e) {
            log.error("Kafka consumer group health check failed for group '{}'", groupId, e);
            return Health.down(e)
                    .withDetail("error", "Failed to describe consumer group")
                    .withDetail("groupId", groupId)
                    .build();
        }
    }
}
