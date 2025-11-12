package com.toronto.opendata.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    
    public JwtResponse(String token, String refreshToken, String username, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
    }
}
