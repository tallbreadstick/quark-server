package com.darauy.quark.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
