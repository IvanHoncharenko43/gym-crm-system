package org.example.shared;

import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {

    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    public String generate(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String finalUsername = baseUsername;
        int serialNumber = 1;
        while (usernameExists(finalUsername)) {
            finalUsername = baseUsername + serialNumber;
            serialNumber++;
        }
        return finalUsername;
    }

    private boolean usernameExists(String username) {
        boolean existsInTrainers = trainerRepository.findByUsername(username).isPresent();
        if (existsInTrainers) {
            return true;
        }
        return traineeRepository.findByUsername(username).isPresent();
    }
}
