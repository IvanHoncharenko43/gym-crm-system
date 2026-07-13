package org.example.trainer.repository;

import jakarta.persistence.*;
import lombok.*;
import org.example.core.repository.Identifiable;
import org.example.trainee.repository.TraineeEntity;
import org.example.training.repository.TrainingTypeEntity;
import org.example.user.repository.UserEntity;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
@Entity
@Table(name = "trainers")
public class TrainerEntity implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "trainers")
    private Set<TraineeEntity> trainees = new HashSet<>();

//    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
//    @ToString.Exclude
//    private Set<Training> trainings = new HashSet<>();
}
