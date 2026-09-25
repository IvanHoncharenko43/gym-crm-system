package org.example.macrocycle.service;

import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.config.MacrocycleGenerationProperties;
import org.example.macrocycle.controller.request.GenerateMacrocycleJobRequest;
import org.example.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(MacrocycleGenerationProperties.class)
public class MacrocycleOrchestrator {

    private final MacrocycleJobService macrocycleJobService;

    public MacrocycleOrchestrator(MacrocycleJobService macrocycleJobService){
        this.macrocycleJobService = macrocycleJobService;
    }

    public MacrocycleJobStatusResponse startProcessing(GenerateMacrocycleJobRequest request) {
        MacrocycleJobStatusResponse response = macrocycleJobService.create(request);
        macrocycleJobService.runAsync(response.jobId());
        return response;
    }
}
