package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.aplicacao.dto.AtualizarPrazoComando;
import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.excecao.ConcorrenciaPrazoException;
import ai.attus.prazos.dominio.excecao.PrazoNaoEncontradoException;
import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtualizarPrazoCasoUso {

    private static final Logger log = LoggerFactory.getLogger(AtualizarPrazoCasoUso.class);

    private final PrazoProcessualRepositorio repositorio;

    public AtualizarPrazoCasoUso(PrazoProcessualRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public PrazoResposta executar(AtualizarPrazoComando comando) {
        PrazoProcessual prazo = repositorio.buscarPorId(comando.id())
                .orElseThrow(() -> new PrazoNaoEncontradoException(comando.id()));

        if (comando.versao() != null && !comando.versao().equals(prazo.getVersao())) {
            throw new ConcorrenciaPrazoException();
        }

        prazo.atualizar(
                comando.numeroProcesso(),
                comando.descricao(),
                comando.dataVencimento(),
                comando.status());

        PrazoProcessual salvo = repositorio.salvar(prazo);
        log.info("Prazo processual alterado | id={} | status={}",
                salvo.getId(), salvo.getStatus());
        return PrazoResposta.de(salvo);
    }
}
