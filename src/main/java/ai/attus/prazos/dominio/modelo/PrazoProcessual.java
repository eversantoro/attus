package ai.attus.prazos.dominio.modelo;

import ai.attus.prazos.dominio.enumeracao.StatusPrazo;
import ai.attus.prazos.dominio.excecao.RegraNegocioException;
import ai.attus.prazos.dominio.servico.ValidadorNumeroCnj;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PrazoProcessual {

    private static final int DESCRICAO_MIN = 10;
    private static final int DESCRICAO_MAX = 255;

    private UUID id;
    private String numeroProcesso;
    private String descricao;
    private LocalDate dataVencimento;
    private StatusPrazo status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Long versao;
    private boolean excluido;
    private LocalDateTime dataExclusao;

    private PrazoProcessual() {
    }

    public static PrazoProcessual criar(
            String numeroProcesso,
            String descricao,
            LocalDate dataVencimento) {
        PrazoProcessual prazo = new PrazoProcessual();
        prazo.id = UUID.randomUUID();
        prazo.aplicarDados(numeroProcesso, descricao, dataVencimento);
        prazo.status = StatusPrazo.PENDENTE;
        prazo.versao = 0L;
        prazo.excluido = false;
        LocalDateTime agora = LocalDateTime.now();
        prazo.dataCriacao = agora;
        prazo.dataAtualizacao = agora;
        return prazo;
    }

    public static PrazoProcessual reconstituir(
            UUID id,
            String numeroProcesso,
            String descricao,
            LocalDate dataVencimento,
            StatusPrazo status,
            LocalDateTime dataCriacao,
            LocalDateTime dataAtualizacao,
            Long versao,
            boolean excluido,
            LocalDateTime dataExclusao) {
        PrazoProcessual prazo = new PrazoProcessual();
        prazo.id = id;
        prazo.numeroProcesso = numeroProcesso;
        prazo.descricao = descricao;
        prazo.dataVencimento = dataVencimento;
        prazo.status = status;
        prazo.dataCriacao = dataCriacao;
        prazo.dataAtualizacao = dataAtualizacao;
        prazo.versao = versao;
        prazo.excluido = excluido;
        prazo.dataExclusao = dataExclusao;
        return prazo;
    }

    public void atualizar(
            String numeroProcesso,
            String descricao,
            LocalDate dataVencimento,
            StatusPrazo status) {
        garantirNaoExcluido();
        aplicarDados(numeroProcesso, descricao, dataVencimento);
        if (status != null) {
            this.status = status;
        }
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void excluirLogicamente() {
        garantirNaoExcluido();
        this.excluido = true;
        this.dataExclusao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void aplicarDados(String numeroProcesso, String descricao, LocalDate dataVencimento) {
        validarNumeroProcesso(numeroProcesso);
        validarDescricao(descricao);
        validarDataVencimento(dataVencimento);
        this.numeroProcesso = numeroProcesso.trim();
        this.descricao = descricao.trim();
        this.dataVencimento = dataVencimento;
    }

    private void garantirNaoExcluido() {
        if (excluido) {
            throw new RegraNegocioException("Prazo processual já foi excluído.");
        }
    }

    public static void validarNumeroProcesso(String numeroProcesso) {
        if (!ValidadorNumeroCnj.ehValido(numeroProcesso)) {
            throw new RegraNegocioException(
                    "Número do processo inválido. Utilize o formato CNJ: NNNNNNN-DD.AAAA.J.TR.OOOO");
        }
    }

    public static void validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().length() < DESCRICAO_MIN) {
            throw new RegraNegocioException(
                    "Descrição deve possuir no mínimo " + DESCRICAO_MIN + " caracteres.");
        }
        if (descricao.trim().length() > DESCRICAO_MAX) {
            throw new RegraNegocioException(
                    "Descrição deve possuir no máximo " + DESCRICAO_MAX + " caracteres.");
        }
    }

    public static void validarDataVencimento(LocalDate dataVencimento) {
        if (dataVencimento == null) {
            throw new RegraNegocioException("Data de vencimento é obrigatória.");
        }
        if (dataVencimento.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Data de vencimento não pode ser no passado.");
        }
    }

    public boolean estaVencidoOuVenceHoje() {
        return dataVencimento != null && !dataVencimento.isAfter(LocalDate.now());
    }

    public UUID getId() {
        return id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusPrazo getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public Long getVersao() {
        return versao;
    }

    public boolean isExcluido() {
        return excluido;
    }

    public LocalDateTime getDataExclusao() {
        return dataExclusao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrazoProcessual that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
