package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.domain.entity.CheckinHistorico;
import br.com.fiap.kuravet.domain.entity.Pet;
import br.com.fiap.kuravet.domain.repository.CheckinHistoricoRepository;
import br.com.fiap.kuravet.domain.repository.PetRepository;
import br.com.fiap.kuravet.dto.CheckinHistoricoRequestDTO;
import br.com.fiap.kuravet.dto.CheckinHistoricoResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckinHistoricoService {

    @Autowired
    private CheckinHistoricoRepository checkinRepository;

    @Autowired
    private PetRepository petRepository;

    public CheckinHistoricoResponseDTO salvar(CheckinHistoricoRequestDTO dto) {
        // Regra: O Pet tem que existir para fazer check-in
        Pet pet = petRepository.findById(dto.idPet())
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));

        CheckinHistorico checkin = new CheckinHistorico();
        checkin.setPet(pet);
        checkin.setObservacoes(dto.observacoes());
        // A data já é setada automaticamente na Entidade

        CheckinHistorico salvo = checkinRepository.save(checkin);

        // Agora a ordem bate exatamente com o ResponseDTO (ID, ID do Pet, Data, Observacoes)
        return new CheckinHistoricoResponseDTO(
                salvo.getId(),
                pet.getId(),
                salvo.getDataCheckin(),
                salvo.getObservacoes()
        );
    }
}