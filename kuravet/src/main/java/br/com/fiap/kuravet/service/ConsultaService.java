package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.StatusConsulta;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Regras de negocio ligadas ao ciclo de vida da CONSULTA.
 */
@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    /**
     * Fluxo funcional "Realizacao de Consulta e Emissao de Diagnostico".
     *
     * <p>Regras aplicadas:
     * <ul>
     *     <li>a consulta informada precisa existir;</li>
     *     <li>somente consultas com status {@code AGENDADA} podem ser realizadas
     *         (espelha a CHECK KV_CK_CONS_STATUS e o fluxo real da clinica: uma
     *         consulta ja REALIZADA ou CANCELADA nao pode ser reaberta por aqui);</li>
     *     <li>o diagnostico e obrigatorio para encerrar o atendimento.</li>
     * </ul>
     *
     * @param idConsulta  ID da consulta a ser realizada
     * @param diagnostico texto do diagnostico emitido pelo veterinario
     * @return a consulta atualizada e persistida com status REALIZADA
     */
    @Transactional
    public Consulta realizarConsultaComDiagnostico(Long idConsulta, String diagnostico) {
        if (!StringUtils.hasText(diagnostico)) {
            throw new RegraDeNegocioException("O diagnostico e obrigatorio para realizar a consulta.");
        }

        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(idConsulta));

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraDeNegocioException(
                    "Somente consultas com status AGENDADA podem ser realizadas. Status atual: " + consulta.getStatus());
        }

        consulta.setStatus(StatusConsulta.REALIZADA);
        consulta.setDiagnostico(diagnostico);

        return consultaRepository.save(consulta);
    }
}
