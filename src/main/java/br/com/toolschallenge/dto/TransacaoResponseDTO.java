package br.com.toolschallenge.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "cartao", "id", "descricao", "formaPagamento" })
public class TransacaoResponseDTO {

    @NotBlank
    private String cartao;

    @NotBlank
    private String id;

    @NotNull
    @Valid
    private DescricaoResponseDTO descricao;

    @NotNull
    @Valid
    private FormaPagamentoDTO formaPagamento;
}