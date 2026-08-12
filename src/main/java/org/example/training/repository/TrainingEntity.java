package org.example.training.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.repository.TrainerEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "trainings")
public class TrainingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TraineeEntity trainee;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingEntity that)) return false;
        return trainingName != null && trainingName.equals(that.getTrainingName()) &&
                trainingDate != null && trainingDate.equals(that.getTrainingDate()) &&
                trainee != null && trainee.equals(that.getTrainee()) &&
                trainer != null && trainer.equals(that.getTrainer()) &&
                durationMinutes != null && durationMinutes.equals(that.getDurationMinutes()) &&
                trainingType != null && trainingType.equals(that.getTrainingType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingName, trainingDate, trainee, trainer, durationMinutes, trainingType);
    }
}
