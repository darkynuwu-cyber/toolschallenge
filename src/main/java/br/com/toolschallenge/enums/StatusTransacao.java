package br.com.toolschallenge.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusTransacao {

    AUTORIZADO(1, "AUTORIZADO"),
    NEGADO(2, "NEGADO"),
    CANCELADO(3, "CANCELADO");

    private final Integer codigo;
    private final String descricao;

    StatusTransacao(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() {
        return codigo;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static StatusTransacao fromValue(String value) {
        for (StatusTransacao status : values()) {
            if (status.descricao.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid transaction status: " + value);
    }

    public static StatusTransacao fromId(Integer codigo) {
        for (StatusTransacao status : values()) {
            if (status.codigo.equals(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid transaction status code: " + codigo);
    }
}

