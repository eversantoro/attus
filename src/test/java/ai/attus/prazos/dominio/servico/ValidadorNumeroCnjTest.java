package ai.attus.prazos.dominio.servico;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorNumeroCnjTest {

    @Test
    void deveAceitarNumeroCnjValido() {
        assertTrue(ValidadorNumeroCnj.ehValido("0001234-56.2024.8.26.0100"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "123",
            "0001234-56.2024.8.26",
            "ABCDEFG-56.2024.8.26.0100",
            "0001234-56.2024.8.26.01000"
    })
    void deveRejeitarNumeroCnjInvalido(String numero) {
        assertFalse(ValidadorNumeroCnj.ehValido(numero));
    }
}
