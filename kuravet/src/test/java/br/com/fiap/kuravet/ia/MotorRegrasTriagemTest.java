package br.com.fiap.kuravet.ia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static br.com.fiap.kuravet.ia.Sintoma.APATIA;
import static br.com.fiap.kuravet.ia.Sintoma.COCEIRA;
import static br.com.fiap.kuravet.ia.Sintoma.CONVULSAO;
import static br.com.fiap.kuravet.ia.Sintoma.DESMAIO;
import static br.com.fiap.kuravet.ia.Sintoma.DIARREIA;
import static br.com.fiap.kuravet.ia.Sintoma.DIFICULDADE_RESPIRAR;
import static br.com.fiap.kuravet.ia.Sintoma.ESPIRROS;
import static br.com.fiap.kuravet.ia.Sintoma.FALTA_DE_APETITE;
import static br.com.fiap.kuravet.ia.Sintoma.FEBRE;
import static br.com.fiap.kuravet.ia.Sintoma.FERIDA;
import static br.com.fiap.kuravet.ia.Sintoma.INGESTAO_TOXICA;
import static br.com.fiap.kuravet.ia.Sintoma.MANCANDO;
import static br.com.fiap.kuravet.ia.Sintoma.NAO_URINA;
import static br.com.fiap.kuravet.ia.Sintoma.SANGRAMENTO;
import static br.com.fiap.kuravet.ia.Sintoma.TOSSE;
import static br.com.fiap.kuravet.ia.Sintoma.VOMITO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MotorRegrasTriagemTest {

    private static final String AVISO =
            "Esta triagem é orientativa e não substitui a avaliação do veterinário.";

    private final MotorRegrasTriagem motor = new MotorRegrasTriagem();

    static Stream<Arguments> cenarios() {
        return Stream.of(
                // R1 - sinais de emergencia
                arguments(Set.of(DIFICULDADE_RESPIRAR), "cão", 4, 1, Urgencia.EMERGENCIA, "R1"),
                arguments(Set.of(INGESTAO_TOXICA), "gato", 2, null, Urgencia.EMERGENCIA, "R1"),
                arguments(Set.of(SANGRAMENTO), "cão", 6, null, Urgencia.EMERGENCIA, "R1"),
                arguments(Set.of(DESMAIO), "cão", 12, 0, Urgencia.EMERGENCIA, "R1"),
                arguments(Set.of(COCEIRA, CONVULSAO), "cão", 3, 1, Urgencia.EMERGENCIA, "R1"),
                // R2 / R3 - nao urina
                arguments(Set.of(NAO_URINA), "gato", 5, 1, Urgencia.EMERGENCIA, "R2"),
                arguments(Set.of(NAO_URINA), "GATO", 5, null, Urgencia.EMERGENCIA, "R2"),
                arguments(Set.of(NAO_URINA), "cão", 5, 1, Urgencia.ALTA, "R3"),
                // R4 - vomito e diarreia juntos
                arguments(Set.of(VOMITO, DIARREIA), "cão", 4, 1, Urgencia.ALTA, "R4"),
                // R5 - filhote ou idoso com vomito ou diarreia
                arguments(Set.of(VOMITO), "cão", 0, 1, Urgencia.ALTA, "R5"),
                arguments(Set.of(DIARREIA), "gato", 11, 1, Urgencia.ALTA, "R5"),
                // R6 - febre com apatia
                arguments(Set.of(FEBRE, APATIA), "cão", 4, 1, Urgencia.ALTA, "R6"),
                // R7 - moderada
                arguments(Set.of(TOSSE), "cão", 4, 1, Urgencia.MODERADA, "R7"),
                arguments(Set.of(VOMITO), "cão", 4, null, Urgencia.MODERADA, "R7"),
                arguments(Set.of(MANCANDO, FERIDA), "cão", 4, 2, Urgencia.MODERADA, "R7"),
                arguments(Set.of(FALTA_DE_APETITE), "gato", 3, 1, Urgencia.MODERADA, "R7"),
                // R8 - baixa
                arguments(Set.of(COCEIRA, ESPIRROS), "cão", 4, 1, Urgencia.BAIXA, "R8"),
                arguments(Set.of(ESPIRROS), "gato", 2, 10, Urgencia.BAIXA, "R8"),
                // R9 - escalonamento por persistencia
                arguments(Set.of(TOSSE), "cão", 4, 5, Urgencia.ALTA, "R9"),
                arguments(Set.of(FALTA_DE_APETITE), "gato", 3, 3, Urgencia.ALTA, "R9"),
                // R10 - nenhum sintoma reconhecido
                arguments(Set.of(), "cão", 4, null, Urgencia.BAIXA, "R10")
        );
    }

    @ParameterizedTest(name = "{0}, {1} de {2} anos, {3} dias -> {4} ({5})")
    @MethodSource("cenarios")
    void deveClassificarUrgenciaConformeRegras(Set<Sintoma> sintomas, String especie, int idade,
                                               Integer dias, Urgencia esperada, String regraEsperada) {
        ResultadoTriagem resultado = motor.avaliar(new SintomasExtraidos(sintomas, dias), new PerfilPet(especie, idade));

        assertThat(resultado.urgencia()).isEqualTo(esperada);
        assertThat(resultado.regrasAplicadas()).anyMatch(regra -> regra.startsWith(regraEsperada + " - "));
        assertThat(resultado.recomendacao()).endsWith(AVISO);
    }

    @Test
    void deveListarTodasAsRegrasDisparadasEmCasoDeVomitoEDiarreiaEmFilhote() {
        ResultadoTriagem resultado = motor.avaliar(
                new SintomasExtraidos(Set.of(VOMITO, DIARREIA), 1), new PerfilPet("cão", 0));

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.ALTA);
        assertThat(codigos(resultado)).containsExactly("R4", "R5", "R7");
        assertThat(resultado.recomendacao()).startsWith("Solicite uma teleconsulta prioritária ainda hoje.");
    }

    @Test
    void deveListarTodasAsRegrasDisparadasEmGatoComEmergenciaEOutrosSintomas() {
        ResultadoTriagem resultado = motor.avaliar(
                new SintomasExtraidos(Set.of(NAO_URINA, CONVULSAO, FEBRE, APATIA, COCEIRA), 2),
                new PerfilPet("Gato", 7));

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(codigos(resultado)).containsExactly("R1", "R2", "R6", "R7", "R8");
        assertThat(resultado.recomendacao()).contains("pronto-socorro veterinário");
    }

    @Test
    void deveEscalonarModeradaPersistenteInformandoOsDias() {
        ResultadoTriagem resultado = motor.avaliar(
                new SintomasExtraidos(Set.of(TOSSE), 5), new PerfilPet("cão", 4));

        assertThat(resultado.regrasAplicadas()).contains("R9 - sintomas persistentes há 5 dias");
    }

    @Test
    void naoDeveEscalonarQuandoUrgenciaNaoEhModerada() {
        ResultadoTriagem resultado = motor.avaliar(
                new SintomasExtraidos(Set.of(COCEIRA), 30), new PerfilPet("cão", 4));

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.BAIXA);
        assertThat(codigos(resultado)).containsExactly("R8");
    }

    @Test
    void deveRecomendarDescreverMelhorQuandoNenhumaRegraDispara() {
        ResultadoTriagem resultado = motor.avaliar(new SintomasExtraidos(null, null), new PerfilPet("cão", 4));

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.BAIXA);
        assertThat(resultado.regrasAplicadas()).containsExactly("R10 - nenhum sintoma reconhecido no relato");
        assertThat(resultado.recomendacao())
                .isEqualTo("Descreva melhor os sintomas ou agende uma teleconsulta de rotina. " + AVISO);
    }

    @Test
    void recomendacaoDeveSempreTerminarComOAviso() {
        for (Sintoma sintoma : Sintoma.values()) {
            ResultadoTriagem resultado = motor.avaliar(
                    new SintomasExtraidos(Set.of(sintoma), 1), new PerfilPet("cão", 4));
            assertThat(resultado.recomendacao()).endsWith(AVISO);
        }
    }

    @Test
    void resultadoDeveTerListaDeRegrasImutavel() {
        ResultadoTriagem resultado = motor.avaliar(
                new SintomasExtraidos(Set.of(TOSSE), 1), new PerfilPet("cão", 4));

        assertThrows(UnsupportedOperationException.class, () -> resultado.regrasAplicadas().add("R99 - invasora"));
    }

    @Test
    void sintomasExtraidosDeveRejeitarDuracaoNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new SintomasExtraidos(Set.of(TOSSE), -1));
    }

    @Test
    void sintomasExtraidosNaoPodeSerAlteradoPorFora() {
        Set<Sintoma> original = new HashSet<>(EnumSet.of(TOSSE));
        SintomasExtraidos extraidos = new SintomasExtraidos(original, 1);

        original.add(CONVULSAO);

        assertThat(extraidos.sintomas()).containsExactly(TOSSE);
        assertThrows(UnsupportedOperationException.class, () -> extraidos.sintomas().add(DESMAIO));
    }

    @Test
    void sintomasExtraidosDeveConverterNuloEmConjuntoVazio() {
        SintomasExtraidos extraidos = new SintomasExtraidos(null, null);

        assertThat(extraidos.sintomas()).isEmpty();
        assertThat(extraidos.duracaoDias()).isNull();
    }

    @Test
    void perfilPetDeveReconhecerGatoSemAcentoESemCaixa() {
        assertThat(new PerfilPet(" Gato ", 3).isGato()).isTrue();
        assertThat(new PerfilPet("GATO", 3).isGato()).isTrue();
        assertThat(new PerfilPet("cão", 3).isGato()).isFalse();
        assertThat(new PerfilPet(null, 3).isGato()).isFalse();
        assertThat(new PerfilPet("cão", 0).isFilhote()).isTrue();
        assertThat(new PerfilPet("cão", 10).isIdoso()).isTrue();
    }

    @Test
    void urgenciaDeveCompararNiveisESugerirTeleconsultaPrioritaria() {
        assertThat(Urgencia.MODERADA.maior(Urgencia.ALTA)).isEqualTo(Urgencia.ALTA);
        assertThat(Urgencia.EMERGENCIA.maior(Urgencia.BAIXA)).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(Urgencia.ALTA.sugereTeleconsultaPrioritaria()).isTrue();
        assertThat(Urgencia.EMERGENCIA.sugereTeleconsultaPrioritaria()).isTrue();
        assertThat(Urgencia.MODERADA.sugereTeleconsultaPrioritaria()).isFalse();
        assertThat(Urgencia.BAIXA.sugereTeleconsultaPrioritaria()).isFalse();
    }

    private static List<String> codigos(ResultadoTriagem resultado) {
        return resultado.regrasAplicadas().stream()
                .map(regra -> regra.substring(0, regra.indexOf(" - ")))
                .toList();
    }
}
