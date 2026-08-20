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
public class TrainerWorkloadEntity {
    private String username;
    private String firstName;
    private String lastName;
    private boolean status;
    private List<YearWorkloadEntity> years = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainerWorkloadEntity that)) return false;
        return username != null && username.equals(that.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
