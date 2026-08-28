package br.com.fiap.kuravet.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Repara o historico do Flyway antes de migrar. Registros de migrations que
 * falharam pela metade travam toda inicializacao seguinte ate serem removidos;
 * o repair() faz isso automaticamente, deixando a aplicacao subir mesmo num
 * schema que ficou sujo por uma tentativa anterior.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repararAntesDeMigrar() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}