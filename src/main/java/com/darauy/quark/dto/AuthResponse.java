package com.darauy.quark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Integer id;
    private String username;
    private String email;
    private String userType;
    private String message;
    private String token;
    private Long expiration;
}
