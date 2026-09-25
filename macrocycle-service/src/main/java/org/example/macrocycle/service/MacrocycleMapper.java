package org.example.macrocycle.service;

import org.example.macrocycle.controller.request.GenerateMacrocycleJobRequest;
import org.example.macrocycle.controller.response.MacrocycleJobStatusResponse;
import org.example.macrocycle.plan.JobStatus;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.springframework.stereotype.Service;

@Service
public class MacrocycleMapper {

    public MacrocycleJobDocument toMacrocycleJobDocument(GenerateMacrocycleJobRequest request){
        MacrocycleJobDocument macrocycleJobDocument = new MacrocycleJobDocument();
        macrocycleJobDocument.setTraineeUsername(request.traineeUsername());
        macrocycleJobDocument.setSupervisingTrainerUsername(request.supervisingTrainerUsername());
        macrocycleJobDocument.setGoal(request.goal());
        macrocycleJobDocument.setTargetEventDate(request.targetEventDate());
        macrocycleJobDocument.setInjuriesAndConstraints(request.injuriesAndConstraints());
        macrocycleJobDocument.setWeeklyAvailability(request.weeklyAvailability());
        macrocycleJobDocument.setPreferredSessionDurationMinutes(request.preferredSessionDurationMinutes());
        macrocycleJobDocument.setContextSnapshot(request.contextSnapshot());
        macrocycleJobDocument.setStatus(JobStatus.PENDING);
        macrocycleJobDocument.setIterationCount(0);
        return macrocycleJobDocument;
    }

    public MacrocycleJobStatusResponse toMacrocycleJobStatusResponse(MacrocycleJobDocument document){
        return new MacrocycleJobStatusResponse(
                document.getId(),
                document.getTraineeUsername(),
                document.getSupervisingTrainerUsername(),
                document.getStatus(),
                document.getPlan(),
                document.getWarnings(),
                document.getFailureReason()
        );
    }
}
