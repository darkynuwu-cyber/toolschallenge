package br.com.toolschallenge.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTransacaoTest {

    private static final String DESCRIPTION_AUTORIZADO = "AUTORIZADO";
    private static final String DESCRIPTION_NEGADO = "NEGADO";
    private static final String DESCRIPTION_CANCELADO = "CANCELADO";

    private static final int CODE_AUTORIZADO = 1;
    private static final int CODE_NEGADO = 2;
    private static final int CODE_CANCELADO = 3;

    private static final String LOWERCASE_AUTORIZADO = "autorizado";
    private static final String LOWERCASE_NEGADO = "negado";
    private static final String LOWERCASE_CANCELADO = "cancelado";

    private static final String INVALID_DESCRIPTION = "INVALID";
    private static final int INVALID_CODE = 99;

    private static final String EXPECTED_INVALID_DESCRIPTION_MESSAGE =
            "Invalid transaction status: " + INVALID_DESCRIPTION;
    private static final String EXPECTED_INVALID_CODE_MESSAGE =
            "Invalid transaction status code: " + INVALID_CODE;

    private static final String DISPLAY_GETTERS_AUTORIZADO =
            "AUTORIZADO should expose correct code and description";
    private static final String DISPLAY_GETTERS_NEGADO =
            "NEGADO should expose correct code and description";
    private static final String DISPLAY_GETTERS_CANCELADO =
            "CANCELADO should expose correct code and description";

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
    @DisplayName(DISPLAY_GETTERS_AUTORIZADO)
    void getters_shouldReturnExpectedValues_forAutorizado() {
        StatusTransacao status = StatusTransacao.AUTORIZADO;

        assertEquals(CODE_AUTORIZADO, status.getCodigo());
        assertEquals(DESCRIPTION_AUTORIZADO, status.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_GETTERS_NEGADO)
    void getters_shouldReturnExpectedValues_forNegado() {
        StatusTransacao status = StatusTransacao.NEGADO;

        assertEquals(CODE_NEGADO, status.getCodigo());
        assertEquals(DESCRIPTION_NEGADO, status.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_GETTERS_CANCELADO)
    void getters_shouldReturnExpectedValues_forCancelado() {
        StatusTransacao status = StatusTransacao.CANCELADO;

        assertEquals(CODE_CANCELADO, status.getCodigo());
        assertEquals(DESCRIPTION_CANCELADO, status.getDescricao());
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_VALID)
    void fromValue_shouldReturnCorrectEnum_forValidDescriptions() {
        assertEquals(StatusTransacao.AUTORIZADO,
                StatusTransacao.fromValue(DESCRIPTION_AUTORIZADO));
        assertEquals(StatusTransacao.NEGADO,
                StatusTransacao.fromValue(DESCRIPTION_NEGADO));
        assertEquals(StatusTransacao.CANCELADO,
                StatusTransacao.fromValue(DESCRIPTION_CANCELADO));
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_CASE_INSENSITIVE)
    void fromValue_shouldBeCaseInsensitive() {
        assertEquals(StatusTransacao.AUTORIZADO,
                StatusTransacao.fromValue(LOWERCASE_AUTORIZADO));
        assertEquals(StatusTransacao.NEGADO,
                StatusTransacao.fromValue(LOWERCASE_NEGADO));
        assertEquals(StatusTransacao.CANCELADO,
                StatusTransacao.fromValue(LOWERCASE_CANCELADO));
    }

    @Test
    @DisplayName(DISPLAY_FROM_VALUE_INVALID)
    void fromValue_shouldThrowException_forInvalidDescription() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> StatusTransacao.fromValue(INVALID_DESCRIPTION)
        );

        assertEquals(EXPECTED_INVALID_DESCRIPTION_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName(DISPLAY_FROM_ID_VALID)
    void fromId_shouldReturnCorrectEnum_forValidCodes() {
        assertEquals(StatusTransacao.AUTORIZADO,
                StatusTransacao.fromId(CODE_AUTORIZADO));
        assertEquals(StatusTransacao.NEGADO,
                StatusTransacao.fromId(CODE_NEGADO));
        assertEquals(StatusTransacao.CANCELADO,
                StatusTransacao.fromId(CODE_CANCELADO));
    }

    @Test
    @DisplayName(DISPLAY_FROM_ID_INVALID)
    void fromId_shouldThrowException_forInvalidCode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> StatusTransacao.fromId(INVALID_CODE)
        );

        assertEquals(EXPECTED_INVALID_CODE_MESSAGE, ex.getMessage());
    }
}