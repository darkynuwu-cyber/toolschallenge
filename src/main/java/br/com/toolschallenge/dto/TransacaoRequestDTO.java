package br.com.toolschallenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

	@NotBlank(message = "Card number is required")
    private String cartao;

    @NotBlank(message = "Id is required")
    @Pattern(regexp = "\\d+", message = "id must contain only numeric digits")
    @Min(1)
    private String id;

    @NotNull(message = "Description is required")
    @Valid
    private DescricaoRequestDTO descricao;

    @NotNull(message = "payment type is required")
    @Valid
    private FormaPagamentoDTO formaPagamento;
}