package org.example.config;

import org.example.trainee.controller.TraineeController;
import org.example.trainer.controller.TrainerController;
import org.example.training.controller.TrainingController;
import org.example.user.controller.UserController;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(UserController.class);
        register(TraineeController.class);
        register(TrainerController.class);
        register(TrainingController.class);
        register(AuthFilter.class);
        packages("org.example.exception.mapper");
        register(ValidationFeature.class);
    }
}
