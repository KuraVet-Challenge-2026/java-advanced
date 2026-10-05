package br.com.fiap.kuravet.dto.pet;

import br.com.fiap.kuravet.model.Pet;

import java.time.LocalDate;

public record PetResponseDTO(
        Long idPet,
        String nome,
        String especie,
        String raca,
        LocalDate dataNascimento,
        Character sexo,
        Long idTutor,
        String nomeTutor,
        String codigoIdentificacao
) {

    public static PetResponseDTO fromEntity(Pet pet) {
        return new PetResponseDTO(
                pet.getIdPet(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento(),
                pet.getSexo(),
                pet.getTutor().getIdTutor(),
                pet.getTutor().getNome(),
                pet.getCodigoIdentificacao()
        );
    }
}
