package com.example.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Verifica un Google ID Token contra el endpoint público de Google.
 * Retorna los claims del token (email, name, sub) si es válido.
 */
@Slf4j
@Service
public class GoogleTokenVerifierService {

    private static final String TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private RestTemplate restTemplate;

    public GoogleTokenVerifierService() {
        this.restTemplate = new RestTemplate();
    }

    // Constructor para inyección en tests
    GoogleTokenVerifierService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Verifica el token con Google y retorna los claims.
     *
     * @param idToken ID Token recibido del frontend tras el login con Google
     * @return mapa con claims (email, name, sub, email_verified, ...)
     * @throws IllegalArgumentException si el token es inválido o expirado
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> verify(String idToken) {
        log.debug("Verificando ID Token de Google con tokeninfo endpoint");
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    TOKENINFO_URL + idToken, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Google tokeninfo retornó respuesta inválida: {}", response.getStatusCode());
                throw new IllegalArgumentException("Token de Google inválido o expirado.");
            }

            Map<String, String> claims = (Map<String, String>) response.getBody();
            log.debug("Token de Google verificado correctamente - email: {}", claims.get("email"));
            return claims;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error al verificar token de Google: {}", e.getMessage());
            throw new IllegalArgumentException("No se pudo verificar el token de Google: " + e.getMessage());
        }
    }
}
