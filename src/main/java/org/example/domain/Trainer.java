package org.example.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trainer extends User implements Identifiable{
    private TrainingType specialization;
}
