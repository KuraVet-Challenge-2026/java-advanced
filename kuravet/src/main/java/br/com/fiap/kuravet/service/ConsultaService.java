package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.VeterinarioRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de negocio ligadas ao ciclo de vida da CONSULTA.
 */
@Service
public class ConsultaService {

    /**
     * Unica fonte de verdade das transicoes de status permitidas. REALIZADA
     * e CANCELADA nao aparecem como chave porque sao estados terminais.
     */
    private static final Map<StatusConsulta, Set<StatusConsulta>> TRANSICOES_PERMITIDAS = Map.of(
            StatusConsulta.AGENDADA, Set.of(StatusConsulta.REALIZADA, StatusConsulta.CANCELADA)
    );

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsultaService(ConsultaRepository consultaRepository, PetRepository petRepository,
                            VeterinarioRepository veterinarioRepository) {
        this.consultaRepository = consultaRepository;
        this.petRepository = petRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Consulta> listar(UsuarioPrincipal principal) {
        if (principal.isTutor()) {
            return consultaRepository.findByPetTutorIdTutor(principal.getIdTutor());
        }
        return consultaRepository.findAll();
    }

    /**
     * Uma consulta de outro tutor "nao existe" para o TUTOR autenticado (404
     * em vez de 403), para nao revelar a existencia de registros de terceiros.
     */
    public Consulta buscarPorId(UsuarioPrincipal principal, Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(id));

        if (principal.isTutor() && !consulta.getPet().getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new ConsultaNaoEncontradaException(id);
        }

        return consulta;
    }

    @Transactional
    public Consulta criar(UsuarioPrincipal principal, ConsultaRequestDTO dto) {
        Pet pet = buscarPetOuLancar(principal, dto.idPet());
        Veterinario veterinario = buscarVeterinarioOuLancar(dto.idVeterinario());

        Consulta consulta = Consulta.builder()
                .pet(pet)
                .veterinario(veterinario)
                .dataConsulta(dto.dataConsulta())
                .tipoConsulta(dto.tipoConsulta())
                .status(StatusConsulta.AGENDADA)
                .build();

        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta atualizar(UsuarioPrincipal principal, Long id, ConsultaRequestDTO dto) {
        Consulta consulta = buscarPorId(principal, id);
        Pet pet = buscarPetOuLancar(principal, dto.idPet());
        Veterinario veterinario = buscarVeterinarioOuLancar(dto.idVeterinario());

        consulta.setPet(pet);
        consulta.setVeterinario(veterinario);
        consulta.setDataConsulta(dto.dataConsulta());
        consulta.setTipoConsulta(dto.tipoConsulta());

        return consultaRepository.save(consulta);
    }

    @Transactional
    public void excluir(UsuarioPrincipal principal, Long id) {
        Consulta consulta = buscarPorId(principal, id);
        consultaRepository.delete(consulta);
    }

    /**
     * Fluxo funcional "Realizacao de Consulta e Emissao de Diagnostico".
     * Restrito a VETERINARIO (ver {@code SecurityConfig}).
     *
     * <p>Regras aplicadas:
     * <ul>
     *     <li>a consulta informada precisa existir;</li>
     *     <li>a transicao de status segue {@link #TRANSICOES_PERMITIDAS} (espelha a
     *         CHECK KV_CK_CONS_STATUS e o fluxo real da clinica: uma consulta ja
     *         REALIZADA ou CANCELADA nao pode ser reaberta por aqui);</li>
     *     <li>o diagnostico e obrigatorio para encerrar o atendimento.</li>
     * </ul>
     */
    @Transactional
    public Consulta realizarConsultaComDiagnostico(Long idConsulta, String diagnostico) {
        if (!StringUtils.hasText(diagnostico)) {
            throw new RegraDeNegocioException("O diagnostico e obrigatorio para realizar a consulta.");
        }

        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(idConsulta));

        transicionar(consulta, StatusConsulta.REALIZADA);
        consulta.setDiagnostico(diagnostico);

        return consultaRepository.save(consulta);
    }

    /**
     * Unico ponto do sistema que altera {@code Consulta.status}. Nenhum
     * controller ou outro metodo deste service pode chamar
     * {@code consulta.setStatus(...)} diretamente.
     */
    private void transicionar(Consulta consulta, StatusConsulta novoStatus) {
        Set<StatusConsulta> permitidas = TRANSICOES_PERMITIDAS.getOrDefault(consulta.getStatus(), Set.of());

        if (!permitidas.contains(novoStatus)) {
            throw new RegraDeNegocioException(
                    "Transicao de status invalida: " + consulta.getStatus() + " -> " + novoStatus);
        }

        consulta.setStatus(novoStatus);
    }

    private Pet buscarPetOuLancar(UsuarioPrincipal principal, Long idPet) {
        Pet pet = petRepository.findById(idPet)
                .orElseThrow(() -> new PetNaoEncontradoException(idPet));

        if (principal.isTutor() && !pet.getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new PetNaoEncontradoException(idPet);
        }

        return pet;
    }

    private Veterinario buscarVeterinarioOuLancar(Long idVeterinario) {
        return veterinarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Veterinario nao encontrado para o ID: " + idVeterinario));
    }
}
