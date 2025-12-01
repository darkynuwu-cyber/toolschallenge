package br.com.toolschallenge.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import br.com.toolschallenge.enums.StatusTransacao;
import jakarta.validation.constraints.DecimalMin;
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
    "valor",
    "dataHora",
    "estabelecimento",
    "nsu",
    "codigoAutorizacao",
    "status"
})
public class DescricaoResponseDTO {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;

    @NotBlank
    @Pattern(
        regexp = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}",
        message = "dataHora deve estar no formato dd/MM/yyyy HH:mm:ss"
    )
    private String dataHora;

    @NotBlank
    private String estabelecimento;

    @NotBlank
    private String nsu;

    @NotBlank
    private String codigoAutorizacao;

    @NotNull
    private StatusTransacao status;
}