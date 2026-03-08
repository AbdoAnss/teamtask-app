package com.teamflow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email
    private String email;
    private String firstName;
    private String lastName;
    @Size(min = 8, max = 100)
    private String password;
}
