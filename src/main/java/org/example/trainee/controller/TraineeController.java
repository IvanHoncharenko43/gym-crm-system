package org.example.trainee.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.controller.response.Trainers;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public TraineeController(TraineeService traineeService, TrainingService trainingService){
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile registerTrainee(@Valid @RequestBody CreateTraineeRequest request){
        log.info("POST /api/v1/trainees endpoint called with request");
        return traineeService.create(request);
    }

    @GetMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary getTrainee(@NotBlank(message = "Username cannot be blank")
                                         @RequestParam("username") String username,
                                     @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("GET /api/v1/trainees endpoint called");
        return traineeService.getByUsername(username, credentials);
    }

    @PutMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary updateTrainee(@NotBlank(message = "Username cannot be blank")
                                            @RequestParam("username") String username,
                                        @Valid @RequestBody UpdateTraineeRequest request,
                                        @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PUT /api/v1/trainees endpoint called with request");
        return traineeService.update(username, request, credentials);
    }

    @DeleteMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTrainee(@NotBlank(message = "Username cannot be blank")
                                  @RequestParam("username") String username,
                              @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("DELETE /api/v1/trainees endpoint called");
        traineeService.deleteByUsername(username, credentials);
    }

    @PutMapping(value = "/trainers", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers updateTrainersList(@NotBlank(message = "Username cannot be blank")
                                           @RequestParam("username") String username,
                                       @Valid @RequestBody UpdateTraineeTrainersRequest request,
                                       @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PUT /api/v1/trainees/trainers endpoint called with request");
        return traineeService.updateTrainersList(username, request, credentials);
    }

    @PatchMapping(value = "/activity", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void changeTraineeActivity(@NotBlank(message = "Username cannot be blank")
                                          @RequestParam("username") String username,
                                      @RequestAttribute("userCredentials") UserCredentials credentials){
        log.info("PATCH /api/v1/trainees/activity endpoint called");
        traineeService.changeActivity(username, credentials);
    }

    @GetMapping("/trainings")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTraineeTrainingList(
            @Valid GetTraineeTrainingsRequest request,
            @RequestAttribute("userCredentials") UserCredentials credentials
    ){
        log.info("GET /api/v1/trainees/trainings endpoint called with request params");
        return trainingService.getTraineeTrainingList(request, credentials);
    }
}
