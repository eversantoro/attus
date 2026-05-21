package ai.attus.prazos.apresentacao.dto;

import ai.attus.prazos.aplicacao.dto.PrazoResposta;
import ai.attus.prazos.dominio.enumeracao.StatusPrazo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PrazoRespostaDto(
        UUID id,
        String numeroProcesso,
        String descricao,
        LocalDate dataVencimento,
        StatusPrazo status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        Long versao,
        boolean vencidoOuVenceHoje) {

    public static PrazoRespostaDto de(PrazoResposta resposta) {
        return new PrazoRespostaDto(
                resposta.id(),
                resposta.numeroProcesso(),
                resposta.descricao(),
                resposta.dataVencimento(),
                resposta.status(),
                resposta.dataCriacao(),
                resposta.dataAtualizacao(),
                resposta.versao(),
                resposta.vencidoOuVenceHoje());
    }
}
