package org.example.crm.trainer.client;

import org.example.crm.trainer.client.response.TrainerWorkloadClientResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/v1/trainers/workloads")
public interface TrainerWorkloadClient {

    @GetExchange
    TrainerWorkloadClientResponse getWorkload(@RequestParam String username, @RequestParam int year, @RequestParam int month);
}
