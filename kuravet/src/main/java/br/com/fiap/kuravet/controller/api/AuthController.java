package br.com.fiap.kuravet.controller.api;

import br.com.fiap.kuravet.dto.auth.CadastroRequestDTO;
import br.com.fiap.kuravet.dto.auth.CadastroResponseDTO;
import br.com.fiap.kuravet.dto.auth.MeResponseDTO;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import br.com.fiap.kuravet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Autenticacao e autocadastro consumidos pelo app mobile. {@code /cadastro}
 * e publico (ver {@code SecurityConfig}); {@code /me} exige HTTP Basic como
 * o resto de {@code /api/**}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<CadastroResponseDTO> cadastrar(@RequestBody @Valid CadastroRequestDTO dto) {
        CadastroResponseDTO resposta = authService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/api/tutores/" + resposta.idTutor()))
                .body(resposta);
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }
}
