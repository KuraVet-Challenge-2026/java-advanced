package br.com.fiap.kuravet.dto.consulta;

import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.model.Consulta;

import java.time.LocalDate;


public record ConsultaResponseDTO(
        Long idConsulta,
        Long idPet,
        String nomePet,
        Long idVeterinario,
        String nomeVeterinario,
        LocalDate dataSolicitacao,
        LocalDate dataConsulta,
        String tipoConsulta,
        String diagnostico,
        String motivoRecusa,
        StatusConsulta status
) {

    public static ConsultaResponseDTO fromEntity(Consulta consulta) {
        return new ConsultaResponseDTO(
                consulta.getIdConsulta(),
                consulta.getPet().getIdPet(),
                consulta.getPet().getNome(),
                consulta.getVeterinario().getIdVeterinario(),
                consulta.getVeterinario().getNome(),
                consulta.getDataSolicitacao(),
                consulta.getDataConsulta(),
                consulta.getTipoConsulta(),
                consulta.getDiagnostico(),
                consulta.getMotivoRecusa(),
                consulta.getStatus()
        );
    }
}