package com.example.config;

import com.example.core.service.CustomUserDetailsService;
import com.example.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationFilter – doFilterInternal")
class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    private UserDetails userDetails;
    private static final String VALID_JWT = "eyJhbGciOiJIUzI1NiJ9.valid.token";
    private static final String EMAIL     = "jose@mail.escuelaing.edu.co";

    @BeforeEach
    void setUp() {
        jwtService       = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter           = new JwtAuthenticationFilter(jwtService, userDetailsService);

        request    = new MockHttpServletRequest();
        response   = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);

        userDetails = User.withUsername(EMAIL)
                .password("$2a$10$hashed")
                .authorities(new SimpleGrantedAuthority("ROLE_JUGADOR"))
                .build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── Sin cabecera Authorization ────────────────────────────────────────────

    @Test
    @DisplayName("Sin cabecera Authorization → pasa al siguiente filtro sin autenticar")
    void noAuthHeader_PassesThrough() throws ServletException, IOException {
        // Sin header Authorization
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "No debe haber autenticación cuando no hay cabecera");
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Cabecera Authorization sin prefijo 'Bearer ' → pasa sin autenticar")
    void authHeaderWithoutBearerPrefix_PassesThrough() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    // ── Token malformado ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Token malformado → extractUsername lanza excepción → pasa sin autenticar")
    void malformedToken_ExtractUsernameThrows_PassesThrough() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer malformed.token.here");
        when(jwtService.extractUsername("malformed.token.here"))
                .thenThrow(new RuntimeException("JWT malformado"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Token con firma inválida → extractUsername lanza IllegalArgumentException → pasa sin autenticar")
    void invalidSignatureToken_PassesThrough() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer header.payload.invalidsig");
        when(jwtService.extractUsername("header.payload.invalidsig"))
                .thenThrow(new IllegalArgumentException("Firma inválida"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ── Username null ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername devuelve null → se omite el bloque de autenticación")
    void usernameNull_SkipsAuthBlock() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    // ── Token válido sin autenticación previa ─────────────────────────────────

    @Test
    @DisplayName("Token válido, sin auth previa → se establece autenticación en SecurityContext")
    void validToken_NoExistingAuth_SetsAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);
        // SecurityContextHolder ya fue limpiado en setUp (getAuthentication() == null)

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "Debe haber autenticación en el contexto tras un token válido");
        assertEquals(EMAIL,
                SecurityContextHolder.getContext().getAuthentication().getName(),
                "El principal debe ser el email del usuario");
    }

    @Test
    @DisplayName("Token válido → userDetailsService.loadUserByUsername es llamado con el email correcto")
    void validToken_LoadsUserByEmail() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername(EMAIL);
    }

    // ── Token válido pero isTokenValid retorna false ───────────────────────────

    @Test
    @DisplayName("Token extraído pero isTokenValid=false → autenticación NO establecida")
    void validExtraction_InvalidToken_NoAuthSet() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Token inválido no debe establecer autenticación");
    }

    // ── Autenticación previa en el contexto ───────────────────────────────────

    @Test
    @DisplayName("Token válido pero ya existe autenticación en contexto → NO re-autentica")
    void validToken_ExistingAuthentication_SkipsReauth() throws ServletException, IOException {
        // Pre-cargar autenticación existente
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(EMAIL);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // userDetailsService NO debe ser invocado si ya había auth
        verifyNoInteractions(userDetailsService);
        // La autenticación original sigue siendo la misma
        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    // ── Cadena de filtros siempre se llama ────────────────────────────────────

    @Test
    @DisplayName("filterChain.doFilter se llama siempre, independientemente del resultado")
    void filterChainAlwaysCalled_ForAllScenarios() throws ServletException, IOException {
        // Escenario: token válido y autenticación exitosa
        request.addHeader("Authorization", "Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
