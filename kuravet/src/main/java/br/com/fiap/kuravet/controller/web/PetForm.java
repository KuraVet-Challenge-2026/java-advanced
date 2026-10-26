package br.com.fiap.kuravet.controller.web;

import br.com.fiap.kuravet.dto.pet.PetRequestDTO;
import br.com.fiap.kuravet.model.Pet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PetForm {

    private Long idPet;

    @NotNull(message = "Selecione o tutor responsável.")
    private Long idTutor;

    @NotBlank(message = "O nome do pet é obrigatório.")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
    private String nome;

    @NotBlank(message = "A espécie é obrigatória.")
    @Size(max = 30, message = "A espécie deve ter no máximo 30 caracteres.")
    private String especie;

    @Size(max = 60, message = "A raça deve ter no máximo 60 caracteres.")
    private String raca;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve estar no passado.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "Informe o sexo do pet.")
    private Character sexo;

    @Size(max = 40, message = "O código de identificação deve ter no máximo 40 caracteres.")
    @Pattern(regexp = "^\\s*[A-Za-z0-9._-]*\\s*$", message = "O código de identificação deve conter apenas letras, números, ponto, hífen ou underscore.")
    private String codigoIdentificacao;

    public PetRequestDTO paraDTO() {
        return new PetRequestDTO(nome, especie, raca, dataNascimento, sexo, codigoIdentificacao);
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
        form.setCodigoIdentificacao(pet.getCodigoIdentificacao());
        return form;
    }
}