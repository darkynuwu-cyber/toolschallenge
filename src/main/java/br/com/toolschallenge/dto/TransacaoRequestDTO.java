package br.com.toolschallenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoRequestDTO {

    @NotBlank
    private String cartao;

    @NotBlank
    @Pattern(regexp = "\\d+", message = "id must contain only numeric digits")
    private String id;

    @NotNull
    @Valid
    private DescricaoRequestDTO descricao;

    @NotNull
    @Valid
    private FormaPagamentoDTO formaPagamento;
}