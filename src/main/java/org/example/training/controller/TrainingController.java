package org.example.training.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.trainingType.dto.TrainingTypes;
import org.example.training.service.TrainingService;
import org.example.trainingType.service.TrainingTypeService;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

@Slf4j
@RestController
@RequestMapping("/api/v1/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService){
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void addTraining(@Valid @RequestBody CreateTrainingRequest request,
                            @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("POST /api/v1/trainings endpoint called");
        trainingService.create(request, credentials);
    }

    @GetMapping("/training-types")
    @ResponseStatus(HttpStatus.OK)
    public TrainingTypes getTrainingTypes(){
        log.info("GET /api/v1/trainings/training-types endpoint called");
        return trainingTypeService.getAllTrainingTypes();
    }
}
