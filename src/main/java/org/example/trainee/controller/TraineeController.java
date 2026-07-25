package org.example.trainee.controller;

import jakarta.validation.Valid;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainee.dto.UpdateTraineeTrainersRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainer.dto.response.TrainersSummaries;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainees")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService){
        this.traineeService = traineeService;
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

    @PutMapping("/{id}/trainers")
    @ResponseStatus(HttpStatus.OK)
    public TrainersSummaries updateTrainersList(@PathVariable("id") Long id,
                                                @Valid @RequestBody UpdateTraineeTrainersRequest request,
                                                @RequestAttribute("userCredentials") UserCredentials credentials){
        return traineeService.updateTrainersList(id, request, credentials);
    }

    @PatchMapping("/{id}/activity")
    @ResponseStatus(HttpStatus.OK)
    public void changeTraineeActivity(@PathVariable("id") Long id,
                                      @RequestAttribute("userCredentials") UserCredentials credentials){
        traineeService.changeActivity(id, credentials);
    }
}
