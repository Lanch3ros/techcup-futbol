package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("TechcupFutbolApplication – smoke tests")
class TechcupFutbolApplicationTests {

    @Test
    @DisplayName("contextLoads – el contexto de Spring arranca sin errores")
    void contextLoads() {
        // El hecho de que @SpringBootTest levante el contexto sin excepción
        // ya constituye la validación de este test.
    }

    @Test
    @DisplayName("main() – invoca SpringApplication.run con la clase correcta")
    void main_InvokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            mockedStatic.when(() -> SpringApplication.run(TechcupFutbolApplication.class, new String[]{}))
                        .thenReturn(null);

            TechcupFutbolApplication.main(new String[]{});

            mockedStatic.verify(() -> SpringApplication.run(TechcupFutbolApplication.class, new String[]{}));
        }
    }
}
