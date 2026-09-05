package org.example.workload.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClusterHealthIndicator implements HealthIndicator {

    private final AdminClient kafkaAdminClient;

    @Override
    public Health health() {
        try {
            DescribeClusterResult result = kafkaAdminClient.describeCluster();
            Collection<Node> nodes = result.nodes().get();
            String clusterId = result.clusterId().get();
            Node controller = result.controller().get();
            if (nodes.isEmpty()) {
                return Health.down()
                        .withDetail("error", "No Kafka brokers reachable")
                        .build();
            }
            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodes.size())
                    .withDetail("controllerId", controller != null ? controller.id() : "unknown")
                    .build();
        } catch (Exception e) {
            log.error("Kafka cluster health check failed", e);
            return Health.down(e)
                    .withDetail("error", "Failed to reach Kafka cluster")
                    .build();
        }
    }
}
