package br.com.fiap.kuravet.dto.consulta;

import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.enums.StatusConsulta;

import java.time.LocalDate;

/**
 * Payload de saida com os dados da CONSULTA. Achata as referencias a PET
 * e VETERINARIO em campos simples para evitar LazyInitializationException
 * e recursao bidirecional na serializacao JSON.
 */
public record ConsultaResponseDTO(
        Long idConsulta,
        Long idPet,
        String nomePet,
        Long idVeterinario,
        String nomeVeterinario,
        LocalDate dataConsulta,
        String tipoConsulta,
        String diagnostico,
        StatusConsulta status
) {

    public static ConsultaResponseDTO fromEntity(Consulta consulta) {
        return new ConsultaResponseDTO(
                consulta.getIdConsulta(),
                consulta.getPet().getIdPet(),
                consulta.getPet().getNome(),
                consulta.getVeterinario().getIdVeterinario(),
                consulta.getVeterinario().getNome(),
                consulta.getDataConsulta(),
                consulta.getTipoConsulta(),
                consulta.getDiagnostico(),
                consulta.getStatus()
        );
    }
}
