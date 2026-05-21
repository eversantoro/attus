package ai.attus.prazos.dominio.modelo;

import ai.attus.prazos.dominio.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrazoProcessualTest {

    private static final String CNJ_VALIDO = "0001234-56.2024.8.26.0100";

    @Test
    void deveCriarPrazoComDadosValidos() {
        assertDoesNotThrow(() -> PrazoProcessual.criar(
                CNJ_VALIDO,
                "Descrição válida do prazo",
                LocalDate.now().plusDays(5)));
    }

    @Test
    void deveRejeitarDataVencimentoNoPassado() {
        assertThrows(RegraNegocioException.class, () -> PrazoProcessual.criar(
                CNJ_VALIDO,
                "Descrição válida do prazo",
                LocalDate.now().minusDays(1)));
    }

    @Test
    void deveRejeitarDescricaoCurta() {
        assertThrows(RegraNegocioException.class, () -> PrazoProcessual.criar(
                CNJ_VALIDO,
                "curta",
                LocalDate.now().plusDays(1)));
    }

    @Test
    void deveRejeitarNumeroCnjInvalido() {
        assertThrows(RegraNegocioException.class, () -> PrazoProcessual.criar(
                "invalido",
                "Descrição válida do prazo",
                LocalDate.now().plusDays(1)));
    }
}
