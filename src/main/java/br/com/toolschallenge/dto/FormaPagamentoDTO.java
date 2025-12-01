package br.com.toolschallenge.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import br.com.toolschallenge.enums.TipoFormaPagamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private TipoFormaPagamento tipo;

    @NotNull
    @Min(1)
    private Integer parcelas;
}
