package ai.attus.prazos.aplicacao.dto;

import ai.attus.prazos.dominio.enumeracao.StatusPrazo;
import ai.attus.prazos.dominio.modelo.PrazoProcessual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PrazoResposta(
        UUID id,
        String numeroProcesso,
        String descricao,
        LocalDate dataVencimento,
        StatusPrazo status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        Long versao,
        boolean vencidoOuVenceHoje) {

    public static PrazoResposta de(PrazoProcessual prazo) {
        return new PrazoResposta(
                prazo.getId(),
                prazo.getNumeroProcesso(),
                prazo.getDescricao(),
                prazo.getDataVencimento(),
                prazo.getStatus(),
                prazo.getDataCriacao(),
                prazo.getDataAtualizacao(),
                prazo.getVersao(),
                prazo.estaVencidoOuVenceHoje());
    }
}
