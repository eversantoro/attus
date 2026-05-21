package ai.attus.prazos.apresentacao.dto;

import ai.attus.prazos.dominio.enumeracao.StatusPrazo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Requisição para atualização de prazo processual")
public record AtualizarPrazoRequisicao(

        @NotBlank(message = "Número do processo é obrigatório")
        @Schema(example = "0001234-56.2024.8.26.0100")
        String numeroProcesso,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 255, message = "Descrição deve ter entre 10 e 255 caracteres")
        String descricao,

        @NotNull(message = "Data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @NotNull(message = "Status é obrigatório")
        StatusPrazo status,

        @Schema(description = "Versão para controle de concorrência otimista")
        Long versao) {
}
