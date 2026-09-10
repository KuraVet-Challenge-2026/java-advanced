package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.auth.CadastroRequestDTO;
import br.com.fiap.kuravet.dto.auth.CadastroResponseDTO;
import br.com.fiap.kuravet.dto.auth.MeResponseDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.repository.TutorRepository;
import br.com.fiap.kuravet.repository.UsuarioRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticacao e autocadastro consumidos pelo app mobile.
 *
 * <p>O autocadastro publico ({@link #cadastrar}) sempre cria um USUARIO de
 * perfil TUTOR: o papel nunca vem do cliente, e definido aqui no servidor.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final TutorService tutorService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       TutorRepository tutorRepository,
                       TutorService tutorService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tutorRepository = tutorRepository;
        this.tutorService = tutorService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria o TUTOR e o USUARIO correspondente em uma unica transacao. Antes
     * de gravar, confere username e CPF unicos para responder 400 com uma
     * mensagem clara em vez de deixar a constraint do banco estourar um 500.
     */
    @Transactional
    public CadastroResponseDTO cadastrar(CadastroRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new RegraDeNegocioException("Nome de usuario ja em uso.");
        }
        if (tutorRepository.existsByCpf(dto.cpf())) {
            throw new RegraDeNegocioException("CPF ja cadastrado.");
        }

        Tutor tutor = tutorService.criar(dto.paraTutorDTO());

        Usuario usuario = Usuario.builder()
                .username(dto.username())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(Perfil.TUTOR)
                .tutor(tutor)
                .build();

        return CadastroResponseDTO.de(usuarioRepository.save(usuario));
    }

    /** Ponto unico para o app validar login e obter o perfil autenticado. */
    public MeResponseDTO me(UsuarioPrincipal principal) {
        String nomeTutor = principal.isTutor()
                ? tutorService.buscarPorId(principal.getIdTutor()).getNome()
                : null;

        return new MeResponseDTO(
                principal.getIdUsuario(),
                principal.getUsername(),
                principal.getPerfil(),
                principal.getIdTutor(),
                nomeTutor
        );
    }
}
