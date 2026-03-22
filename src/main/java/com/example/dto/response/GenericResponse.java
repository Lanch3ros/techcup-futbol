package com.example.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class GenericResponse {
    private String message;
    private Object data;

}
