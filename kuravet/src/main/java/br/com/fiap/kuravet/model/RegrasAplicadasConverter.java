package br.com.fiap.kuravet.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Grava as regras aplicadas em uma unica coluna, separadas por " | ".
 * As descricoes das regras contem virgulas, por isso o separador nao e a virgula.
 */
@Converter
public class RegrasAplicadasConverter implements AttributeConverter<List<String>, String> {

    private static final String SEPARADOR = " | ";

    @Override
    public String convertToDatabaseColumn(List<String> regras) {
        if (regras == null || regras.isEmpty()) {
            return "";
        }
        return String.join(SEPARADOR, regras);
    }

    @Override
    public List<String> convertToEntityAttribute(String coluna) {
        if (coluna == null || coluna.isBlank()) {
            return List.of();
        }
        return List.of(coluna.split(Pattern.quote(SEPARADOR)));
    }
}
