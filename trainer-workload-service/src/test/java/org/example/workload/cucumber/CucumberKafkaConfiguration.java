package org.example.workload.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;

@CucumberContextConfiguration
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "${app.kafka.topics.trainer-workload-update}",
        "${app.kafka.topics.trainer-workload-update-dlt}"
})
@ActiveProfiles("kafka-it")
public class CucumberKafkaConfiguration {

    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.3.8").withReuse(true);

    static { MONGO.start(); }
}

