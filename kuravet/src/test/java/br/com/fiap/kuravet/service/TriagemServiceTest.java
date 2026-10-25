package br.com.fiap.kuravet.service;

import br.com.fiap.kuravet.dto.triagem.TriagemRequestDTO;
import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.exception.PetNaoEncontradoException;
import br.com.fiap.kuravet.exception.TriagemNaoEncontradaException;
import br.com.fiap.kuravet.ia.ContextoOrientacao;
import br.com.fiap.kuravet.ia.ExtratorComFallback;
import br.com.fiap.kuravet.ia.ExtratorPalavrasChave;
import br.com.fiap.kuravet.ia.GeradorOrientacaoComFallback;
import br.com.fiap.kuravet.ia.GeradorOrientacaoModeloFixo;
import br.com.fiap.kuravet.ia.MetodoExtracao;
import br.com.fiap.kuravet.ia.MotorRegrasTriagem;
import br.com.fiap.kuravet.ia.OrientacaoGerada;
import br.com.fiap.kuravet.ia.OrigemOrientacao;
import br.com.fiap.kuravet.ia.ResultadoExtracao;
import br.com.fiap.kuravet.ia.SintomasExtraidos;
import br.com.fiap.kuravet.ia.llm.ExtratorGemini;
import br.com.fiap.kuravet.ia.llm.GeminiProperties;
import br.com.fiap.kuravet.ia.llm.GeradorOrientacaoGemini;
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
    private ExtratorComFallback extratorSemLlm;

    @BeforeEach
    void setUp() {
        // LLM desligada: extrator e orientacao caem direto nas palavras-chave e no modelo fixo, sem rede.
        GeminiProperties semLlm = new GeminiProperties(false, "", "http://localhost", "modelo", 1000);
        extratorSemLlm = new ExtratorComFallback(semLlm,
                org.mockito.Mockito.mock(ExtratorGemini.class), new ExtratorPalavrasChave());
        GeradorOrientacaoComFallback orientacaoSemLlm = new GeradorOrientacaoComFallback(semLlm,
                org.mockito.Mockito.mock(GeradorOrientacaoGemini.class), new GeradorOrientacaoModeloFixo());
        triagemService = new TriagemService(triagemRepository, petService, extratorSemLlm,
                new MotorRegrasTriagem(), orientacaoSemLlm);
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
    void deveGravarOMetodoRealmenteUsadoNaExtracao() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet gato = pet(2L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(5));
        when(petService.buscarPorId(tutor, 2L)).thenReturn(gato);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));
        ExtratorComFallback extrator = org.mockito.Mockito.mock(ExtratorComFallback.class);
        when(extrator.extrair(any())).thenReturn(new ResultadoExtracao(
                new SintomasExtraidos(Set.of(Sintoma.TOSSE), 1), MetodoExtracao.LLM));
        GeradorOrientacaoComFallback gerador = org.mockito.Mockito.mock(GeradorOrientacaoComFallback.class);
        TriagemService comLlm = new TriagemService(triagemRepository, petService, extrator,
                new MotorRegrasTriagem(), gerador);

        Triagem resultado = comLlm.realizar(tutor, new TriagemRequestDTO(2L, "tosse"));

        assertThat(resultado.getMetodoExtracao()).isEqualTo(MetodoExtracao.LLM);
        assertThat(resultado.getSintomas()).containsExactly(Sintoma.TOSSE);
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

    // ---------- orientacao personalizada ----------

    @Test
    void devePersistirOrientacaoComAOrigemQueAProduziu() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet gato = pet(2L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(5));
        when(petService.buscarPorId(tutor, 2L)).thenReturn(gato);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));

        Triagem resultado = triagemService.realizar(tutor,
                new TriagemRequestDTO(2L, "Minha gata não faz xixi desde ontem"));

        assertThat(resultado.getOrigemOrientacao()).isEqualTo(OrigemOrientacao.MODELO_FIXO);
        assertThat(resultado.getOrientacao())
                .startsWith("Sintomas identificados em Pet 2: não urina.")
                .contains("Como a urgência é EMERGENCIA")
                .endsWith(GeradorOrientacaoComFallback.AVISO);
    }

    @Test
    void geradorDeveReceberAUrgenciaDoMotorEAsTriagensAnterioresDoPet() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet gato = pet(2L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(5));
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        Triagem anterior = triagem(1L, ID_TUTOR_1, Urgencia.MODERADA, ontem);
        anterior.setSintomas(Set.of(Sintoma.VOMITO));
        when(petService.buscarPorId(tutor, 2L)).thenReturn(gato);
        when(triagemRepository.findTop3ByPetIdPetOrderByDataTriagemDesc(2L)).thenReturn(List.of(anterior));
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));
        GeradorOrientacaoComFallback gerador = org.mockito.Mockito.mock(GeradorOrientacaoComFallback.class);
        when(gerador.gerar(any())).thenReturn(new OrientacaoGerada("Orientação da IA.", OrigemOrientacao.IA_GENERATIVA));
        TriagemService comGerador = new TriagemService(triagemRepository, petService, extratorSemLlm,
                new MotorRegrasTriagem(), gerador);

        Triagem resultado = comGerador.realizar(tutor, new TriagemRequestDTO(2L, "Minha gata não faz xixi desde ontem"));

        ArgumentCaptor<ContextoOrientacao> captor = ArgumentCaptor.forClass(ContextoOrientacao.class);
        verify(gerador).gerar(captor.capture());
        ContextoOrientacao contexto = captor.getValue();
        assertThat(contexto.nomePet()).isEqualTo("Pet 2");
        assertThat(contexto.especie()).isEqualTo("Gato");
        assertThat(contexto.idadeAnos()).isEqualTo(5);
        assertThat(contexto.urgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(contexto.anteriores()).containsExactly(
                new ContextoOrientacao.TriagemAnterior(ontem, Urgencia.MODERADA, Set.of(Sintoma.VOMITO)));

        assertThat(resultado.getUrgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(resultado.getRecomendacao()).contains("pronto-socorro veterinário");
        assertThat(resultado.getOrientacao()).isEqualTo("Orientação da IA.");
        assertThat(resultado.getOrigemOrientacao()).isEqualTo(OrigemOrientacao.IA_GENERATIVA);
    }

    @Test
    void erroNoGeradorNaoDeveImpedirATriagemDeSerSalva() {
        UsuarioPrincipal tutor = principalTutor(ID_TUTOR_1);
        Pet gato = pet(2L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(5));
        when(petService.buscarPorId(tutor, 2L)).thenReturn(gato);
        when(triagemRepository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));
        GeradorOrientacaoComFallback gerador = org.mockito.Mockito.mock(GeradorOrientacaoComFallback.class);
        when(gerador.gerar(any())).thenThrow(new IllegalStateException("falha inesperada"));
        TriagemService comGeradorQuebrado = new TriagemService(triagemRepository, petService, extratorSemLlm,
                new MotorRegrasTriagem(), gerador);

        Triagem resultado = comGeradorQuebrado.realizar(tutor,
                new TriagemRequestDTO(2L, "Minha gata não faz xixi desde ontem"));

        verify(triagemRepository).save(any(Triagem.class));
        assertThat(resultado.getUrgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(resultado.getOrientacao()).isNull();
        assertThat(resultado.getOrigemOrientacao()).isNull();
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

    @Test
    void deveFiltrarPorUrgenciaMantendoAOrdemPorData() {
        LocalDateTime agora = LocalDateTime.now();
        when(triagemRepository.findAll()).thenReturn(List.of(
                triagem(1L, ID_TUTOR_1, Urgencia.ALTA, agora.minusDays(1)),
                triagem(2L, ID_TUTOR_2, Urgencia.BAIXA, agora),
                triagem(3L, ID_TUTOR_2, Urgencia.ALTA, agora)));

        List<Triagem> triagens = triagemService.listarPorUrgencia(principalVeterinario(), Urgencia.ALTA);

        assertThat(triagens).extracting(Triagem::getIdTriagem).containsExactly(3L, 1L);
    }

    @Test
    void listarUrgentesDeveTrazerSomenteAltaEEmergencia() {
        LocalDateTime agora = LocalDateTime.now();
        when(triagemRepository.findAll()).thenReturn(List.of(
                triagem(1L, ID_TUTOR_1, Urgencia.BAIXA, agora),
                triagem(2L, ID_TUTOR_1, Urgencia.MODERADA, agora),
                triagem(3L, ID_TUTOR_2, Urgencia.ALTA, agora),
                triagem(4L, ID_TUTOR_2, Urgencia.EMERGENCIA, agora)));

        List<Triagem> triagens = triagemService.listarUrgentes(principalVeterinario());

        assertThat(triagens).extracting(Triagem::getIdTriagem).containsExactly(4L, 3L);
    }

    // ---------- listarPorPet ----------

    @Test
    void listarPorPetDeveOrdenarDaMaisRecenteParaAMaisAntiga() {
        UsuarioPrincipal vet = principalVeterinario();
        LocalDateTime agora = LocalDateTime.now();
        Pet pet = pet(5L, ID_TUTOR_1, "Gato", LocalDate.now().minusYears(2));
        when(petService.buscarPorId(vet, 5L)).thenReturn(pet);
        when(triagemRepository.findByPetIdPet(5L)).thenReturn(List.of(
                triagem(1L, ID_TUTOR_1, Urgencia.BAIXA, agora.minusDays(5)),
                triagem(2L, ID_TUTOR_1, Urgencia.EMERGENCIA, agora.minusDays(10)),
                triagem(3L, ID_TUTOR_1, Urgencia.MODERADA, agora)));

        List<Triagem> historico = triagemService.listarPorPet(vet, 5L);

        assertThat(historico).extracting(Triagem::getIdTriagem).containsExactly(3L, 1L, 2L);
    }

    @Test
    void listarPorPetDeveRespeitarOwnershipDoPetService() {
        UsuarioPrincipal outroTutor = principalTutor(ID_TUTOR_2);
        when(petService.buscarPorId(outroTutor, 5L)).thenThrow(new PetNaoEncontradoException(5L));

        assertThrows(PetNaoEncontradoException.class, () -> triagemService.listarPorPet(outroTutor, 5L));

        verify(triagemRepository, never()).findByPetIdPet(any());
    }

    // ---------- contarUrgentesDesde ----------

    @Test
    void contarUrgentesDesdeDeveSomarSomenteAltaEEmergencia() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        when(triagemRepository.countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia.ALTA, desde)).thenReturn(2L);
        when(triagemRepository.countByUrgenciaAndDataTriagemGreaterThanEqual(Urgencia.EMERGENCIA, desde)).thenReturn(3L);

        assertThat(triagemService.contarUrgentesDesde(desde)).isEqualTo(5L);

        verify(triagemRepository, never())
                .countByUrgenciaAndDataTriagemGreaterThanEqual(eq(Urgencia.MODERADA), any());
        verify(triagemRepository, never())
                .countByUrgenciaAndDataTriagemGreaterThanEqual(eq(Urgencia.BAIXA), any());
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
