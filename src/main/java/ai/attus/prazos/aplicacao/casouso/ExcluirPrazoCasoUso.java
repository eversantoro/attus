package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.dominio.excecao.PrazoNaoEncontradoException;
import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExcluirPrazoCasoUso {

    private static final Logger log = LoggerFactory.getLogger(ExcluirPrazoCasoUso.class);

    private final PrazoProcessualRepositorio repositorio;

    public ExcluirPrazoCasoUso(PrazoProcessualRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void executar(UUID id) {
        PrazoProcessual prazo = repositorio.buscarPorId(id)
                .orElseThrow(() -> new PrazoNaoEncontradoException(id));
        prazo.excluirLogicamente();
        repositorio.salvar(prazo);
        log.info("Prazo processual excluído (soft delete) | id={}", id);
    }
}
