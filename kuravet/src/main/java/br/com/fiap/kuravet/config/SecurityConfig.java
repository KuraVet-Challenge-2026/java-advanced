package br.com.fiap.kuravet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        BasicAuthenticationEntryPoint apiEntryPoint = new BasicAuthenticationEntryPoint();
        apiEntryPoint.setRealmName("Realm");

        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(apiEntryPoint))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(apiEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/ping").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cadastro").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/consultas/solicitacoes").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/aprovacao").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/recusa").hasRole("VETERINARIO")
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
                        .requestMatchers("/login", "/error", "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/portal/**").hasRole("VETERINARIO")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/portal/painel", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}