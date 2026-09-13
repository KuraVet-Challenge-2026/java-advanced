package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.VeterinarioRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsultaService {

    private static final Map<StatusConsulta, Set<StatusConsulta>> TRANSICOES_PERMITIDAS = Map.of(
            StatusConsulta.SOLICITADA, Set.of(StatusConsulta.AGENDADA, StatusConsulta.RECUSADA),
            StatusConsulta.AGENDADA, Set.of(StatusConsulta.REALIZADA, StatusConsulta.CANCELADA)
    );

    private static final Set<StatusConsulta> ESTADOS_ATIVOS =
            Set.of(StatusConsulta.SOLICITADA, StatusConsulta.AGENDADA);

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           PetRepository petRepository,
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

    public List<Consulta> listarPorStatus(UsuarioPrincipal principal, StatusConsulta status) {
        if (principal.isTutor()) {
            return consultaRepository.findByPetTutorIdTutorAndStatusOrderByDataConsultaAsc(
                    principal.getIdTutor(), status);
        }
        return consultaRepository.findByStatusOrderByDataConsultaAsc(status);
    }

    public long contarPorStatus(StatusConsulta status) {
        return consultaRepository.countByStatus(status);
    }

    public Consulta buscarPorId(UsuarioPrincipal principal, Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(id));

        if (principal.isTutor() && !consulta.getPet().getTutor().getIdTutor().equals(principal.getIdTutor())) {
            throw new ConsultaNaoEncontradaException(id);
        }

        return consulta;
    }

    @Transactional
    public Consulta solicitarTeleconsulta(UsuarioPrincipal principal, ConsultaRequestDTO dto) {
        Pet pet = buscarPetOuLancar(principal, dto.idPet());
        Veterinario veterinario = buscarVeterinarioOuLancar(dto.idVeterinario());

        if (consultaRepository.existsByPetIdPetAndDataConsultaAndStatusIn(
                pet.getIdPet(), dto.dataConsulta(), ESTADOS_ATIVOS)) {
            throw new RegraDeNegocioException(
                    "O pet " + pet.getNome() + " ja possui uma consulta ativa em " + dto.dataConsulta() + ".");
        }

        Consulta solicitacao = Consulta.builder()
                .pet(pet)
                .veterinario(veterinario)
                .dataConsulta(dto.dataConsulta())
                .tipoConsulta(dto.tipoConsulta())
                .dataSolicitacao(LocalDate.now())
                .status(StatusConsulta.SOLICITADA)
                .build();

        return consultaRepository.save(solicitacao);
    }

    @Transactional
    public Consulta aprovarSolicitacao(Long idConsulta) {
        Consulta consulta = buscarOuLancar(idConsulta);
        transicionar(consulta, StatusConsulta.AGENDADA);
        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta recusarSolicitacao(Long idConsulta, String motivo) {
        Consulta consulta = buscarOuLancar(idConsulta);
        transicionar(consulta, StatusConsulta.RECUSADA);
        consulta.setMotivoRecusa(motivo);
        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta realizarConsultaComDiagnostico(Long idConsulta, String diagnostico) {
        Consulta consulta = buscarOuLancar(idConsulta);
        transicionar(consulta, StatusConsulta.REALIZADA);
        consulta.setDiagnostico(diagnostico);
        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta cancelar(UsuarioPrincipal principal, Long idConsulta) {
        Consulta consulta = buscarPorId(principal, idConsulta);
        transicionar(consulta, StatusConsulta.CANCELADA);
        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta atualizar(UsuarioPrincipal principal, Long id, ConsultaRequestDTO dto) {
        Consulta consulta = buscarPorId(principal, id);

        if (consulta.getStatus() != StatusConsulta.SOLICITADA
                && consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraDeNegocioException(
                    "Consulta com status " + consulta.getStatus() + " nao pode mais ser alterada.");
        }

        consulta.setPet(buscarPetOuLancar(principal, dto.idPet()));
        consulta.setVeterinario(buscarVeterinarioOuLancar(dto.idVeterinario()));
        consulta.setDataConsulta(dto.dataConsulta());
        consulta.setTipoConsulta(dto.tipoConsulta());

        return consultaRepository.save(consulta);
    }

    @Transactional
    public void excluir(UsuarioPrincipal principal, Long id) {
        consultaRepository.delete(buscarPorId(principal, id));
    }

    private void transicionar(Consulta consulta, StatusConsulta novoStatus) {
        Set<StatusConsulta> permitidas = TRANSICOES_PERMITIDAS.getOrDefault(consulta.getStatus(), Set.of());

        if (!permitidas.contains(novoStatus)) {
            throw new RegraDeNegocioException(
                    "Transicao de status invalida: " + consulta.getStatus() + " -> " + novoStatus + ".");
        }

        consulta.setStatus(novoStatus);
    }

    private Consulta buscarOuLancar(Long idConsulta) {
        return consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(idConsulta));
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