package org.example.workload.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class MonthWorkloadEntity {
    private Month month;
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
