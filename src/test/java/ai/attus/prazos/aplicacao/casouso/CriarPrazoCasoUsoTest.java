package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.aplicacao.dto.CriarPrazoComando;
import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarPrazoCasoUsoTest {

    @Mock
    private PrazoProcessualRepositorio repositorio;

    @InjectMocks
    private CriarPrazoCasoUso casoUso;

    @Test
    void deveCriarPrazoComSucesso() {
        when(repositorio.salvar(any(PrazoProcessual.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        PrazoResposta resposta = casoUso.executar(new CriarPrazoComando(
                "0001234-56.2024.8.26.0100",
                "Prazo para manifestação",
                LocalDate.now().plusDays(10)));

        assertNotNull(resposta.id());
        assertEquals("0001234-56.2024.8.26.0100", resposta.numeroProcesso());
    }
}
