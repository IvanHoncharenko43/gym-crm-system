package org.example.trainer.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.trainer.controller.response.Trainers;
import org.example.trainer.service.TrainerService;
import org.example.training.controller.response.Trainings;
import org.example.training.service.TrainingService;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.controller.dto.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public TrainerController(TrainerService trainerService, TrainingService trainingService){
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile registerTrainer(@Valid @RequestBody CreateTrainerRequest request){
        log.info("POST /api/v1/trainers endpoint called");
        return trainerService.create(request);
    }

    @GetMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary getTrainer(@NotBlank(message = "Username cannot be blank")
                                         @RequestParam("username") String username,
                                     @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("GET /api/v1/trainers endpoint called");
        return trainerService.getByUsername(username, credentials);
    }

    @PutMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary updateTrainer(@NotBlank(message = "Username cannot be blank")
                                            @RequestParam("username") String username,
                                        @Valid @RequestBody UpdateTrainerRequest request,
                                        @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PUT /api/v1/trainers endpoint called");
        return trainerService.update(username, request, credentials);
    }

    @GetMapping(value = "/not-assigned", params = "trainee-username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers getNotAssignedOnTraineeActiveTrainersList(@NotBlank(message = "Trainee username cannot be blank")
                                                                  @RequestParam("trainee-username") String traineeUsername,
                                                              @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("GET /api/v1/trainers/not-assigned endpoint called");
        return trainerService.getUnassignedTrainersByTraineeList(traineeUsername, credentials);
    }

    @PatchMapping(value = "/activity", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void changeTrainerActivity(@NotBlank(message = "Username cannot be blank")
                                          @RequestParam("username") String username,
                                      @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PATCH /api/v1/trainers/activity endpoint called");
        trainerService.changeActivity(username, credentials);
    }

    @GetMapping(value = "/trainings")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTrainerTrainingList(
            @Valid GetTrainerTrainingsRequest request,
            @RequestAttribute("userCredentials") UserCredentials credentials
    ){
        log.info("GET /api/v1/trainers/trainings endpoint called");
        return trainingService.getTrainerTrainingList(request, credentials);
    }
}
