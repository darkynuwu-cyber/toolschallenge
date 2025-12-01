package br.com.toolschallenge.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescricaoRequestDTO {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;

    @NotBlank
    @Pattern(
        regexp = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}",
        message = "dataHora must be in the format dd/MM/yyyy HH:mm:ss"
    )
    private String dataHora;

    @NotBlank
    private String estabelecimento;
}