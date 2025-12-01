-- Table compatible with br.com.toolschallenge.entity.TransactionEntity
CREATE TABLE IF NOT EXISTS tb_transacao (
    id                    VARCHAR(20)  NOT NULL,
    card_number           VARCHAR(20)  NOT NULL,
    amount                NUMERIC(10,2) NOT NULL,
    transaction_date_time TIMESTAMP    NOT NULL,
    merchant              VARCHAR(100) NOT NULL,
    nsu                   VARCHAR(20),
    authorization_code    VARCHAR(20),
    status                INTEGER,
    payment_type          INTEGER,
    installments          INTEGER,
    CONSTRAINT pk_tb_transacao PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS seq_nsu
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;