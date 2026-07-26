package org.example.user.controller;

import jakarta.validation.Valid;
import org.example.core.dto.ChangePasswordRequest;
import org.example.user.dto.UserCredentials;
import org.example.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
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
        userService.changePassword(request, credentials);
    }
}
