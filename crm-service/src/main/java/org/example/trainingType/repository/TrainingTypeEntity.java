package org.example.trainingType.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.trainingType.dto.TrainingType;

import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "training_types")
public class TrainingTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private TrainingType trainingTypeName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingTypeEntity that)) return false;
        return trainingTypeName != null && trainingTypeName == that.getTrainingTypeName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingTypeName);
    }
}
