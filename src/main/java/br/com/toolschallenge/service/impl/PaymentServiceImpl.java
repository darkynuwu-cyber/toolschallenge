package br.com.toolschallenge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.dto.TransacaoRequestDTO;
import br.com.toolschallenge.entity.TransactionEntity;
import br.com.toolschallenge.enums.StatusTransacao;
import br.com.toolschallenge.exception.DuplicateTransactionIdException;
import br.com.toolschallenge.exception.PaymentNotFoundException;
import br.com.toolschallenge.mapper.TransactionMapper;
import br.com.toolschallenge.repository.TransactionRepository;
import br.com.toolschallenge.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public PagamentoResponseDTO createPayment(PagamentoRequestDTO request) {
    	TransactionEntity entity;
    	TransactionEntity entityResponse;
    	TransacaoRequestDTO transacao = request.getTransacao();
        if (transactionRepository.existsById(transacao.getId())) {
            throw new DuplicateTransactionIdException(transacao.getId());
        }
        entity = transactionMapper.toTransactionEntityRequest(request);
        entityResponse = transactionRepository.save(entity);
        return transactionMapper.toPaymentResponse(entityResponse);
    }
    
    @Override
    public List<PagamentoResponseDTO> listAllPayments() {
    	return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toPaymentResponse)
                .toList();
    }

    @Override
    public PagamentoResponseDTO findPaymentById(String id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return transactionMapper.toPaymentResponse(entity);
    }
    
    @Override
    public PagamentoResponseDTO cancelPayment(String id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (StatusTransacao.CANCELADO.getCodigo().equals(entity.getStatus())) {
            return transactionMapper.toPaymentResponse(entity);
        }

        entity.setStatus(StatusTransacao.CANCELADO.getCodigo());
        entity = transactionRepository.save(entity);

        return transactionMapper.toPaymentResponse(entity);
    }
}