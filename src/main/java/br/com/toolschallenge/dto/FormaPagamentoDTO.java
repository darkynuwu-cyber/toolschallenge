package br.com.toolschallenge.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import br.com.toolschallenge.enums.TipoFormaPagamento;
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
@JsonPropertyOrder({
    "tipo",
    "parcelas"
})
public class FormaPagamentoDTO {

	@NotNull(message = "Type is required")
    private TipoFormaPagamento tipo;

	@NotBlank(message = "Installments is required")
    @Pattern(regexp = "\\d+", message = "id must contain only numeric digits")
    @Min(1)
    private String parcelas;
}
