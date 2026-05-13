package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.domain.entity.CheckinHistorico;
import br.com.fiap.kuravet.domain.entity.Pet;
import br.com.fiap.kuravet.domain.repository.CheckinHistoricoRepository;
import br.com.fiap.kuravet.domain.repository.PetRepository;
import br.com.fiap.kuravet.dto.CheckinHistoricoRequestDTO;
import br.com.fiap.kuravet.dto.CheckinHistoricoResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckinHistoricoService {

    @Autowired
    private CheckinHistoricoRepository checkinRepository;

    @Autowired
    private PetRepository petRepository;

    public CheckinHistoricoResponseDTO salvar(CheckinHistoricoRequestDTO dto) {
        //O Pet tem que existir para fazer check-in
        Pet pet = petRepository.findById(dto.idPet())
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));

        CheckinHistorico checkin = new CheckinHistorico();
        checkin.setPet(pet);
        checkin.setObservacoes(dto.observacoes());


        CheckinHistorico salvo = checkinRepository.save(checkin);

        return new CheckinHistoricoResponseDTO(
                salvo.getId(),
                pet.getId(),
                salvo.getDataCheckin(),
                salvo.getObservacoes()
        );
    }

    public CheckinHistoricoResponseDTO atualizar(Long id, CheckinHistoricoRequestDTO dto) {
        CheckinHistorico checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Check-in não encontrado"));

        Pet pet = petRepository.findById(dto.idPet())
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));

        checkin.setPet(pet);
        checkin.setObservacoes(dto.observacoes());

        CheckinHistorico atualizado = checkinRepository.save(checkin);
        return new CheckinHistoricoResponseDTO(atualizado.getId(), pet.getId(), atualizado.getDataCheckin(), atualizado.getObservacoes());
    }

    public void excluir(Long id) {
        CheckinHistorico checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Check-in não encontrado"));
        checkinRepository.delete(checkin);
    }

    // Método para LISTAR todos os Check-ins
    public List<CheckinHistoricoResponseDTO> listarTodos() {
        return checkinRepository.findAll().stream()
                .map(c -> new CheckinHistoricoResponseDTO(
                        c.getId(),
                        c.getPet().getId(),
                        c.getDataCheckin(),
                        c.getObservacoes()
                ))
                .toList();
    }
}