package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.aplicacao.dto.CriarPrazoComando;
import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarPrazoCasoUso {

    private static final Logger log = LoggerFactory.getLogger(CriarPrazoCasoUso.class);

    private final PrazoProcessualRepositorio repositorio;

    public CriarPrazoCasoUso(PrazoProcessualRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public PrazoResposta executar(CriarPrazoComando comando) {
        PrazoProcessual prazo = PrazoProcessual.criar(
                comando.numeroProcesso(),
                comando.descricao(),
                comando.dataVencimento());
        PrazoProcessual salvo = repositorio.salvar(prazo);
        log.info("Prazo processual criado | id={} | numeroProcesso={}",
                salvo.getId(), salvo.getNumeroProcesso());
        return PrazoResposta.de(salvo);
    }
}
