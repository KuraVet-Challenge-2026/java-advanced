package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.tutor.TutorRequestDTO;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.repository.TutorRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negocio do TUTOR.
 *
 * <p>Metodos sem {@link UsuarioPrincipal} sao de uso interno/portal (o
 * VETERINARIO gerencia todos os tutores por ali). Os que recebem o
 * principal sao os expostos pela API mobile e aplicam o mesmo padrao de
 * dono usado em {@link PetService} e {@link ConsultaService}.
 */
@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    public List<Tutor> listar() {
        return tutorRepository.findAll();
    }

    /** API mobile: TUTOR ve somente o proprio cadastro; VETERINARIO ve todos. */
    public List<Tutor> listar(UsuarioPrincipal principal) {
        if (principal.isTutor()) {
            return tutorRepository.findById(principal.getIdTutor())
                    .map(List::of)
                    .orElse(List.of());
        }
        return listar();
    }

    public Tutor buscarPorId(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new TutorNaoEncontradoException(id));
    }

    /**
     * Um tutor de outro dono "nao existe" para o TUTOR autenticado (404 em
     * vez de 403), para nao revelar a existencia de registros de terceiros.
     * VETERINARIO acessa qualquer tutor.
     */
    public Tutor buscarPorId(UsuarioPrincipal principal, Long id) {
        Tutor tutor = buscarPorId(id);

        if (principal.isTutor() && !tutor.getIdTutor().equals(principal.getIdTutor())) {
            throw new TutorNaoEncontradoException(id);
        }

        return tutor;
    }

    @Transactional
    public Tutor criar(TutorRequestDTO dto) {
        Tutor tutor = Tutor.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .email(dto.email())
                .endereco(dto.endereco())
                .build();

        return tutorRepository.save(tutor);
    }

    @Transactional
    public Tutor atualizar(UsuarioPrincipal principal, Long id, TutorRequestDTO dto) {
        Tutor tutor = buscarPorId(principal, id);

        tutor.setNome(dto.nome());
        tutor.setCpf(dto.cpf());
        tutor.setTelefone(dto.telefone());
        tutor.setEmail(dto.email());
        tutor.setEndereco(dto.endereco());

        return tutorRepository.save(tutor);
    }

    @Transactional
    public void excluir(UsuarioPrincipal principal, Long id) {
        tutorRepository.delete(buscarPorId(principal, id));
    }
}
