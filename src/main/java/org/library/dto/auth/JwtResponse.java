package org.library.dto.auth;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private long userId;
    private String name;
    private String email;
    private String role;
}
