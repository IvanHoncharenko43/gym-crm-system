package org.example.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;

    @JsonProperty("isActive")
    private boolean isActive;
}
