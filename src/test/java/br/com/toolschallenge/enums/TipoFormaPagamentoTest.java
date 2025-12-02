package br.com.toolschallenge.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TipoFormaPagamentoTest {

    private static final String DESCRIPTION_AVISTA = "AVISTA";
    private static final String DESCRIPTION_PARCELADO_LOJA = "PARCELADO LOJA";
    private static final String DESCRIPTION_PARCELADO_EMISSOR = "PARCELADO EMISSOR";

    private static final int CODE_AVISTA = 1;
    private static final int CODE_PARCELADO_LOJA = 2;
    private static final int CODE_PARCELADO_EMISSOR = 3;

    private static final String LOWERCASE_AVISTA = "avista";
    private static final String LOWERCASE_PARCELADO_LOJA = "parcelado loja";
    private static final String LOWERCASE_PARCELADO_EMISSOR = "parcelado emissor";

    private static final String INVALID_DESCRIPTION = "INVALID";
    private static final int INVALID_CODE = 99;

    private static final String EXPECTED_INVALID_DESCRIPTION_MESSAGE =
            "Invalid payment method type: " + INVALID_DESCRIPTION;
    private static final String EXPECTED_INVALID_CODE_MESSAGE =
            "Invalid payment method type code: " + INVALID_CODE;

    private static final String DISPLAY_GETTERS_AVISTA =
            "AVISTA should expose correct code and description";
    private static final String DISPLAY_GETTERS_PARCELADO_LOJA =
            "PARCELADO_LOJA should expose correct code and description";
    private static final String DISPLAY_GETTERS_PARCELADO_EMISSOR =
            "PARCELADO_EMISSOR should expose correct code and description";

    private static final String DISPLAY_FROM_VALUE_VALID =
            "fromValue should return correct enum for valid descriptions";
    private static final String DISPLAY_FROM_VALUE_CASE_INSENSITIVE =
            "fromValue should ignore case when matching description";
    private static final String DISPLAY_FROM_VALUE_INVALID =
            "fromValue should throw IllegalArgumentException for invalid description";

    private static final String DISPLAY_FROM_ID_VALID =
            "fromId should return correct enum for valid codes";
    private static final String DISPLAY_FROM_ID_INVALID =
            "fromId should throw IllegalArgumentException for invalid code";

    @Test
    @DisplayName(DISPLAY_GETTERS_AVISTA)
    void getters_shouldReturnExpectedValues_forAvista() {
        TipoFormaPagamento tipo = TipoFormaPagamento.AVISTA;

        assertEquals(CODE_AVISTA, tipo.getCodigo());
        assertEquals(DESCRIPTION_AVISTA, tipo.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_GETTERS_PARCELADO_LOJA)
    void getters_shouldReturnExpectedValues_forParceladoLoja() {
        TipoFormaPagamento tipo = TipoFormaPagamento.PARCELADO_LOJA;

        assertEquals(CODE_PARCELADO_LOJA, tipo.getCodigo());
        assertEquals(DESCRIPTION_PARCELADO_LOJA, tipo.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_GETTERS_PARCELADO_EMISSOR)
    void getters_shouldReturnExpectedValues_forParceladoEmissor() {
        TipoFormaPagamento tipo = TipoFormaPagamento.PARCELADO_EMISSOR;

        assertEquals(CODE_PARCELADO_EMISSOR, tipo.getCodigo());
        assertEquals(DESCRIPTION_PARCELADO_EMISSOR, tipo.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_VALID)
    void fromValue_shouldReturnCorrectEnum_forValidDescriptions() {
        assertEquals(TipoFormaPagamento.AVISTA,
                TipoFormaPagamento.fromValue(DESCRIPTION_AVISTA));
        assertEquals(TipoFormaPagamento.PARCELADO_LOJA,
                TipoFormaPagamento.fromValue(DESCRIPTION_PARCELADO_LOJA));
        assertEquals(TipoFormaPagamento.PARCELADO_EMISSOR,
                TipoFormaPagamento.fromValue(DESCRIPTION_PARCELADO_EMISSOR));
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_CASE_INSENSITIVE)
    void fromValue_shouldBeCaseInsensitive() {
        assertEquals(TipoFormaPagamento.AVISTA,
                TipoFormaPagamento.fromValue(LOWERCASE_AVISTA));
        assertEquals(TipoFormaPagamento.PARCELADO_LOJA,
                TipoFormaPagamento.fromValue(LOWERCASE_PARCELADO_LOJA));
        assertEquals(TipoFormaPagamento.PARCELADO_EMISSOR,
                TipoFormaPagamento.fromValue(LOWERCASE_PARCELADO_EMISSOR));
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_INVALID)
    void fromValue_shouldThrowException_forInvalidDescription() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TipoFormaPagamento.fromValue(INVALID_DESCRIPTION)
        );

        assertEquals(EXPECTED_INVALID_DESCRIPTION_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName(DISPLAY_FROM_ID_VALID)
    void fromId_shouldReturnCorrectEnum_forValidCodes() {
        assertEquals(TipoFormaPagamento.AVISTA,
                TipoFormaPagamento.fromId(CODE_AVISTA));
        assertEquals(TipoFormaPagamento.PARCELADO_LOJA,
                TipoFormaPagamento.fromId(CODE_PARCELADO_LOJA));
        assertEquals(TipoFormaPagamento.PARCELADO_EMISSOR,
                TipoFormaPagamento.fromId(CODE_PARCELADO_EMISSOR));
    }

    @Test
    @DisplayName(DISPLAY_FROM_ID_INVALID)
    void fromId_shouldThrowException_forInvalidCode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TipoFormaPagamento.fromId(INVALID_CODE)
        );

        assertEquals(EXPECTED_INVALID_CODE_MESSAGE, ex.getMessage());
    }
}
