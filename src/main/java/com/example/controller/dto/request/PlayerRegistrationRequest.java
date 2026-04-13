package com.example.controller.dto.request;

import com.example.core.model.Program;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlayerRegistrationRequest {

    /** Nombre completo. Alternativa: {@link #firstName} + {@link #lastName}. */
    private String name;

    private String firstName;
    private String lastName;

    @NotBlank(message = "Se necesita número de identificación")
    private String identification;

    private Program program;

    @Email(message = "Formato invalido de correo")
    @NotBlank(message = "Se necesita correo")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Se necesita rol de usuario")
    private String userType;


    @Min(value = 1, message = "El número del dorsal debe ser mínimo 1")
    @Max(value = 99, message = "El número del dorsal debe ser igual o menor a 99")
    private Integer jerseyNumber;

    @Pattern(regexp = "(?i)^(Portero|Defensa|Volante|Delantero)$", message = "Posición invalida")
    private String position;

    private LocalDate birthDate;

    @Min(value = 1, message = "La edad debe ser positiva")
    @Max(value = 120, message = "La edad no es válida")
    private Integer age;

    private String gender;

    private Integer semester;

    private String skillLevel;


    private String securityRole;

    @AssertTrue(message = "Ingresar name o bien firstName y lastName")
    public boolean isNameProvided() {
        if (name != null && !name.isBlank()) {
            return true;
        }
        return firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank();
    }
}
