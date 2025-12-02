package br.com.toolschallenge.data;

import br.com.toolschallenge.dto.*;
import br.com.toolschallenge.entity.TransactionEntity;
import br.com.toolschallenge.enums.StatusTransacao;
import br.com.toolschallenge.enums.TipoFormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class PaymentTestDataFactory {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static final String DEFAULT_CARD_NUMBER = "4444********1234";
    public static final String DEFAULT_TRANSACTION_ID = "100023568900001";
    public static final String DEFAULT_DATE_TIME = "01/05/2021 18:30:00";
    public static final String DEFAULT_ESTABLISHMENT = "PetShop Mundo cão";
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("50.00");

    private PaymentTestDataFactory() {
    }

    public static PagamentoRequestDTO createValidPaymentRequest() {
        return PagamentoRequestDTO.builder()
                .transacao(createValidTransacaoRequest())
                .build();
    }

    public static TransacaoRequestDTO createValidTransacaoRequest() {
        return TransacaoRequestDTO.builder()
                .cartao(DEFAULT_CARD_NUMBER)
                .id(DEFAULT_TRANSACTION_ID)
                .descricao(createValidDescricaoRequest())
                .formaPagamento(createAvistaFormaPagamento())
                .build();
    }

    public static DescricaoRequestDTO createValidDescricaoRequest() {
        return DescricaoRequestDTO.builder()
                .valor(DEFAULT_AMOUNT)
                .dataHora(DEFAULT_DATE_TIME)
                .estabelecimento(DEFAULT_ESTABLISHMENT)
                .build();
    }

    public static FormaPagamentoDTO createAvistaFormaPagamento() {
        return FormaPagamentoDTO.builder()
                .tipo(TipoFormaPagamento.AVISTA)
                .parcelas(1)
                .build();
    }

    public static TransactionEntity createAuthorizedTransactionEntity() {
        return TransactionEntity.builder()
                .id(DEFAULT_TRANSACTION_ID)
                .cardNumber(DEFAULT_CARD_NUMBER)
                .amount(DEFAULT_AMOUNT)
                .dateTime(LocalDateTime.parse(DEFAULT_DATE_TIME, DATE_TIME_FORMATTER))
                .establishment(DEFAULT_ESTABLISHMENT)
                .nsu("0000000001")
                .authorizationCode("AUTHCODE123")
                .status(StatusTransacao.AUTORIZADO.getCodigo())
                .paymentType(TipoFormaPagamento.AVISTA.getCodigo())
                .installments(1)
                .build();
    }

    public static TransactionEntity createCanceledTransactionEntity() {
        return TransactionEntity.builder()
                .id(DEFAULT_TRANSACTION_ID)
                .cardNumber(DEFAULT_CARD_NUMBER)
                .amount(DEFAULT_AMOUNT)
                .dateTime(LocalDateTime.parse(DEFAULT_DATE_TIME, DATE_TIME_FORMATTER))
                .establishment(DEFAULT_ESTABLISHMENT)
                .nsu("0000000001")
                .authorizationCode("AUTHCODE123")
                .status(StatusTransacao.CANCELADO.getCodigo())
                .paymentType(TipoFormaPagamento.AVISTA.getCodigo())
                .installments(1)
                .build();
    }

    public static PagamentoResponseDTO createAuthorizedPaymentResponse() {
        return PagamentoResponseDTO.builder()
                .transacao(
                        TransacaoResponseDTO.builder()
                                .cartao(DEFAULT_CARD_NUMBER)
                                .id(DEFAULT_TRANSACTION_ID)
                                .descricao(
                                        DescricaoResponseDTO.builder()
                                                .valor(DEFAULT_AMOUNT)
                                                .dataHora(DEFAULT_DATE_TIME)
                                                .estabelecimento(DEFAULT_ESTABLISHMENT)
                                                .nsu("0000000001")
                                                .codigoAutorizacao("AUTHCODE123")
                                                .status(StatusTransacao.AUTORIZADO)
                                                .build()
                                )
                                .formaPagamento(createAvistaFormaPagamento())
                                .build()
                )
                .build();
    }

    public static PagamentoResponseDTO createCanceledPaymentResponse() {
        return PagamentoResponseDTO.builder()
                .transacao(
                        TransacaoResponseDTO.builder()
                                .cartao(DEFAULT_CARD_NUMBER)
                                .id(DEFAULT_TRANSACTION_ID)
                                .descricao(
                                        DescricaoResponseDTO.builder()
                                                .valor(DEFAULT_AMOUNT)
                                                .dataHora(DEFAULT_DATE_TIME)
                                                .estabelecimento(DEFAULT_ESTABLISHMENT)
                                                .nsu("0000000001")
                                                .codigoAutorizacao("AUTHCODE123")
                                                .status(StatusTransacao.CANCELADO)
                                                .build()
                                )
                                .formaPagamento(createAvistaFormaPagamento())
                                .build()
                )
                .build();
    }
}
