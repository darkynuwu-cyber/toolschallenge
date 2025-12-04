package br.com.toolschallenge.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescricaoRequestDTO {

	@NotNull(message = "Amount is required")
    @DecimalMin("0.01")
	@Digits(integer = 8, fraction = 2, message = "Amount must have at most 10 digits with 2 decimal places")
    private BigDecimal valor;

	@NotBlank(message = "Date is required")
    @Pattern(
        regexp = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}",
        message = "dataHora must be in the format dd/MM/yyyy HH:mm:ss"
    )
    private String dataHora;

    @NotBlank(message = "Establishment name is required")
    @Size(max = 100, message = "Establishment name must be at most 100 characters long")
    private String estabelecimento;
}