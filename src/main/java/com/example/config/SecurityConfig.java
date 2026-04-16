package com.example.config;

import com.example.core.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // -----------------------------------------------------------------------
    // Reglas de acceso por endpoint
    // -----------------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf.disable()) // NOSONAR — API stateless con JWT; CSRF no aplica
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Swagger UI — acceso público
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // Login y OAuth2 Google — público
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/google").permitAll()

                // Registro de jugadores — público (cualquiera puede registrarse)
                .requestMatchers(HttpMethod.POST, "/api/v1/players/register").permitAll()

                // Consulta de torneos y partidos — público (lectura libre para espectadores)
                .requestMatchers(HttpMethod.GET, "/api/v1/tournaments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/matches/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/stats/**").permitAll()

                // ── ORGANIZADOR ──────────────────────────────────────────────
                // Gestión de torneos
                .requestMatchers(HttpMethod.POST,  "/api/v1/tournaments").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/tournaments/*/start").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/tournaments/*/finish").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tournaments/*/status").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/tournaments/*/generate-matches").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/tournaments/*/generate-quarter-finals").hasAnyRole("ORGANIZADOR", "ADMIN")
                // Revisión de pagos
                .requestMatchers(HttpMethod.PATCH, "/api/v1/payments/*/approve").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/payments/*/reject").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/referees").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/matches/*/referee").hasAnyRole("ORGANIZADOR", "ADMIN")

                // ── ÁRBITRO ──────────────────────────────────────────────────
                .requestMatchers(HttpMethod.PATCH, "/api/v1/matches/*/status").hasAnyRole("ARBITRO", "ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/matches/*/result").hasAnyRole("ARBITRO", "ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST,  "/api/v1/matches/*/events").hasAnyRole("ARBITRO", "ORGANIZADOR", "ADMIN")

                // ── CAPITÁN ──────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/v1/tournaments/*/teams/*").hasAnyRole("CAPITAN", "ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/payments/upload").hasAnyRole("CAPITAN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/teams").hasAnyRole("CAPITAN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/teams/*/lineup").hasAnyRole("CAPITAN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/teams/*/invitations").hasAnyRole("CAPITAN", "ADMIN")

                // ── Cualquier usuario autenticado ─────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
