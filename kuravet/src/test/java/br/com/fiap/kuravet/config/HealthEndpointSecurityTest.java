package br.com.fiap.kuravet.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida so as regras do SecurityConfig para o actuator, sem Oracle e sem contexto completo.
 * Os endpoints reais do actuator nao existem em @WebMvcTest; controllers falsos nos mesmos
 * caminhos bastam para exercitar a cadeia de seguranca.
 */
@WebMvcTest(HealthEndpointSecurityTest.EndpointsFalsos.class)
@Import({SecurityConfig.class, HealthEndpointSecurityTest.EndpointsFalsos.class})
class HealthEndpointSecurityTest {

    @RestController
    static class EndpointsFalsos {

        @GetMapping("/actuator/health")
        String health() {
            return "{\"status\":\"UP\"}";
        }

        @GetMapping("/actuator/health/db")
        String healthDb() {
            return "{\"status\":\"UP\"}";
        }

        @GetMapping("/actuator/env")
        String env() {
            return "{}";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthDeveSerPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":\"UP\"}"));
    }

    @Test
    void subCaminhosDoHealthDevemSerPublicos() throws Exception {
        mockMvc.perform(get("/actuator/health/db"))
                .andExpect(status().isOk());
    }

    @Test
    void outrosEndpointsDoActuatorDevemExigirLogin() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
