package org.example.workload.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "year_workloads")
public class YearWorkloadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workload_year")
    private int year;

    @OneToMany(mappedBy = "yearWorkload", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MonthWorkloadEntity> months = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "trainer_workload_id", nullable = false)
    private TrainerWorkloadEntity trainerWorkload;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YearWorkloadEntity that)) return false;
        return year == that.getYear()
                && trainerWorkload != null && trainerWorkload.equals(that.getTrainerWorkload());
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, trainerWorkload);
    }
}
