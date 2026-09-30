package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.consulta.ConsultaRequestDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.enums.StatusConsulta;
import br.com.fiap.kuravet.exception.ConsultaNaoEncontradaException;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.RegraDeNegocioException;
import br.com.fiap.kuravet.model.Consulta;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.model.Veterinario;
import br.com.fiap.kuravet.repository.ConsultaRepository;
import br.com.fiap.kuravet.repository.PetRepository;
import br.com.fiap.kuravet.repository.VeterinarioRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    private ConsultaService consultaService;

    private static final Long ID_TUTOR_1 = 1L;
    private static final Long ID_TUTOR_2 = 2L;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        consultaService = new ConsultaService(consultaRepository, petRepository, veterinarioRepository);
    }

    // ---------- helpers ----------

    private UsuarioPrincipal principalTutor(Long idTutor) {
        Usuario usuario = Usuario.builder()
                .idUsuario(idTutor)
                .username("tutor" + idTutor)
                .senha("senha")
                .perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(idTutor).nome("Tutor " + idTutor).cpf("111.111.111-1" + idTutor).build())
                .build();
        return new UsuarioPrincipal(usuario);
    }

    private UsuarioPrincipal principalVeterinario() {
        Usuario usuario = Usuario.builder()
                .idUsuario(99L)
                .username("vet")
                .senha("senha")
                .perfil(Perfil.VETERINARIO)
                .build();
        return new UsuarioPrincipal(usuario);
    }

    private Pet pet(Long idPet, Long idTutor) {
        return Pet.builder()
                .idPet(idPet)
                .nome("Rex")
                .especie("Cachorro")
                .dataNascimento(LocalDate.of(2020, 1, 1))
                .sexo('M')
                .tutor(Tutor.builder().idTutor(idTutor).build())
                .build();
    }

    private Veterinario veterinario(Long idVeterinario) {
        return Veterinario.builder()
                .idVeterinario(idVeterinario)
                .nome("Dra. Ana")
                .crmv("CRMV-" + idVeterinario)
                .build();
    }

    private Consulta consulta(Long idConsulta, Pet pet, Veterinario veterinario,
                               StatusConsulta status, LocalDate dataConsulta) {
        return Consulta.builder()
                .idConsulta(idConsulta)
                .pet(pet)
                .veterinario(veterinario)
                .dataConsulta(dataConsulta)
                .tipoConsulta("Rotina")
                .dataSolicitacao(LocalDate.now())
                .status(status)
                .build();
    }

    private void mockSalvarRetornandoArgumento() {
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void invocarTransicao(StatusConsulta alvo, Long idConsulta) {
        switch (alvo) {
            case AGENDADA -> consultaService.aprovarSolicitacao(idConsulta);
            case RECUSADA -> consultaService.recusarSolicitacao(idConsulta, "Agenda indisponivel");
            case REALIZADA -> consultaService.realizarConsultaComDiagnostico(idConsulta, "Diagnostico qualquer");
            case CANCELADA -> consultaService.cancelar(principalVeterinario(), idConsulta);
            default -> throw new IllegalArgumentException("Status alvo nao suportado no teste: " + alvo);
        }
    }

    // ---------- 1. solicitarTeleconsulta ----------

    @Test
    void deveSolicitarTeleconsultaComStatusSolicitadaEDataDeHoje() {
        Pet pet = pet(10L, ID_TUTOR_1);
        Veterinario veterinario = veterinario(20L);
        LocalDate dataConsulta = LocalDate.now().plusDays(5);
        ConsultaRequestDTO dto = new ConsultaRequestDTO(pet.getIdPet(), veterinario.getIdVeterinario(), dataConsulta, "Rotina");

        when(petRepository.findById(pet.getIdPet())).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(veterinario.getIdVeterinario())).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByPetIdPetAndDataConsultaAndStatusIn(
                eq(pet.getIdPet()), eq(dataConsulta), any())).thenReturn(false);
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.solicitarTeleconsulta(principalTutor(ID_TUTOR_1), dto);

        ArgumentCaptor<Consulta> captor = ArgumentCaptor.forClass(Consulta.class);
        verify(consultaRepository).save(captor.capture());
        Consulta salva = captor.getValue();

        assertThat(salva.getStatus()).isEqualTo(StatusConsulta.SOLICITADA);
        assertThat(salva.getDataSolicitacao()).isEqualTo(LocalDate.now());
        assertThat(salva.getPet()).isEqualTo(pet);
        assertThat(salva.getVeterinario()).isEqualTo(veterinario);
        assertThat(salva.getDataConsulta()).isEqualTo(dataConsulta);
        assertThat(resultado).isEqualTo(salva);
    }

    @Test
    void deveLancarPetNaoEncontradoAoSolicitarTeleconsultaParaPetDeOutroTutor() {
        Pet petDeOutroTutor = pet(10L, ID_TUTOR_2);
        ConsultaRequestDTO dto = new ConsultaRequestDTO(
                petDeOutroTutor.getIdPet(), 20L, LocalDate.now().plusDays(5), "Rotina");

        when(petRepository.findById(petDeOutroTutor.getIdPet())).thenReturn(Optional.of(petDeOutroTutor));

        assertThrows(PetNaoEncontradoException.class,
                () -> consultaService.solicitarTeleconsulta(principalTutor(ID_TUTOR_1), dto));

        verifyNoInteractions(veterinarioRepository);
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveLancarRegraDeNegocioAoSolicitarTeleconsultaComConsultaAtivaNaMesmaData() {
        Pet pet = pet(10L, ID_TUTOR_1);
        Veterinario veterinario = veterinario(20L);
        LocalDate dataConsulta = LocalDate.now().plusDays(5);
        ConsultaRequestDTO dto = new ConsultaRequestDTO(pet.getIdPet(), veterinario.getIdVeterinario(), dataConsulta, "Rotina");

        when(petRepository.findById(pet.getIdPet())).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(veterinario.getIdVeterinario())).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByPetIdPetAndDataConsultaAndStatusIn(
                eq(pet.getIdPet()), eq(dataConsulta), any())).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
                () -> consultaService.solicitarTeleconsulta(principalTutor(ID_TUTOR_1), dto));

        verify(consultaRepository, never()).save(any());
    }

    // ---------- 4, 5, 6, 7. transicoes validas ----------

    @Test
    void deveAprovarSolicitacaoTransicionandoDeSolicitadaParaAgendada() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.aprovarSolicitacao(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void deveRecusarSolicitacaoTransicionandoParaRecusadaEGravandoMotivo() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.recusarSolicitacao(1L, "Agenda do veterinario indisponivel");

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.RECUSADA);
        assertThat(resultado.getMotivoRecusa()).isEqualTo("Agenda do veterinario indisponivel");
        verify(consultaRepository).save(consulta);
    }

    @Test
    void deveRealizarConsultaTransicionandoDeAgendadaParaRealizadaEGravandoDiagnostico() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.AGENDADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.realizarConsultaComDiagnostico(1L, "Pet saudavel");

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.REALIZADA);
        assertThat(resultado.getDiagnostico()).isEqualTo("Pet saudavel");
        verify(consultaRepository).save(consulta);
    }

    // ---------- cancelamento: regra depende do perfil (TUTOR x VETERINARIO) ----------

    @Test
    void deveCancelarSolicitacaoDoProprioTutor() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.cancelar(principalTutor(ID_TUTOR_1), 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void deveCancelarAgendadaDoProprioTutor() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.AGENDADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.cancelar(principalTutor(ID_TUTOR_1), 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void deveLancarConsultaNaoEncontradaAoTutorCancelarConsultaDeOutroTutor() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_2), veterinario(20L),
                StatusConsulta.AGENDADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThrows(ConsultaNaoEncontradaException.class,
                () -> consultaService.cancelar(principalTutor(ID_TUTOR_1), 1L));

        verify(consultaRepository, never()).save(any());
    }

    @ParameterizedTest(name = "deveRecusarCancelamentoPeloTutorEmEstadoTerminal: {0}")
    @EnumSource(value = StatusConsulta.class, names = {"REALIZADA", "RECUSADA", "CANCELADA"})
    void deveRecusarCancelamentoPeloTutorEmEstadoTerminal(StatusConsulta statusAtual) {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                statusAtual, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraDeNegocioException.class,
                () -> consultaService.cancelar(principalTutor(ID_TUTOR_1), 1L));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveCancelarConsultaAgendadaPeloVeterinario() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.AGENDADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        mockSalvarRetornandoArgumento();

        Consulta resultado = consultaService.cancelar(principalVeterinario(), 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void deveRecusarCancelamentoDeSolicitacaoPendentePeloVeterinario() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> consultaService.cancelar(principalVeterinario(), 1L));

        assertThat(excecao.getMessage()).isEqualTo("Solicitacoes pendentes devem ser recusadas com um motivo.");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void podeCancelarDeveSerCoerenteComAsRegrasDeCancelamento() {
        Consulta solicitada = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        Consulta agendada = consulta(2L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.AGENDADA, LocalDate.now().plusDays(5));
        Consulta realizada = consulta(3L, pet(10L, ID_TUTOR_1), veterinario(20L),
                StatusConsulta.REALIZADA, LocalDate.now().plusDays(5));

        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        UsuarioPrincipal veterinario = principalVeterinario();

        assertThat(consultaService.podeCancelar(tutor, solicitada)).isTrue();
        assertThat(consultaService.podeCancelar(tutor, agendada)).isTrue();
        assertThat(consultaService.podeCancelar(tutor, realizada)).isFalse();

        assertThat(consultaService.podeCancelar(veterinario, solicitada)).isFalse();
        assertThat(consultaService.podeCancelar(veterinario, agendada)).isTrue();
        assertThat(consultaService.podeCancelar(veterinario, realizada)).isFalse();
    }

    // ---------- 8. transicoes invalidas ----------

    @ParameterizedTest(name = "deveRecusarTransicaoDeEstadoInvalida: {0} -> {1}")
    @CsvSource({
            "SOLICITADA,REALIZADA",
            "REALIZADA,CANCELADA",
            "RECUSADA,AGENDADA",
            "CANCELADA,AGENDADA"
    })
    void deveRecusarTransicaoDeEstadoInvalida(StatusConsulta statusAtual, StatusConsulta statusAlvo) {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_1), veterinario(20L),
                statusAtual, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraDeNegocioException.class, () -> invocarTransicao(statusAlvo, 1L));

        verify(consultaRepository, never()).save(any());
    }

    // ---------- 9, 10. buscarPorId e ownership ----------

    @Test
    void deveLancarConsultaNaoEncontradaAoBuscarConsultaDeOutroTutor() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_2), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThrows(ConsultaNaoEncontradaException.class,
                () -> consultaService.buscarPorId(principalTutor(ID_TUTOR_1), 1L));
    }

    @Test
    void deveRetornarConsultaDeQualquerTutorQuandoUsuarioEVeterinario() {
        Consulta consulta = consulta(1L, pet(10L, ID_TUTOR_2), veterinario(20L),
                StatusConsulta.SOLICITADA, LocalDate.now().plusDays(5));
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        Consulta resultado = consultaService.buscarPorId(principalVeterinario(), 1L);

        assertThat(resultado).isEqualTo(consulta);
    }
}
