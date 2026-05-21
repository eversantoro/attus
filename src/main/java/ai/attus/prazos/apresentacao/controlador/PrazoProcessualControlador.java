package ai.attus.prazos.apresentacao.controlador;

import ai.attus.prazos.aplicacao.casouso.AtualizarPrazoCasoUso;
import ai.attus.prazos.aplicacao.casouso.BuscarPrazoCasoUso;
import ai.attus.prazos.aplicacao.casouso.CriarPrazoCasoUso;
import ai.attus.prazos.aplicacao.casouso.ExcluirPrazoCasoUso;
import ai.attus.prazos.aplicacao.casouso.ListarPrazosCasoUso;
import ai.attus.prazos.aplicacao.dto.AtualizarPrazoComando;
import ai.attus.prazos.aplicacao.dto.CriarPrazoComando;
import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.apresentacao.dto.AtualizarPrazoRequisicao;
import ai.attus.prazos.apresentacao.dto.CriarPrazoRequisicao;
import ai.attus.prazos.apresentacao.dto.PrazoRespostaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prazos")
@Tag(name = "Prazos Processuais", description = "Gerenciamento de prazos processuais")
public class PrazoProcessualControlador {

    private final CriarPrazoCasoUso criarPrazoCasoUso;
    private final ListarPrazosCasoUso listarPrazosCasoUso;
    private final BuscarPrazoCasoUso buscarPrazoCasoUso;
    private final AtualizarPrazoCasoUso atualizarPrazoCasoUso;
    private final ExcluirPrazoCasoUso excluirPrazoCasoUso;

    public PrazoProcessualControlador(
            CriarPrazoCasoUso criarPrazoCasoUso,
            ListarPrazosCasoUso listarPrazosCasoUso,
            BuscarPrazoCasoUso buscarPrazoCasoUso,
            AtualizarPrazoCasoUso atualizarPrazoCasoUso,
            ExcluirPrazoCasoUso excluirPrazoCasoUso) {
        this.criarPrazoCasoUso = criarPrazoCasoUso;
        this.listarPrazosCasoUso = listarPrazosCasoUso;
        this.buscarPrazoCasoUso = buscarPrazoCasoUso;
        this.atualizarPrazoCasoUso = atualizarPrazoCasoUso;
        this.excluirPrazoCasoUso = excluirPrazoCasoUso;
    }

    @PostMapping
    @Operation(summary = "Cria um novo prazo processual")
    public ResponseEntity<PrazoRespostaDto> criar(@Valid @RequestBody CriarPrazoRequisicao requisicao) {
        PrazoResposta resposta = criarPrazoCasoUso.executar(new CriarPrazoComando(
                requisicao.numeroProcesso(),
                requisicao.descricao(),
                requisicao.dataVencimento()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PrazoRespostaDto.de(resposta));
    }

    @GetMapping
    @Operation(summary = "Lista prazos com paginação e ordenação")
    public Page<PrazoRespostaDto> listar(
            @PageableDefault(size = 10, sort = "dataVencimento", direction = Sort.Direction.ASC)
            Pageable paginacao) {
        return listarPrazosCasoUso.executar(paginacao).map(PrazoRespostaDto::de);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca prazo por identificador")
    public PrazoRespostaDto buscar(@PathVariable UUID id) {
        return PrazoRespostaDto.de(buscarPrazoCasoUso.executar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um prazo existente")
    public PrazoRespostaDto atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarPrazoRequisicao requisicao) {
        PrazoResposta resposta = atualizarPrazoCasoUso.executar(new AtualizarPrazoComando(
                id,
                requisicao.numeroProcesso(),
                requisicao.descricao(),
                requisicao.dataVencimento(),
                requisicao.status(),
                requisicao.versao()));
        return PrazoRespostaDto.de(resposta);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove prazo (soft delete)")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        excluirPrazoCasoUso.executar(id);
        return ResponseEntity.noContent().build();
    }
}
