package com.example.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {
    protected Long id;
    protected String identification;
    protected String email;
    protected String password;
    protected String fullName;
    protected String role;
    protected String profilePhoto;

    public abstract boolean login();
    public abstract void logout();
}
