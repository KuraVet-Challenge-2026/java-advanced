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

/**
 * Configuracao de seguranca hibrida do KuraVet.
 *
 * <p>Duas cadeias de filtros isoladas:
 * <ul>
 *     <li>{@code /api/**} - consumida pelo app mobile (React Native). Sem CSRF,
 *         sem sessao HTTP, autenticada via HTTP Basic contra a tabela USUARIO
 *         ({@code br.com.fiap.kuravet.security.UsuarioDetailsService}).</li>
 *     <li>demais rotas (portal web) - protegidas por formLogin() tradicional do
 *         Spring Security; {@code /portal/**} exige perfil VETERINARIO.</li>
 * </ul>
 *
 * <p>Aqui ficam apenas as regras que dependem de rota e metodo HTTP. A
 * autorizacao por dono (um TUTOR so ve e altera os proprios pets e consultas)
 * depende dos dados e por isso vive na camada de service.
 */
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
                // Sem isto, uma falha de autenticacao aqui pode ser resolvida pelo
                // entry point de formLogin() da cadeia do portal (redirect 302 para
                // /login) em vez do 401 padrao que o app mobile depende para
                // distinguir "login invalido" de qualquer outro erro.
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(apiEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/ping").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cadastro").permitAll()

                        // Fluxo de teleconsulta: quem pede e o tutor, quem decide e o veterinario
                        .requestMatchers(HttpMethod.POST, "/api/consultas/solicitacoes").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/aprovacao").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/recusa").hasRole("VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/diagnostico").hasRole("VETERINARIO")

                        // Cadastro, edicao e exclusao de pet sao exclusivos do tutor dono
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
                        // /error tambem precisa ficar liberado aqui: um 401/403 da
                        // cadeia da API vira sendError() -> forward interno do Tomcat
                        // para /error, que nao bate em /api/** e cai nesta cadeia.
                        // Sem isto, essa segunda passada exige login e mascara o
                        // status original com um redirect para /login.
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