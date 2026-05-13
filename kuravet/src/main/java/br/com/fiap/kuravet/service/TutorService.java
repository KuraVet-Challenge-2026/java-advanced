package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.domain.entity.Tutor;
import br.com.fiap.kuravet.domain.repository.TutorRepository;
import br.com.fiap.kuravet.dto.TutorRequestDTO;
import br.com.fiap.kuravet.dto.TutorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TutorService {

    @Autowired
    private TutorRepository tutorRepository;

    public TutorResponseDTO salvar(TutorRequestDTO dto) {
        Tutor tutor = new Tutor();
        tutor.setNome(dto.nome());
        tutor.setCpf(dto.cpf());
        tutor.setEmail(dto.email());
        tutor.setTelefone(dto.telefone());

        Tutor tutorSalvo = tutorRepository.save(tutor);

        return new TutorResponseDTO(
                tutorSalvo.getId(),
                tutorSalvo.getNome(),
                tutorSalvo.getEmail(),
                tutorSalvo.getTelefone(),
                tutorSalvo.getDataCadastro()
        );
    }

    // Metodo para LISTAR todos os Tutores
    public List<TutorResponseDTO> listarTodos() {
        return tutorRepository.findAll().stream()
                .map(t -> new TutorResponseDTO(
                        t.getId(),
                        t.getNome(),
                        t.getEmail(),
                        t.getTelefone(),
                        t.getDataCadastro()
                ))
                .toList();
    }

    public TutorResponseDTO atualizar(Long id, TutorRequestDTO dto) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado para atualização"));

        tutor.setNome(dto.nome());
        tutor.setCpf(dto.cpf());
        tutor.setEmail(dto.email());
        tutor.setTelefone(dto.telefone());

        Tutor atualizado = tutorRepository.save(tutor);
        return new TutorResponseDTO(atualizado.getId(), atualizado.getNome(), atualizado.getEmail(), atualizado.getTelefone(), atualizado.getDataCadastro());
    }

    public void excluir(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado para exclusão"));
        tutorRepository.delete(tutor);
    }

}