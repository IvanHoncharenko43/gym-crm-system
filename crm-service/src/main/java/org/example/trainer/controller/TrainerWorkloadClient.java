package org.example.trainer.controller;

import org.example.trainer.controller.request.TrainerWorkloadRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/trainers/workloads")
public interface TrainerWorkloadClient {

    @PostExchange
    void updateTrainerWorkload(@RequestBody TrainerWorkloadRequest request);
}
