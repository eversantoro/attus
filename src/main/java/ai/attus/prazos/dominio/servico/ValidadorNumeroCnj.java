package ai.attus.prazos.dominio.servico;

import java.util.regex.Pattern;

public final class ValidadorNumeroCnj {

    /**
     * Formato CNJ: NNNNNNN-DD.AAAA.J.TR.OOOO
     */
    private static final Pattern PADRAO_CNJ = Pattern.compile(
            "^\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}$");

    private ValidadorNumeroCnj() {
    }

    public static boolean ehValido(String numeroProcesso) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return false;
        }
        return PADRAO_CNJ.matcher(numeroProcesso.trim()).matches();
    }
}
