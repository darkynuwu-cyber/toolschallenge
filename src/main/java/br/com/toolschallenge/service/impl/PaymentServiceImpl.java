package br.com.toolschallenge.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
import br.com.toolschallenge.exception.DuplicateTransactionIdException;
import br.com.toolschallenge.exception.PaymentNotFoundException;
import br.com.toolschallenge.repository.TransactionRepository;
import br.com.toolschallenge.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final TransactionRepository transactionRepository;

    @Override
    public PagamentoResponseDTO createPayment(PagamentoRequestDTO request) {
    	TransacaoRequestDTO transacao = request.getTransacao();
        if (transactionRepository.existsById(transacao.getId())) {
            throw new DuplicateTransactionIdException(transacao.getId());
        }
        TransactionEntity entity = buildEntityFromRequest(request);
        entity = transactionRepository.save(entity);
        return buildResponseFromEntity(entity);
    }

    @Override
    public PagamentoResponseDTO findPaymentById(String id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return buildResponseFromEntity(entity);
    }
    
    @Override
    public List<PagamentoResponseDTO> listAllPayments() {
    	return transactionRepository.findAll()
                .stream()
                .map(this::buildResponseFromEntity)
                .toList();
    }
    
    @Override
    public PagamentoResponseDTO cancelPayment(String id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (StatusTransacao.CANCELADO.getCodigo().equals(entity.getStatus())) {
            return buildResponseFromEntity(entity);
        }

        entity.setStatus(StatusTransacao.CANCELADO.getCodigo());
        entity = transactionRepository.save(entity);

        return buildResponseFromEntity(entity);
    }

    private TransactionEntity buildEntityFromRequest(PagamentoRequestDTO request) {
        TransacaoRequestDTO transacao = request.getTransacao();
        DescricaoRequestDTO descricao = transacao.getDescricao();
        FormaPagamentoDTO formaPagamento = transacao.getFormaPagamento();

        LocalDateTime dateTime =
                LocalDateTime.parse(descricao.getDataHora(), DATE_TIME_FORMATTER);

        String nsu = generateNsu();
        String authorizationCode = generateAuthorizationCode();

        return TransactionEntity.builder()
                .id(transacao.getId())
                .cardNumber(transacao.getCartao())
                .amount(descricao.getValor())
                .dateTime(dateTime)
                .establishment(descricao.getEstabelecimento())
                .nsu(nsu)
                .authorizationCode(authorizationCode)
                .status(StatusTransacao.AUTORIZADO.getCodigo())
                .paymentType(formaPagamento.getTipo().getCodigo())
                .installments(formaPagamento.getParcelas())
                .build();
    }

    private PagamentoResponseDTO buildResponseFromEntity(TransactionEntity entity) {

        DescricaoResponseDTO descricaoResponse = DescricaoResponseDTO.builder()
                .valor(entity.getAmount())
                .dataHora(entity.getDateTime().format(DATE_TIME_FORMATTER))
                .estabelecimento(entity.getEstablishment())
                .nsu(entity.getNsu())
                .codigoAutorizacao(entity.getAuthorizationCode())
                .status(StatusTransacao.fromId(entity.getStatus()))
                .build();

        FormaPagamentoDTO formaPagamentoDTO = FormaPagamentoDTO.builder()
                .tipo(TipoFormaPagamento.fromId(entity.getPaymentType()))
                .parcelas(entity.getInstallments())
                .build();

        TransacaoResponseDTO transacaoResponse = TransacaoResponseDTO.builder()
                .cartao(entity.getCardNumber())
                .id(entity.getId())
                .descricao(descricaoResponse)
                .formaPagamento(formaPagamentoDTO)
                .build();

        return PagamentoResponseDTO.builder()
                .transacao(transacaoResponse)
                .build();
    }

    private String generateNsu() {
        Long next = transactionRepository.getNextNsu();
        return String.format("%010d", next);
    }

    private String generateAuthorizationCode() {
        return UUID.randomUUID().toString().replace("-", "")
                .substring(0, 9);
    }
}