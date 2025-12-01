package br.com.toolschallenge.entity;

import br.com.toolschallenge.enums.StatusTransacao;
import br.com.toolschallenge.enums.TipoFormaPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transacao")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "card_number", nullable = false, length = 20)
    private String cardNumber;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "merchant", nullable = false, length = 100)
    private String establishment;

    @Column(name = "nsu", length = 20)
    private String nsu;

    @Column(name = "authorization_code", length = 20)
    private String authorizationCode;

    @Column(name = "status", length = 20)
    private Integer status;

    @Column(name = "payment_type", length = 30)
    private Integer paymentType;

    @Column(name = "installments")
    private Integer installments;
}
