package org.example.training;

import jakarta.validation.Valid;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.dto.request.TrainingTypes;
import org.example.training.service.TrainingService;
import org.example.training.service.TrainingTypeService;
import org.example.user.dto.UserCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

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
        trainingService.create(request, credentials);
    }

    @GetMapping("/training-types")
    @ResponseStatus(HttpStatus.OK)
    public TrainingTypes getTrainingTypes(){
        return trainingTypeService.getAllTrainingTypes();
    }
}
