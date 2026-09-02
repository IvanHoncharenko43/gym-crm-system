package org.example.workload.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "month_workloads")
public class MonthWorkloadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workload_month")
    private Month month;

    @Column(name = "training_summary_duration_minutes")
    private int trainingSummaryDurationMinutes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonthWorkloadEntity that)) return false;
        return month != null && month.equals(that.getMonth());
    }

    @Override
    public int hashCode() {
        return Objects.hash(month);
    }
}
