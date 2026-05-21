package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.excecao.PrazoNaoEncontradoException;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarPrazoCasoUso {

    private final PrazoProcessualRepositorio repositorio;

    public BuscarPrazoCasoUso(PrazoProcessualRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PrazoResposta executar(UUID id) {
        return repositorio.buscarPorId(id)
                .map(PrazoResposta::de)
                .orElseThrow(() -> new PrazoNaoEncontradoException(id));
    }
}
