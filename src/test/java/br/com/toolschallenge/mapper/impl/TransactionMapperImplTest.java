package br.com.toolschallenge.mapper.impl;

import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_AMOUNT;
import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_CARD_NUMBER;
import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.toolschallenge.dto.DescricaoRequestDTO;
import br.com.toolschallenge.dto.DescricaoResponseDTO;
import br.com.toolschallenge.dto.FormaPagamentoDTO;
import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.dto.TransacaoRequestDTO;
import br.com.toolschallenge.dto.TransacaoResponseDTO;
import br.com.toolschallenge.entity.TransactionEntity;
import br.com.toolschallenge.enums.StatusTransacao;
import br.com.toolschallenge.enums.TipoFormaPagamento;
import br.com.toolschallenge.exception.InvalidInstallmentsForPaymentTypeException;
import br.com.toolschallenge.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionMapperImplTest {

    private static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss";
    private static final String SAMPLE_DATE_TIME = "01/05/2021 18:30:00";
    private static final String SAMPLE_ESTABLISHMENT = "PetShop Mundo cão";
    private static final String SAMPLE_AUTH_CODE = "AUTHCODE1";
    private static final long NEXT_NSU_VALUE = 1L;
    private static final String EXPECTED_NSU = "0000000001";
    private static final int EXPECTED_AUTH_CODE_LENGTH = 9;

    private static final String DISPLAY_TO_PAYMENT_RESPONSE_NULL =
            "toPaymentResponse should return null when entity is null";
    private static final String DISPLAY_TO_PAYMENT_RESPONSE_MAP_FIELDS =
            "toPaymentResponse should map all fields from entity to DTO";
    private static final String DISPLAY_TO_TRANSACTION_ENTITY_MAP_FIELDS =
            "toTransactionEntityRequest should map all fields and generate NSU and authorization code";
    private static final String DISPLAY_TO_TRANSACTION_ENTITY_INVALID_INSTALLMENTS =
            "toTransactionEntityRequest should throw InvalidInstallmentsForPaymentTypeException when installments lower than 2 and payment type is AVISTA";

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionMapperImpl mapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @Test
    @DisplayName(DISPLAY_TO_PAYMENT_RESPONSE_NULL)
    void toPaymentResponse_shouldReturnNull_whenEntityIsNull() {
        PagamentoResponseDTO result = mapper.toPaymentResponse(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName(DISPLAY_TO_PAYMENT_RESPONSE_MAP_FIELDS)
    void toPaymentResponse_shouldMapAllFields() {
        LocalDateTime dateTime = LocalDateTime.parse(SAMPLE_DATE_TIME, formatter);

        TransactionEntity entity = TransactionEntity.builder()
                .id(DEFAULT_TRANSACTION_ID)
                .cardNumber(DEFAULT_CARD_NUMBER)
                .amount(DEFAULT_AMOUNT)
                .dateTime(dateTime)
                .establishment(SAMPLE_ESTABLISHMENT)
                .nsu(EXPECTED_NSU)
                .authorizationCode(SAMPLE_AUTH_CODE)
                .status(StatusTransacao.AUTORIZADO.getCodigo())
                .paymentType(TipoFormaPagamento.PARCELADO_LOJA.getCodigo())
                .installments(3)
                .build();

        PagamentoResponseDTO response = mapper.toPaymentResponse(entity);

        assertThat(response).isNotNull();
        TransacaoResponseDTO transacao = response.getTransacao();
        assertThat(transacao).isNotNull();
        assertThat(transacao.getId()).isEqualTo(entity.getId());
        assertThat(transacao.getCartao()).isEqualTo(entity.getCardNumber());

        DescricaoResponseDTO descricao = transacao.getDescricao();
        assertThat(descricao).isNotNull();
        assertThat(descricao.getValor()).isEqualTo(entity.getAmount());
        assertThat(descricao.getDataHora()).isEqualTo(dateTime.format(formatter));
        assertThat(descricao.getEstabelecimento()).isEqualTo(entity.getEstablishment());
        assertThat(descricao.getNsu()).isEqualTo(entity.getNsu());
        assertThat(descricao.getCodigoAutorizacao()).isEqualTo(entity.getAuthorizationCode());
        assertThat(descricao.getStatus())
                .isEqualTo(StatusTransacao.fromId(entity.getStatus()));

        FormaPagamentoDTO formaPagamento = transacao.getFormaPagamento();
        assertThat(formaPagamento).isNotNull();
        assertThat(formaPagamento.getTipo())
                .isEqualTo(TipoFormaPagamento.fromId(entity.getPaymentType()));
        assertThat(formaPagamento.getParcelas()).isEqualTo(entity.getInstallments());
    }

    @Test
    @DisplayName(DISPLAY_TO_TRANSACTION_ENTITY_MAP_FIELDS)
    void toTransactionEntityRequest_shouldMapFieldsAndGenerateValues() {
        FormaPagamentoDTO formaPagamento = FormaPagamentoDTO.builder()
                .tipo(TipoFormaPagamento.PARCELADO_LOJA)
                .parcelas(3)
                .build();

        DescricaoRequestDTO descricao = DescricaoRequestDTO.builder()
                .valor(DEFAULT_AMOUNT)
                .dataHora(SAMPLE_DATE_TIME)
                .estabelecimento(SAMPLE_ESTABLISHMENT)
                .build();

        TransacaoRequestDTO transacao = TransacaoRequestDTO.builder()
                .id(DEFAULT_TRANSACTION_ID)
                .cartao(DEFAULT_CARD_NUMBER)
                .descricao(descricao)
                .formaPagamento(formaPagamento)
                .build();

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .transacao(transacao)
                .build();

        given(transactionRepository.getNextNsu()).willReturn(NEXT_NSU_VALUE);

        TransactionEntity entity = mapper.toTransactionEntityRequest(request);

        assertNotNull(entity);
        assertEquals(DEFAULT_TRANSACTION_ID, entity.getId());
        assertEquals(DEFAULT_CARD_NUMBER, entity.getCardNumber());
        assertEquals(DEFAULT_AMOUNT, entity.getAmount());
        assertEquals(SAMPLE_ESTABLISHMENT, entity.getEstablishment());

        LocalDateTime expectedDateTime = LocalDateTime.parse(SAMPLE_DATE_TIME, formatter);
        assertEquals(expectedDateTime, entity.getDateTime());

        assertEquals(EXPECTED_NSU, entity.getNsu());
        assertNotNull(entity.getAuthorizationCode());
        assertEquals(EXPECTED_AUTH_CODE_LENGTH, entity.getAuthorizationCode().length());

        assertEquals(StatusTransacao.AUTORIZADO.getCodigo(), entity.getStatus());
        assertEquals(formaPagamento.getTipo().getCodigo(), entity.getPaymentType());
        assertEquals(formaPagamento.getTipo().getCodigo(), entity.getInstallments());

        verify(transactionRepository).getNextNsu();
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_TO_TRANSACTION_ENTITY_INVALID_INSTALLMENTS)
    void toTransactionEntityRequest_shouldThrowInvalidInstallmentsForAvista() {
        FormaPagamentoDTO formaPagamento = FormaPagamentoDTO.builder()
                .tipo(TipoFormaPagamento.AVISTA)
                .parcelas(1)
                .build();

        DescricaoRequestDTO descricao = DescricaoRequestDTO.builder()
                .valor(BigDecimal.TEN)
                .dataHora(SAMPLE_DATE_TIME)
                .estabelecimento(SAMPLE_ESTABLISHMENT)
                .build();

        TransacaoRequestDTO transacao = TransacaoRequestDTO.builder()
                .id(DEFAULT_TRANSACTION_ID)
                .cartao(DEFAULT_CARD_NUMBER)
                .descricao(descricao)
                .formaPagamento(formaPagamento)
                .build();

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .transacao(transacao)
                .build();

        given(transactionRepository.getNextNsu()).willReturn(NEXT_NSU_VALUE);

        assertThrows(InvalidInstallmentsForPaymentTypeException.class,
                () -> mapper.toTransactionEntityRequest(request));

        verify(transactionRepository).getNextNsu();
        verifyNoMoreInteractions(transactionRepository);
    }
}
