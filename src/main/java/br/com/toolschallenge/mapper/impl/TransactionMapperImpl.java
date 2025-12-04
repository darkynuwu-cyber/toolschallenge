package br.com.toolschallenge.mapper.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

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
import br.com.toolschallenge.mapper.TransactionMapper;
import br.com.toolschallenge.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionMapperImpl implements TransactionMapper {
	
	private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public PagamentoResponseDTO toPaymentResponse(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        DescricaoResponseDTO descricaoResponse = DescricaoResponseDTO.builder()
                .valor(entity.getAmount())
                .dataHora(entity.getDateTime().format(DATE_TIME_FORMATTER))
                .estabelecimento(entity.getEstablishment())
                .nsu(entity.getNsu())
                .codigoAutorizacao(entity.getAuthorizationCode())
                .status(StatusTransacao.fromId(entity.getStatus()))
                .build();

        FormaPagamentoDTO formaPagamentoDTOResponse = FormaPagamentoDTO.builder()
                .tipo(TipoFormaPagamento.fromId(entity.getPaymentType()))
                .parcelas(entity.getInstallments())
                .build();

        TransacaoResponseDTO transacaoResponse = TransacaoResponseDTO.builder()
                .cartao(entity.getCardNumber())
                .id(entity.getId())
                .descricao(descricaoResponse)
                .formaPagamento(formaPagamentoDTOResponse)
                .build();

        return PagamentoResponseDTO.builder()
                .transacao(transacaoResponse)
                .build();
    }
    
    @Override
    public TransactionEntity toTransactionEntityRequest(PagamentoRequestDTO request) {
        TransacaoRequestDTO transacao = request.getTransacao();
        DescricaoRequestDTO descricao = transacao.getDescricao();
        FormaPagamentoDTO formaPagamento = transacao.getFormaPagamento();

        LocalDateTime dateTime =
                LocalDateTime.parse(descricao.getDataHora(), DATE_TIME_FORMATTER);

        String nsu = generateNsu();
        String authorizationCode = generateAuthorizationCode();
        String installmentsQtd = formaPagamento.getParcelas();

        return TransactionEntity.builder()
                .id(transacao.getId())
                .cardNumber(transacao.getCartao())
                .amount(descricao.getValor())
                .dateTime(dateTime)
                .establishment(descricao.getEstabelecimento())
                .nsu(nsu)
                .authorizationCode(authorizationCode)
                .status(StatusTransacao.AUTORIZADO.getCodigo())
                .paymentType(normalizePayment(installmentsQtd, formaPagamento.getTipo().getCodigo()))
                .installments(installmentsQtd)
                .build();
    }
    
    private Integer normalizePayment(String installmentsQtd, Integer paymentType) {
    	Integer installmentsQtdInt = Integer.valueOf(installmentsQtd);
    	Integer avistaCode = TipoFormaPagamento.AVISTA.getCodigo();
    	if (installmentsQtdInt > 1 && avistaCode.equals(paymentType)) {
            throw new InvalidInstallmentsForPaymentTypeException();
        }
    	return paymentType;
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
