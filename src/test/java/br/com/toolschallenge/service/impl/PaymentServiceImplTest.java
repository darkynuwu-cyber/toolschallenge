package br.com.toolschallenge.service.impl;

import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_AMOUNT;
import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_CARD_NUMBER;
import static br.com.toolschallenge.data.PaymentTestDataFactory.DEFAULT_TRANSACTION_ID;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createAuthorizedPaymentResponse;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createAuthorizedTransactionEntity;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createCanceledPaymentResponse;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createCanceledTransactionEntity;
import static br.com.toolschallenge.data.PaymentTestDataFactory.createValidPaymentRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.entity.TransactionEntity;
import br.com.toolschallenge.enums.StatusTransacao;
import br.com.toolschallenge.exception.DuplicateTransactionIdException;
import br.com.toolschallenge.exception.PaymentNotFoundException;
import br.com.toolschallenge.mapper.TransactionMapper;
import br.com.toolschallenge.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String EXPECTED_NSU = "0000000001";

    private static final String DISPLAY_CREATE_PAYMENT_UNIQUE =
            "createPayment should persist transaction and return response when id is unique";
    private static final String DISPLAY_CREATE_PAYMENT_DUPLICATE =
            "createPayment should throw DuplicateTransactionIdException when id already exists";
    private static final String DISPLAY_FIND_PAYMENT_BY_ID_EXISTS =
            "findPaymentById should return mapped response when transaction exists";
    private static final String DISPLAY_FIND_PAYMENT_BY_ID_NOT_FOUND =
            "findPaymentById should throw PaymentNotFoundException when transaction does not exist";
    private static final String DISPLAY_LIST_ALL_PAYMENTS =
            "listAllPayments should map all entities to DTOs";
    private static final String DISPLAY_CANCEL_PAYMENT_AUTHORIZED =
            "cancelPayment should update status to CANCELADO when authorized";
    private static final String DISPLAY_CANCEL_PAYMENT_IDEMPOTENT =
            "cancelPayment should be idempotent when transaction is already CANCELADO";
    private static final String DISPLAY_CANCEL_PAYMENT_NOT_FOUND =
            "cancelPayment should throw PaymentNotFoundException when transaction does not exist";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName(DISPLAY_CREATE_PAYMENT_UNIQUE)
    void createPayment_shouldPersist_whenIdIsUnique() {
        PagamentoRequestDTO request = createValidPaymentRequest();
        TransactionEntity entity = createAuthorizedTransactionEntity();
        PagamentoResponseDTO mappedResponse = createAuthorizedPaymentResponse();

        given(transactionRepository.existsById(DEFAULT_TRANSACTION_ID)).willReturn(false);
        given(transactionMapper.toTransactionEntityRequest(any(PagamentoRequestDTO.class)))
                .willReturn(entity);
        given(transactionRepository.save(any(TransactionEntity.class)))
                .willReturn(entity);
        given(transactionMapper.toPaymentResponse(entity))
                .willReturn(mappedResponse);

        PagamentoResponseDTO response = paymentService.createPayment(request);

        assertNotNull(response);
        assertNotNull(response.getTransacao());
        assertEquals(DEFAULT_TRANSACTION_ID, response.getTransacao().getId());
        assertEquals(DEFAULT_CARD_NUMBER, response.getTransacao().getCartao());
        assertEquals(DEFAULT_AMOUNT, response.getTransacao().getDescricao().getValor());
        assertEquals(StatusTransacao.AUTORIZADO, response.getTransacao().getDescricao().getStatus());
        assertEquals(EXPECTED_NSU, response.getTransacao().getDescricao().getNsu());

        verify(transactionRepository).existsById(DEFAULT_TRANSACTION_ID);
        verify(transactionMapper).toTransactionEntityRequest(any(PagamentoRequestDTO.class));
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(transactionMapper).toPaymentResponse(entity);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_CREATE_PAYMENT_DUPLICATE)
    void createPayment_shouldThrow_whenIdAlreadyExists() {
        PagamentoRequestDTO request = createValidPaymentRequest();

        given(transactionRepository.existsById(DEFAULT_TRANSACTION_ID)).willReturn(true);

        assertThrows(DuplicateTransactionIdException.class,
                () -> paymentService.createPayment(request));

        verify(transactionRepository).existsById(DEFAULT_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_FIND_PAYMENT_BY_ID_EXISTS)
    void findPaymentById_shouldReturnResponse_whenExists() {
        TransactionEntity entity = createAuthorizedTransactionEntity();
        PagamentoResponseDTO mappedResponse = createAuthorizedPaymentResponse();

        given(transactionRepository.findById(DEFAULT_TRANSACTION_ID))
                .willReturn(Optional.of(entity));
        given(transactionMapper.toPaymentResponse(entity))
                .willReturn(mappedResponse);

        PagamentoResponseDTO response = paymentService.findPaymentById(DEFAULT_TRANSACTION_ID);

        assertNotNull(response);
        assertEquals(DEFAULT_TRANSACTION_ID, response.getTransacao().getId());
        assertEquals(StatusTransacao.AUTORIZADO, response.getTransacao().getDescricao().getStatus());

        verify(transactionRepository).findById(DEFAULT_TRANSACTION_ID);
        verify(transactionMapper).toPaymentResponse(entity);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_FIND_PAYMENT_BY_ID_NOT_FOUND)
    void findPaymentById_shouldThrow_whenNotFound() {
        given(transactionRepository.findById(DEFAULT_TRANSACTION_ID))
                .willReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.findPaymentById(DEFAULT_TRANSACTION_ID));

        verify(transactionRepository).findById(DEFAULT_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_LIST_ALL_PAYMENTS)
    void listAllPayments_shouldReturnList() {
        TransactionEntity e1 = createAuthorizedTransactionEntity();
        TransactionEntity e2 = createCanceledTransactionEntity();

        PagamentoResponseDTO r1 = createAuthorizedPaymentResponse();
        PagamentoResponseDTO r2 = createCanceledPaymentResponse();

        given(transactionRepository.findAll()).willReturn(List.of(e1, e2));
        given(transactionMapper.toPaymentResponse(e1)).willReturn(r1);
        given(transactionMapper.toPaymentResponse(e2)).willReturn(r2);

        List<PagamentoResponseDTO> result = paymentService.listAllPayments();

        assertEquals(2, result.size());
        assertEquals(StatusTransacao.AUTORIZADO, result.get(0).getTransacao().getDescricao().getStatus());
        assertEquals(StatusTransacao.CANCELADO, result.get(1).getTransacao().getDescricao().getStatus());

        verify(transactionRepository).findAll();
        verify(transactionMapper).toPaymentResponse(e1);
        verify(transactionMapper).toPaymentResponse(e2);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_CANCEL_PAYMENT_AUTHORIZED)
    void cancelPayment_shouldUpdateStatusToCanceled() {
        TransactionEntity authorized = createAuthorizedTransactionEntity();
        PagamentoResponseDTO canceledResponse = createCanceledPaymentResponse();

        given(transactionRepository.findById(DEFAULT_TRANSACTION_ID))
                .willReturn(Optional.of(authorized));
        given(transactionRepository.save(any(TransactionEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(transactionMapper.toPaymentResponse(any(TransactionEntity.class)))
                .willReturn(canceledResponse);

        PagamentoResponseDTO response = paymentService.cancelPayment(DEFAULT_TRANSACTION_ID);

        assertEquals(StatusTransacao.CANCELADO, response.getTransacao().getDescricao().getStatus());

        verify(transactionRepository).findById(DEFAULT_TRANSACTION_ID);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(transactionMapper).toPaymentResponse(any(TransactionEntity.class));
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName(DISPLAY_CANCEL_PAYMENT_IDEMPOTENT)
    void cancelPayment_shouldBeIdempotent_whenAlreadyCanceled() {
        TransactionEntity canceled = createCanceledTransactionEntity();
        PagamentoResponseDTO canceledResponse = createCanceledPaymentResponse();

        given(transactionRepository.findById(DEFAULT_TRANSACTION_ID))
                .willReturn(Optional.of(canceled));
        given(transactionMapper.toPaymentResponse(canceled))
                .willReturn(canceledResponse);

        PagamentoResponseDTO response = paymentService.cancelPayment(DEFAULT_TRANSACTION_ID);

        assertEquals(StatusTransacao.CANCELADO, response.getTransacao().getDescricao().getStatus());

        verify(transactionRepository).findById(DEFAULT_TRANSACTION_ID);
        verify(transactionMapper).toPaymentResponse(canceled);
        verifyNoMoreInteractions(transactionRepository);
    }
    
    @Test
    @DisplayName(DISPLAY_CANCEL_PAYMENT_NOT_FOUND)
    void cancelPayment_shouldThrow_whenNotFound() {

        given(transactionRepository.findById(DEFAULT_TRANSACTION_ID))
                .willReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.cancelPayment(DEFAULT_TRANSACTION_ID));

        verify(transactionRepository).findById(DEFAULT_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepository);
    }

}
