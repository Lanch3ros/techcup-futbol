package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SwaggerConfig – Bean customOpenAPI")
class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    @DisplayName("customOpenAPI – retorna una instancia no nula")
    void customOpenAPI_ReturnsNotNull() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertNotNull(api, "El bean OpenAPI no debe ser null");
    }

    @Test
    @DisplayName("customOpenAPI – título correcto: 'API de TechCup Fútbol'")
    void customOpenAPI_HasCorrectTitle() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertEquals("API de TechCup Fútbol", api.getInfo().getTitle());
    }

    @Test
    @DisplayName("customOpenAPI – versión '1.0'")
    void customOpenAPI_HasCorrectVersion() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertEquals("1.0", api.getInfo().getVersion());
    }

    @Test
    @DisplayName("customOpenAPI – descripción de la API no es nula ni vacía")
    void customOpenAPI_HasDescription() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertNotNull(api.getInfo().getDescription());
        assertFalse(api.getInfo().getDescription().isBlank());
    }

    @Test
    @DisplayName("customOpenAPI – contiene exactamente un SecurityRequirement")
    void customOpenAPI_HasOneSecurityRequirement() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertNotNull(api.getSecurity(), "La lista de security requirements no debe ser null");
        assertEquals(1, api.getSecurity().size(), "Debe haber exactamente 1 security requirement");
    }

    @Test
    @DisplayName("customOpenAPI – SecurityRequirement referencia al esquema 'bearerAuth'")
    void customOpenAPI_SecurityRequirementReferencesBearerAuth() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        SecurityRequirement secReq = api.getSecurity().get(0);
        assertTrue(secReq.containsKey("bearerAuth"),
                "El security requirement debe referenciar al esquema 'bearerAuth'");
    }

    @Test
    @DisplayName("customOpenAPI – Components contiene el SecurityScheme 'bearerAuth'")
    void customOpenAPI_ComponentsHasBearerAuthScheme() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        assertNotNull(api.getComponents(), "Components no debe ser null");
        assertNotNull(api.getComponents().getSecuritySchemes(), "SecuritySchemes no debe ser null");
        assertTrue(api.getComponents().getSecuritySchemes().containsKey("bearerAuth"),
                "Debe existir el esquema 'bearerAuth'");
    }

    @Test
    @DisplayName("customOpenAPI – SecurityScheme es de tipo HTTP")
    void customOpenAPI_SecuritySchemeIsHttp() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType(),
                "El tipo del esquema debe ser HTTP");
    }

    @Test
    @DisplayName("customOpenAPI – SecurityScheme usa scheme 'bearer'")
    void customOpenAPI_SecuritySchemeUsesBearerScheme() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals("bearer", scheme.getScheme(),
                "El scheme debe ser 'bearer'");
    }

    @Test
    @DisplayName("customOpenAPI – SecurityScheme tiene bearerFormat 'JWT'")
    void customOpenAPI_SecuritySchemeBearerFormatIsJwt() {
        OpenAPI api = swaggerConfig.customOpenAPI();
        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals("JWT", scheme.getBearerFormat(),
                "El bearerFormat debe ser 'JWT'");
    }
}
