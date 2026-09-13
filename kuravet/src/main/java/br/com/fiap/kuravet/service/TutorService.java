package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.tutor.TutorRequestDTO;
import br.com.fiap.kuravet.exception.TutorNaoEncontradoException;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.repository.TutorRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    public List<Tutor> listar() {
        return tutorRepository.findAll();
    }

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
