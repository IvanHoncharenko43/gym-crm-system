package org.example.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Trainee extends User{
    private Long id;
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;
}
