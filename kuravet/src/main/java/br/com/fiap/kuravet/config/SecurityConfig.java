package br.com.fiap.kuravet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao de seguranca hibrida do KuraVet.
 *
 * <p>Duas cadeias de filtros isoladas:
 * <ul>
 *     <li>{@code /api/**}   - consumida pelo app mobile (React Native). Sem CSRF,
 *         sem sessao HTTP, autenticada via HTTP Basic contra a tabela USUARIO
 *         ({@code br.com.fiap.kuravet.security.UsuarioDetailsService}).</li>
 *     <li>demais rotas (portal web) - protegidas por formLogin() tradicional do
 *         Spring Security; {@code /portal/**} exige perfil VETERINARIO.</li>
 * </ul>
 *
 * <p>Autorizacao por dono (TUTOR so ve/altera os proprios pets e consultas) e
 * feita na camada de service, ja que depende de dados (nao so de rota/metodo).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/ping").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/diagnostico").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.POST, "/api/pets").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/pets/*").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/pets/*").hasRole("TUTOR")
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/portal/**").hasRole("VETERINARIO")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
