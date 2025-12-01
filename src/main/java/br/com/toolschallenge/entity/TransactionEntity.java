package br.com.toolschallenge.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
