package org.example.crm.trainer.client;

import jakarta.validation.Valid;
import org.example.crm.trainer.client.dto.TrainerWorkloadSummary;
import org.example.crm.trainer.client.dto.request.TrainerWorkloadQuery;
import org.example.crm.trainer.client.dto.request.TrainerWorkloadRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/trainers/workloads")
public interface TrainerWorkloadClient {

    @PostExchange
    void updateTrainerWorkload(@RequestBody TrainerWorkloadRequest request);

    @GetExchange
    TrainerWorkloadSummary getWorkload(@Valid @ParameterObject TrainerWorkloadQuery query);
}
