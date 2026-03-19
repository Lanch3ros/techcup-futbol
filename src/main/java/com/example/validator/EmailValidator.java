package com.example.validator;

public interface EmailValidator {
    boolean validate(String email);
    String getDomain();
}