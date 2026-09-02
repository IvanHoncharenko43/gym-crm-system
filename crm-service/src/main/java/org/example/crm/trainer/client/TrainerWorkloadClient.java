package org.example.crm.trainer.client;

import jakarta.validation.Valid;
import org.example.crm.trainer.client.request.TrainerMonthlyWorkloadClientRequest;
import org.example.crm.trainer.client.request.TrainerUpdateWorkloadClientRequest;
import org.example.crm.trainer.client.response.TrainerWorkloadClientResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/trainers/workloads")
public interface TrainerWorkloadClient {

    @PostExchange
    void updateTrainerWorkload(@RequestBody TrainerUpdateWorkloadClientRequest request);

    @GetExchange
    TrainerWorkloadClientResponse getWorkload(@Valid @ParameterObject TrainerMonthlyWorkloadClientRequest query);
}
