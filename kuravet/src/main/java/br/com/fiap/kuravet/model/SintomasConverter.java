package br.com.fiap.kuravet.model;

import br.com.fiap.kuravet.ia.Sintoma;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Grava o conjunto de sintomas como texto separado por virgula ("VOMITO,DIARREIA").
 * Conjunto vazio vira NULL na coluna e NULL volta como conjunto vazio.
 */
@Converter
public class SintomasConverter implements AttributeConverter<Set<Sintoma>, String> {

    private static final String SEPARADOR = ",";

    @Override
    public String convertToDatabaseColumn(Set<Sintoma> sintomas) {
        if (sintomas == null || sintomas.isEmpty()) {
            return null;
        }
        return EnumSet.copyOf(sintomas).stream()
                .map(Sintoma::name)
                .collect(Collectors.joining(SEPARADOR));
    }

    @Override
    public Set<Sintoma> convertToEntityAttribute(String coluna) {
        if (coluna == null || coluna.isBlank()) {
            return EnumSet.noneOf(Sintoma.class);
        }
        return Arrays.stream(coluna.split(SEPARADOR))
                .map(String::trim)
                .filter(nome -> !nome.isEmpty())
                .map(Sintoma::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Sintoma.class)));
    }
}
