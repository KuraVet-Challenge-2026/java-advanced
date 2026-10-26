package br.com.fiap.kuravet.dto.pet;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Bean Validation pura, sem Spring: so o campo codigoIdentificacao varia.
class PetRequestDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    private static Set<ConstraintViolation<PetRequestDTO>> validarCodigo(String codigoIdentificacao) {
        PetRequestDTO dto = new PetRequestDTO("Thor", "Cachorro", null,
                LocalDate.of(2020, 1, 1), 'M', codigoIdentificacao);
        return validator.validateProperty(dto, "codigoIdentificacao");
    }

    @Test
    void codigoComEspacosNasBordasDeveSerValido() {
        assertThat(validarCodigo(" pz-123 ")).isEmpty();
    }

    @Test
    void codigoComEspacoNoMeioDeveSerInvalido() {
        assertThat(validarCodigo("pz 123")).isNotEmpty();
    }

    @Test
    void codigoComCaractereEspecialDeveSerInvalido() {
        assertThat(validarCodigo("pz@123"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("O código de identificação deve conter apenas letras, números, ponto, hífen ou underscore.");
    }

    @Test
    void codigoNuloDeveSerValido() {
        assertThat(validarCodigo(null)).isEmpty();
    }

    @Test
    void codigoCom41CaracteresDeveSerInvalido() {
        assertThat(validarCodigo("A".repeat(41)))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("O código de identificação deve ter no máximo 40 caracteres.");
    }

    @Test
    void codigoCom40CaracteresDeveSerValido() {
        assertThat(validarCodigo("A".repeat(40))).isEmpty();
    }
}
