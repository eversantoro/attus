package ai.attus.prazos.infraestrutura.persistencia.adaptador;

import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.dominio.repositorio.PrazoProcessualRepositorio;
import ai.attus.prazos.infraestrutura.persistencia.entidade.PrazoProcessualEntidade;
import ai.attus.prazos.infraestrutura.persistencia.mapeador.PrazoProcessualMapeador;
import ai.attus.prazos.infraestrutura.persistencia.repositorio.PrazoProcessualJpaRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PrazoProcessualRepositorioAdaptador implements PrazoProcessualRepositorio {

    private final PrazoProcessualJpaRepositorio jpaRepositorio;

    public PrazoProcessualRepositorioAdaptador(PrazoProcessualJpaRepositorio jpaRepositorio) {
        this.jpaRepositorio = jpaRepositorio;
    }

    @Override
    public PrazoProcessual salvar(PrazoProcessual prazo) {
        PrazoProcessualEntidade entidade = jpaRepositorio.findById(prazo.getId())
                .map(existente -> {
                    PrazoProcessualMapeador.atualizarEntidade(existente, prazo);
                    return existente;
                })
                .orElseGet(() -> PrazoProcessualMapeador.paraEntidade(prazo));

        PrazoProcessualEntidade salva = jpaRepositorio.save(entidade);
        return PrazoProcessualMapeador.paraDominio(salva);
    }

    @Override
    public Optional<PrazoProcessual> buscarPorId(UUID id) {
        return jpaRepositorio.findByIdAndExcluidoFalse(id)
                .map(PrazoProcessualMapeador::paraDominio);
    }

    @Override
    public Page<PrazoProcessual> listarAtivos(Pageable paginacao) {
        return jpaRepositorio.findByExcluidoFalse(paginacao)
                .map(PrazoProcessualMapeador::paraDominio);
    }

    @Override
    public boolean existeAtivoPorId(UUID id) {
        return jpaRepositorio.existsByIdAndExcluidoFalse(id);
    }
}
