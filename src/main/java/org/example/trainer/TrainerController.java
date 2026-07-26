package org.example.trainer;

import jakarta.validation.Valid;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.dto.GetTrainerTrainingsRequest;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.dto.response.Trainers;
import org.example.trainer.service.TrainerService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;

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
        return trainerService.create(request);
    }

    @GetMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary getTrainer(@RequestParam("username") String username,
                                     @RequestAttribute("userCredentials") UserCredentials credentials){
        return trainerService.getByUsername(username, credentials);
    }

    @PutMapping(params = "username")
    @ResponseStatus(HttpStatus.OK)
    public TrainerSummary updateTrainer(@RequestParam("username") String username,
                                        @Valid @RequestBody UpdateTrainerRequest request,
                                        @RequestAttribute("userCredentials") UserCredentials credentials){
        return trainerService.update(username, request, credentials);
    }

    @GetMapping(value = "/not-assigned", params = "trainee-username")
    @ResponseStatus(HttpStatus.OK)
    public Trainers getNotAssignedOnTraineeActiveTrainersList(@RequestParam("trainee-username") String traineeUsername,
                                                              @RequestAttribute("userCredentials") UserCredentials credentials){
        return trainerService.getUnassignedTrainersByTraineeList(traineeUsername, credentials);
    }

    @PatchMapping(value = "/activity", params = "username")
    @ResponseStatus(HttpStatus.OK)
    public void changeTrainerActivity(@RequestParam("username") String username,
                                      @RequestAttribute("userCredentials") UserCredentials credentials){
        trainerService.changeActivity(username, credentials);
    }

    @GetMapping(value = "/trainings")
    @ResponseStatus(HttpStatus.OK)
    public Trainings getTrainerTrainingList(
            @Valid GetTrainerTrainingsRequest request,
            @RequestAttribute("userCredentials") UserCredentials credentials
    ){
        return trainingService.getTrainerTrainingList(request, credentials);
    }
}
