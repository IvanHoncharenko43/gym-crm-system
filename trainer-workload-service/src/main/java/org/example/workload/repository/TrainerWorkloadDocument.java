package org.example.workload.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "trainer_year_workloads")
@CompoundIndex(name = "username_year_index", def = "{'username': 1, 'year': 1}", unique = true)
public class TrainerWorkloadDocument {
    @Id
    private String id;

    private String username;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;
    private boolean status;
    private int year;

    private Set<MonthWorkload> months = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainerWorkloadDocument that)) return false;
        return username != null && username.equals(that.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
