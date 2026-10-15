package br.com.fiap.kuravet.ia;

import java.text.Normalizer;
import java.util.Locale;

public record PerfilPet(String especie, int idadeAnos) {

    public boolean isFilhote() {
        return idadeAnos < 1;
    }

    public boolean isIdoso() {
        return idadeAnos >= 10;
    }

    public boolean isGato() {
        return "gato".equals(normalizar(especie));
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }
}
