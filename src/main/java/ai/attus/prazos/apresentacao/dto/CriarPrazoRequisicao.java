package ai.attus.prazos.apresentacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Requisição para criação de prazo processual")
public record CriarPrazoRequisicao(

        @NotBlank(message = "Número do processo é obrigatório")
        @Schema(example = "0001234-56.2024.8.26.0100")
        String numeroProcesso,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 255, message = "Descrição deve ter entre 10 e 255 caracteres")
        @Schema(example = "Prazo para apresentação de contestação")
        String descricao,

        @NotNull(message = "Data de vencimento é obrigatória")
        @Schema(example = "2026-06-15")
        LocalDate dataVencimento) {
}
