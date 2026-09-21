package org.example.macrocycle.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.macrocycle.plan.JobStatus;
import org.example.macrocycle.plan.MacrocycleGoal;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.plan.TraineeContextSnapshot;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "macrocycle_jobs")
@CompoundIndex(name = "trainee_status_index", def = "{'traineeUsername': 1, 'status': 1}")
public class MacrocycleJobDocument {

    @Id
    private String id;

    private String traineeUsername;
    private String supervisingTrainerUsername;
    private MacrocycleGoal goal;
    private LocalDate targetEventDate;
    private String injuriesAndConstraints;
    private Set<DayOfWeek> weeklyAvailability = new HashSet<>();
    private int preferredSessionDurationMinutes;
    private TraineeContextSnapshot contextSnapshot;

    private JobStatus status;
    private int iterationCount;
    private MacrocyclePlan plan;
    private Set<String> warnings = new HashSet<>();
    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Indexed
    private LocalDateTime expiresAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MacrocycleJobDocument that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
