package com.santy.finances.DTOs;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
