package ai.attus.prazos.aplicacao.casouso;

import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarPrazosCasoUso {

    private final PrazoProcessualRepositorio repositorio;

    public ListarPrazosCasoUso(PrazoProcessualRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Page<PrazoResposta> executar(Pageable paginacao) {
        return repositorio.listarAtivos(paginacao).map(PrazoResposta::de);
    }
}
