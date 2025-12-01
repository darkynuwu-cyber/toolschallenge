package br.com.toolschallenge.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoFormaPagamento {

    AVISTA(1, "AVISTA"),
    PARCELADO_LOJA(2, "PARCELADO LOJA"),
    PARCELADO_EMISSOR(3, "PARCELADO EMISSOR");

    private final Integer codigo;
    private final String descricao;

    TipoFormaPagamento(Integer codigo, String descricao) {
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
    public static TipoFormaPagamento fromValue(String value) {
        for (TipoFormaPagamento tipo : values()) {
            if (tipo.descricao.equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Invalid payment method type: " + value);
    }

    public static TipoFormaPagamento fromId(Integer codigo) {
        for (TipoFormaPagamento tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Invalid payment method type code: " + codigo);
    }
}
