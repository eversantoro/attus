package ai.attus.prazos.aplicacao.dto;

import ai.attus.prazos.dominio.enumeracao.StatusPrazo;

import java.time.LocalDate;
import java.util.UUID;

public record AtualizarPrazoComando(
        UUID id,
        String numeroProcesso,
        String descricao,
        LocalDate dataVencimento,
        StatusPrazo status,
        Long versao) {
}
