package ai.attus.prazos.dominio.excecao;

import java.util.UUID;

public class PrazoNaoEncontradoException extends RuntimeException {

    public PrazoNaoEncontradoException(UUID id) {
        super("Prazo processual não encontrado: " + id);
    }
}
