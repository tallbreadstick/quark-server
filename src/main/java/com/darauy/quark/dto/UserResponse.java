package com.darauy.quark.dto;

import com.darauy.quark.entity.users.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private User.UserType userType;
}
