package com.banquito.paymentprocessor.validafraude.banquito.controller.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la solicitud de validación de fraude")
public class ValidacionFraudeRequestDTO {
    
    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener solo números")
    @Schema(description = "Número de tarjeta a validar", example = "4532123456789012")
    private String numeroTarjeta;

    @NotNull(message = "El monto es requerido")
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;

    @NotBlank(message = "El código de comercio es requerido")
    @Schema(description = "Código del comercio", example = "COM123")
    private String codigoComercio;

    @NotBlank(message = "El código único es requerido")
    @Schema(description = "Código único de la transacción", example = "TRX123456")
    private String codigoUnico;

    @NotBlank(message = "El tipo de transacción es requerido")
    @Schema(description = "Tipo de transacción (PEN, APR, REC, etc.)", example = "PEN")
    private String tipoTransaccion;
} 