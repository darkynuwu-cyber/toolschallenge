package br.com.toolschallenge.mapper;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.entity.TransactionEntity;

public interface TransactionMapper {

    PagamentoResponseDTO toPaymentResponse(TransactionEntity entity);
    
    TransactionEntity toTransactionEntityRequest(PagamentoRequestDTO request);
}