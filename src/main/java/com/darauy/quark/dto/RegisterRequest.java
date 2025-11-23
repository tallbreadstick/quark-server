package com.darauy.quark.dto;

import com.darauy.quark.entity.users.User;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "Invalid username")
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotNull
    private User.UserType userType;
}
