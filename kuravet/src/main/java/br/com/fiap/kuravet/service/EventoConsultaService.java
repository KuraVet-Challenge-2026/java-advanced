package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.domain.entity.EventoConsulta;
import br.com.fiap.kuravet.domain.entity.Pet;
import br.com.fiap.kuravet.domain.repository.EventoConsultaRepository;
import br.com.fiap.kuravet.domain.repository.PetRepository;
import br.com.fiap.kuravet.dto.EventoConsultaRequestDTO;
import br.com.fiap.kuravet.dto.EventoConsultaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoConsultaService {

    @Autowired
    private EventoConsultaRepository eventoRepository;

    @Autowired
    private PetRepository petRepository;

    public EventoConsultaResponseDTO salvar(EventoConsultaRequestDTO dto) {
        // Regra: O Pet tem que existir para a consulta
        Pet pet = petRepository.findById(dto.idPet())
                .orElseThrow(() -> new RuntimeException("Pet não encontrado para a consulta"));

        EventoConsulta evento = new EventoConsulta();
        evento.setPet(pet);
        evento.setDiagnostico(dto.diagnostico());
        evento.setTratamento(dto.tratamento());

        EventoConsulta salvo = eventoRepository.save(evento);

        return new EventoConsultaResponseDTO(
                salvo.getId(),
                pet.getId(),
                salvo.getDiagnostico(),
                salvo.getTratamento()
        );
    }

    public EventoConsultaResponseDTO atualizar(Long id, EventoConsultaRequestDTO dto) {
        EventoConsulta evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento de consulta não encontrado"));

        Pet pet = petRepository.findById(dto.idPet())
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));

        evento.setPet(pet);
        evento.setDiagnostico(dto.diagnostico());
        evento.setTratamento(dto.tratamento());

        EventoConsulta atualizado = eventoRepository.save(evento);
        return new EventoConsultaResponseDTO(atualizado.getId(), pet.getId(), atualizado.getDiagnostico(), atualizado.getTratamento());
    }

    public void excluir(Long id) {
        EventoConsulta evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento de consulta não encontrado"));
        eventoRepository.delete(evento);
    }

    public List<EventoConsultaResponseDTO> listarTodos() {
        return eventoRepository.findAll().stream()
                .map(e -> new EventoConsultaResponseDTO(
                        e.getId(),
                        e.getPet().getId(),
                        e.getDiagnostico(),
                        e.getTratamento()
                ))
                .toList();
    }

}