package org.example.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.ChangePasswordRequest;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestAttribute("userCredentials") UserCredentials credentials
            ){
        log.info("PUT /api/v1/users/change-password endpoint called");
        userService.changePassword(request, credentials);
    }
}
