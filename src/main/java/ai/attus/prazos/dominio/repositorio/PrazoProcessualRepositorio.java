package ai.attus.prazos.dominio.repositorio;

import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PrazoProcessualRepositorio {

    PrazoProcessual salvar(PrazoProcessual prazo);

    Optional<PrazoProcessual> buscarPorId(UUID id);

    Page<PrazoProcessual> listarAtivos(Pageable paginacao);

    boolean existeAtivoPorId(UUID id);
}
