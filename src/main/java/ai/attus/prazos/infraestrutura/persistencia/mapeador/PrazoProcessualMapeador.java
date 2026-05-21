package ai.attus.prazos.infraestrutura.persistencia.mapeador;

import ai.attus.prazos.dominio.modelo.PrazoProcessual;
import ai.attus.prazos.infraestrutura.persistencia.entidade.PrazoProcessualEntidade;

public final class PrazoProcessualMapeador {

    private PrazoProcessualMapeador() {
    }

    public static PrazoProcessual paraDominio(PrazoProcessualEntidade entidade) {
        return PrazoProcessual.reconstituir(
                entidade.getId(),
                entidade.getNumeroProcesso(),
                entidade.getDescricao(),
                entidade.getDataVencimento(),
                entidade.getStatus(),
                entidade.getDataCriacao(),
                entidade.getDataAtualizacao(),
                entidade.getVersao(),
                entidade.isExcluido(),
                entidade.getDataExclusao());
    }

    public static PrazoProcessualEntidade paraEntidade(PrazoProcessual dominio) {
        PrazoProcessualEntidade entidade = new PrazoProcessualEntidade();
        entidade.setId(dominio.getId());
        entidade.setNumeroProcesso(dominio.getNumeroProcesso());
        entidade.setDescricao(dominio.getDescricao());
        entidade.setDataVencimento(dominio.getDataVencimento());
        entidade.setStatus(dominio.getStatus());
        entidade.setDataCriacao(dominio.getDataCriacao());
        entidade.setDataAtualizacao(dominio.getDataAtualizacao());
        entidade.setVersao(dominio.getVersao());
        entidade.setExcluido(dominio.isExcluido());
        entidade.setDataExclusao(dominio.getDataExclusao());
        return entidade;
    }

    public static void atualizarEntidade(PrazoProcessualEntidade entidade, PrazoProcessual dominio) {
        entidade.setNumeroProcesso(dominio.getNumeroProcesso());
        entidade.setDescricao(dominio.getDescricao());
        entidade.setDataVencimento(dominio.getDataVencimento());
        entidade.setStatus(dominio.getStatus());
        entidade.setDataAtualizacao(dominio.getDataAtualizacao());
        entidade.setExcluido(dominio.isExcluido());
        entidade.setDataExclusao(dominio.getDataExclusao());
    }
}
