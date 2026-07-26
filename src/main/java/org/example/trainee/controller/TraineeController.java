package org.example.trainee.controller;

import jakarta.validation.Valid;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainee.dto.UpdateTraineeTrainersRequest;
import org.example.trainee.dto.GetTraineeTrainingsRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.dto.response.Trainers;
import org.example.training.dto.request.Trainings;
import org.example.training.service.TrainingService;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.UserProfile;
import org.springframework.http.HttpStatus;
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
        return traineeService.create(request);
    }

    @GetMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary getTrainee(@RequestParam("username") String username,
                                     @RequestAttribute("userCredentials") UserCredentials credentials){
        return traineeService.getByUsername(username, credentials);
    }

    @PutMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TraineeSummary updateTrainee(@RequestParam("username") String username,
                                        @Valid @RequestBody UpdateTraineeRequest request,
                                        @RequestAttribute("userCredentials") UserCredentials credentials){
        return traineeService.update(username, request, credentials);
    }

    @DeleteMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTrainee(@RequestParam("username") String username,
                              @RequestAttribute("userCredentials") UserCredentials credentials){
        traineeService.deleteByUsername(username, credentials);
    }

    @PutMapping(value = "/trainers", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers updateTrainersList(@RequestParam("username") String username,
                                       @Valid @RequestBody UpdateTraineeTrainersRequest request,
                                       @RequestAttribute("userCredentials") UserCredentials credentials){
        return traineeService.updateTrainersList(username, request, credentials);
    }

    @PatchMapping(value = "/activity", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void changeTraineeActivity(@RequestParam("username") String username,
                                      @RequestAttribute("userCredentials") UserCredentials credentials){
        traineeService.changeActivity(username, credentials);
    }

    @GetMapping("/trainings")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTraineeTrainingList(
            @Valid GetTraineeTrainingsRequest request,
            @RequestAttribute("userCredentials") UserCredentials credentials
    ){
        return trainingService.getTraineeTrainingList(request, credentials);
    }
}
