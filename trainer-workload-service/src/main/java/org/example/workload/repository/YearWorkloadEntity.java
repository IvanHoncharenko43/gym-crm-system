package org.example.workload.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class YearWorkloadEntity {
    private int year;
    List<MonthWorkloadEntity> months = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YearWorkloadEntity that)) return false;
        return year == that.getYear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(year);
    }
}
