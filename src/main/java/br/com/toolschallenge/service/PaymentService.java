package br.com.toolschallenge.service;

import java.util.List;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;

public interface PaymentService {

    PagamentoResponseDTO createPayment(PagamentoRequestDTO request);

    PagamentoResponseDTO findPaymentById(String id);
    
    List<PagamentoResponseDTO> listAllPayments();
    
    PagamentoResponseDTO cancelPayment(String id);
}