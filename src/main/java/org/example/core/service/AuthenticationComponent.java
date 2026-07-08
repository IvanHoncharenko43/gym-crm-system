package org.example.core.service;

import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.dto.Credentials;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationComponent {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public AuthenticationComponent(TraineeRepository traineeRepository, TrainerRepository trainerRepository){
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    public TraineeEntity authenticate(Credentials credentials){
//        TraineeEntity trainee = traineeRepository.findByUsername(username)
//                .orElseThrow(() -> new AuthenticationFailedException("Invalid username for authentication"));
//        if(!trainee.getPassword().equals(password)){
//            throw new AuthenticationFailedException("Invalid password for authentication");
//        }
//        return trainee;
    }
}
