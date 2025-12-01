package br.com.toolschallenge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.toolschallenge.dto.PagamentoRequestDTO;
import br.com.toolschallenge.dto.PagamentoResponseDTO;
import br.com.toolschallenge.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> createPayment(
            @Valid @RequestBody PagamentoRequestDTO request) {

        PagamentoResponseDTO response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/listAllPayments")
    public ResponseEntity<List<PagamentoResponseDTO>> listAllPayments() {
    	List<PagamentoResponseDTO> response = paymentService.listAllPayments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> findPaymentById(@PathVariable("id") String id) {
        PagamentoResponseDTO response = paymentService.findPaymentById(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/estorno")
    public ResponseEntity<PagamentoResponseDTO> cancelPayment(
            @PathVariable("id") String id) {

        PagamentoResponseDTO response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }
}
