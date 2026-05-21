CREATE TABLE prazo_processual (
    id              UUID PRIMARY KEY,
    numero_processo VARCHAR(25)  NOT NULL,
    descricao       VARCHAR(255) NOT NULL,
    data_vencimento DATE         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    data_criacao    TIMESTAMP    NOT NULL,
    data_atualizacao TIMESTAMP   NOT NULL,
    versao          BIGINT       NOT NULL DEFAULT 0,
    excluido        BOOLEAN      NOT NULL DEFAULT FALSE,
    data_exclusao   TIMESTAMP
);

CREATE INDEX idx_prazo_data_vencimento ON prazo_processual (data_vencimento) WHERE excluido = FALSE;
CREATE INDEX idx_prazo_numero_processo ON prazo_processual (numero_processo) WHERE excluido = FALSE;
