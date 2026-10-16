package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.ia.ExtratorPalavrasChave;
import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.MotorRegrasTriagem;
import br.com.fiap.kuravet.ia.Sintoma;
import br.com.fiap.kuravet.ia.Urgencia;
import br.com.fiap.kuravet.model.Pet;
import br.com.fiap.kuravet.model.Triagem;
import br.com.fiap.kuravet.model.Tutor;
import br.com.fiap.kuravet.model.Usuario;
import br.com.fiap.kuravet.repository.TriagemRepository;
import br.com.fiap.kuravet.security.UsuarioPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriagemServiceTest {

    private static final Long ID_TUTOR_1 = 1L;
    private static final Long ID_TUTOR_2 = 2L;

    @Mock
    private TriagemRepository triagemRepository;

    @Mock
    private PetService petService;

    private TriagemService triagemService;

    @BeforeEach
    void setUp() {
        // Extrator e motor sao deterministicos e sem dependencias: usamos as implementacoes reais.
        triagemService = new TriagemService(triagemRepository, petService,
                new ExtratorPalavrasChave(), new MotorRegrasTriagem());
    }

    // ---------- helpers ----------

    private UsuarioPrincipal principalTutor(Long idTutor) {
        Usuario usuario = Usuario.builder()
                .idUsuario(idTutor)
                .username("tutor" + idTutor)
                .senha("senha")
                .perfil(Perfil.TUTOR)
                .tutor(Tutor.builder().idTutor(idTutor).nome("Tutor " + idTutor).build())
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

    private Pet pet(Long idPet, Long idTutor, String especie, LocalDate nascimento) {
        return Pet.builder()
                .idPet(idPet)
                .nome("Pet " + idPet)
                .especie(especie)
                .dataNascimento(nascimento)
                .sexo('M')
                .tutor(Tutor.builder().idTutor(idTutor).nome("Tutor " + idTutor).build())
                .build();
    }

    private Triagem triagem(Long id, Long idTutor, Urgencia urgencia, LocalDateTime data) {
        return Triagem.builder()
                .idTriagem(id)
                .pet(pet(10L + id, idTutor, "Cachorro", LocalDate.now().minusYears(4)))
                .relato("relato de teste")
                .urgencia(urgencia)
                .recomendacao("recomendacao")
                .regrasAplicadas(List.of("R7 - teste"))
                .metodoExtracao(MetodoExtracao.PALAVRAS_CHAVE)
                .dataTriagem(data)
                .build();
    }

    // ---------- realizar ----------

    @Test
    void devePersistirTriagemComUrgenciaDecididaPeloMotor() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet gato = pet(2L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(5));
        when(petService.buscarPorId(tutor, 2L)).thenReturn(gato);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> {
            Triagem t = inv.getArgument(0);
            t.setIdTriagem(1L);
            return t;
        });

        Triagem resultado = triagemService.realizar(tutor,
                new TriagemRequestDTO(2L, "Minha gata não faz xixi desde ontem"));

        ArgumentCaptor<Triagem> captor = ArgumentCaptor.forClass(Triagem.class);
        verify(triagemRepository).save(captor.capture());
        Triagem salva = captor.getValue();

        assertThat(salva.getPet()).isSameAs(gato);
        assertThat(salva.getSintomas()).containsExactly(Sintoma.NAO_URINA);
        assertThat(salva.getUrgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(salva.getRegrasAplicadas()).anyMatch(regra -> regra.startsWith("R2 - "));
        assertThat(salva.getRecomendacao()).contains("pronto-socorro veterinário");
        assertThat(salva.getMetodoExtracao()).isEqualTo(MetodoExtracao.PALAVRAS_CHAVE);
        assertThat(salva.getRelato()).isEqualTo("Minha gata não faz xixi desde ontem");
        assertThat(salva.getDataTriagem()).isNotNull();
        assertThat(resultado.getIdTriagem()).isEqualTo(1L);
    }

    @Test
    void deveConsiderarIdadeDoPetNaDecisao() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet filhote = pet(3L, ID_TUTOR_1, "Cachorro", LocalDate.now().minusMonths(6));
        when(petService.buscarPorId(tutor, 3L)).thenReturn(filhote);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));

        Triagem resultado = triagemService.realizar(tutor, new TriagemRequestDTO(3L, "Ele vomitou hoje cedo"));

        assertThat(resultado.getUrgencia()).isEqualTo(Urgencia.ALTA);
        assertThat(resultado.getRegrasAplicadas()).anyMatch(regra -> regra.startsWith("R5 - "));
    }

    @Test
    void naoDevePersistirTriagemParaPetDeOutroTutor() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_2);
        when(petService.buscarPorId(eq(tutor), anyLong())).thenThrow(new PetNaoEncontradoException(1L));

        assertThrows(PetNaoEncontradoException.class,
                () -> triagemService.realizar(tutor, new TriagemRequestDTO(1L, "Está vomitando muito")));

        verify(triagemRepository, never()).save(any());
    }

    // ---------- listar ----------

    @Test
    void veterinarioDeveListarTodasAsTriagens() {
        LocalDateTime agora = LocalDateTime.now();
        when(triagemRepository.findAll()).thenReturn(List.of(
                triagem(1L, ID_TUTOR_1, Urgencia.BAIXA, agora),
                triagem(2L, ID_TUTOR_2, Urgencia.ALTA, agora)));

        List<Triagem> triagens = triagemService.listar(principalVeterinario());

        assertThat(triagens).hasSize(2);
        verify(triagemRepository, never()).findByPetTutorIdTutor(any());
    }

    @Test
    void tutorDeveListarSomenteTriagensDosProprios() {
        LocalDateTime agora = LocalDateTime.now();
        when(triagemRepository.findByPetTutorIdTutor(ID_TUTOR_1))
                .thenReturn(List.of(triagem(1L, ID_TUTOR_1, Urgencia.MODERADA, agora)));

        List<Triagem> triagens = triagemService.listar(principalTutor(ID_TUTOR_1));

        assertThat(triagens).extracting(Triagem::getIdTriagem).containsExactly(1L);
        verify(triagemRepository, never()).findAll();
    }

    @Test
    void deveOrdenarPorUrgenciaDecrescenteEDepoisPorDataMaisRecente() {
        LocalDateTime agora = LocalDateTime.now();
        when(triagemRepository.findAll()).thenReturn(List.of(
                triagem(1L, ID_TUTOR_1, Urgencia.BAIXA, agora),
                triagem(2L, ID_TUTOR_1, Urgencia.MODERADA, agora.minusDays(1)),
                triagem(3L, ID_TUTOR_2, Urgencia.EMERGENCIA, agora.minusDays(3)),
                triagem(4L, ID_TUTOR_2, Urgencia.MODERADA, agora),
                triagem(5L, ID_TUTOR_1, Urgencia.ALTA, agora.minusHours(2))));

        List<Triagem> triagens = triagemService.listar(principalVeterinario());

        assertThat(triagens).extracting(Triagem::getIdTriagem).containsExactly(3L, 5L, 4L, 2L, 1L);
    }

    // ---------- buscarPorId ----------

    @Test
    void tutorNaoDeveVerTriagemDePetDeOutroTutor() {
        when(triagemRepository.findById(1L))
                .thenReturn(Optional.of(triagem(1L, ID_TUTOR_1, Urgencia.ALTA, LocalDateTime.now())));

        assertThrows(TriagemNaoEncontradaException.class,
                () -> triagemService.buscarPorId(principalTutor(ID_TUTOR_2), 1L));
    }

    @Test
    void veterinarioDeveVerQualquerTriagem() {
        when(triagemRepository.findById(1L))
                .thenReturn(Optional.of(triagem(1L, ID_TUTOR_1, Urgencia.ALTA, LocalDateTime.now())));

        assertThat(triagemService.buscarPorId(principalVeterinario(), 1L).getIdTriagem()).isEqualTo(1L);
    }

    @Test
    void deveLancarQuandoTriagemNaoExiste() {
        when(triagemRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TriagemNaoEncontradaException.class,
                () -> triagemService.buscarPorId(principalVeterinario(), 404L));
    }

    @Test
    void sintomasDaTriagemNaoPodemVazarComoNulo() {
        Triagem semSintomas = Triagem.builder().build();

        assertThat(semSintomas.getSintomas()).isEqualTo(Set.of());
        assertThat(semSintomas.getRegrasAplicadas()).isEmpty();
    }
}
