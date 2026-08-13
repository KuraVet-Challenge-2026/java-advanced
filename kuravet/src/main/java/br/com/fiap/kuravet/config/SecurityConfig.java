package br.com.fiap.kuravet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao de seguranca hibrida do KuraVet.
 *
 * <p>Duas cadeias de filtros isoladas:
 * <ul>
 *     <li>{@code /api/**}   - consumida pelo app mobile (React Native). Sem CSRF,
 *         sem sessao HTTP e sem redirecionamento para tela de login.</li>
 *     <li>demais rotas (ex: {@code /painel/**}) - portal web, protegido por
 *         formLogin() tradicional do Spring Security.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Cadeia de seguranca da API mobile: totalmente desprotegida de CSRF,
     * stateless (sem JSESSIONID) e sem exigir autenticacao nesta Sprint.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * Cadeia de seguranca do portal web: exige autenticacao via formLogin()
     * padrao, com CSRF habilitado (comportamento padrao do Spring Security).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/painel/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Usuarios em memoria apenas para fins de desenvolvimento desta Sprint 3.
     * Deve ser substituido por uma origem persistente (banco) futuramente.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var veterinario = User.withUsername("veterinario")
                .password(passwordEncoder.encode("vet123"))
                .roles("VETERINARIO")
                .build();

        var tutor = User.withUsername("tutor")
                .password(passwordEncoder.encode("tutor123"))
                .roles("TUTOR")
                .build();

        return new InMemoryUserDetailsManager(veterinario, tutor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
