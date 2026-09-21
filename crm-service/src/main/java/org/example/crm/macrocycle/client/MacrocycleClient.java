package org.example.crm.macrocycle.client;

import org.example.crm.macrocycle.client.request.GenerateMacrocycleJobRequest;
import org.example.crm.macrocycle.client.response.MacrocycleJobAcceptedResponse;
import org.example.crm.macrocycle.client.response.MacrocycleJobStatusClientResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/macrocycles")
public interface MacrocycleClient {

    @PostExchange
    MacrocycleJobAcceptedResponse generate(@RequestBody GenerateMacrocycleJobRequest request);

    @GetExchange("/{jobId}")
    MacrocycleJobStatusClientResponse getStatus(@PathVariable String jobId);

    @DeleteExchange("/{jobId}")
    void discard(@PathVariable String jobId);
}
