package ai.attus.prazos.aplicacao.dto;

import ai.attus.prazos.dominio.enumeracao.StatusPrazo;

import java.time.LocalDate;

public record CriarPrazoComando(
        String numeroProcesso,
        String descricao,
        LocalDate dataVencimento) {
}
