package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.model.Pet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PetForm {

    private Long idPet;

    @NotNull(message = "Selecione o tutor responsavel.")
    private Long idTutor;

    @NotBlank(message = "O nome do pet e obrigatorio.")
    @Size(max = 60, message = "O nome deve ter no maximo 60 caracteres.")
    private String nome;

    @NotBlank(message = "A especie e obrigatoria.")
    @Size(max = 30, message = "A especie deve ter no maximo 30 caracteres.")
    private String especie;

    @Size(max = 60, message = "A raca deve ter no maximo 60 caracteres.")
    private String raca;

    @NotNull(message = "A data de nascimento e obrigatoria.")
    @Past(message = "A data de nascimento deve estar no passado.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "Informe o sexo do pet.")
    private Character sexo;

    public PetRequestDTO paraDTO() {
        return new PetRequestDTO(nome, especie, raca, dataNascimento, sexo);
    }

    public static PetForm de(Pet pet) {
        PetForm form = new PetForm();
        form.setIdPet(pet.getIdPet());
        form.setIdTutor(pet.getTutor().getIdTutor());
        form.setNome(pet.getNome());
        form.setEspecie(pet.getEspecie());
        form.setRaca(pet.getRaca());
        form.setDataNascimento(pet.getDataNascimento());
        form.setSexo(pet.getSexo());
        return form;
    }
}