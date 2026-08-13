package br.com.fiap.kuravet.dto;

import br.com.fiap.kuravet.model.Pet;

import java.time.LocalDate;

/**
 * Payload de saida com os dados do PET. Achata a referencia ao TUTOR em
 * dois campos simples (idTutor/nomeTutor) para evitar LazyInitializationException
 * e recursao bidirecional na serializacao JSON.
 */
public record PetResponseDTO(
        Long idPet,
        String nome,
        String especie,
        String raca,
        LocalDate dataNascimento,
        Character sexo,
        Long idTutor,
        String nomeTutor
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
                pet.getTutor().getNome()
        );
    }
}
