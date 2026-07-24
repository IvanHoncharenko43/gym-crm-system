package org.example.trainer.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.trainee.repository.TraineeEntity;
import org.example.core.repository.TrainingTypeEntity;
import org.example.user.repository.UserEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "trainers")
public class TrainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @ManyToMany(mappedBy = "trainers")
    private Set<TraineeEntity> trainees = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainerEntity that)) return false;
        return user != null && user.equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
