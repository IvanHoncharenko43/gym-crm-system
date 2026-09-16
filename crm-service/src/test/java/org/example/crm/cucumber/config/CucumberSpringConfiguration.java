package org.example.crm.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.example.crm.config.SecurityConfig;
import org.example.crm.security.controller.AuthController;
import org.example.crm.security.service.AuthService;
import org.example.crm.security.service.JwtService;
import org.example.crm.security.service.OwnershipVerifier;
import org.example.crm.security.service.TokenBlackListService;
import org.example.crm.trainee.controller.TraineeController;
import org.example.crm.trainee.service.TraineeService;
import org.example.crm.trainer.controller.TrainerController;
import org.example.crm.trainer.service.TrainerService;
import org.example.crm.trainer.service.TrainerWorkloadService;
import org.example.crm.training.service.TrainingService;
import org.example.crm.user.controller.UserController;
import org.example.crm.user.service.UserService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@WebMvcTest(controllers = {UserController.class, TraineeController.class, TrainerController.class, AuthController.class})
@Import({SecurityConfig.class, TestContext.class})
public class CucumberSpringConfiguration {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TrainerWorkloadService trainerWorkloadService;

    @MockitoBean
    private OwnershipVerifier ownershipVerifier;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListService tokenBlackListService;
}
