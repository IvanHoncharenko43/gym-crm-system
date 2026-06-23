package org.example.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trainer extends User {
    private TrainingType specialization;
    private Long userId;
}
