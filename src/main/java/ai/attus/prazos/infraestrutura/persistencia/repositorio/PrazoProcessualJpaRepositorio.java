package ai.attus.prazos.infraestrutura.persistencia.repositorio;

import ai.attus.prazos.infraestrutura.persistencia.entidade.PrazoProcessualEntidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrazoProcessualJpaRepositorio extends JpaRepository<PrazoProcessualEntidade, UUID> {

    Page<PrazoProcessualEntidade> findByExcluidoFalse(Pageable paginacao);

    Optional<PrazoProcessualEntidade> findByIdAndExcluidoFalse(UUID id);

    boolean existsByIdAndExcluidoFalse(UUID id);
}
