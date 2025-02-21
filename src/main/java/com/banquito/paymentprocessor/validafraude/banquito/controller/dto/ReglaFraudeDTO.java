package com.banquito.paymentprocessor.validafraude.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la gestión de reglas de fraude")
public class ReglaFraudeDTO {

    @Schema(description = "Código único de la regla de fraude", example = "RF001")
    private String codReglaFraude;

    @NotBlank(message = "El nombre de la regla es requerido")
    @Size(max = 50, message = "El nombre de la regla no puede exceder los 50 caracteres")
    @Schema(description = "Nombre de la regla de fraude", example = "Límite de monto diario")
    private String nombreRegla;

    @Size(max = 100, message = "La descripción no puede exceder los 100 caracteres")
    @Schema(description = "Descripción detallada de la regla", example = "Límite de monto para transacciones diarias por tarjeta")
    private String descripcion;

    @NotBlank(message = "El tipo de regla es requerido")
    @Size(max = 3, message = "El tipo de regla no puede exceder los 3 caracteres")
    @Schema(description = "Tipo de regla (MON: Monto, TRX: Transacciones)", example = "MON")
    private String tipoRegla;

    @DecimalMin(value = "0.0", message = "El monto límite no puede ser negativo")
    @DecimalMax(value = "999999999.99", message = "El monto límite excede el valor permitido")
    @Schema(description = "Monto límite para la regla", example = "1000.00")
    private BigDecimal montoLimite;

    @Min(value = 0, message = "El número máximo de transacciones no puede ser negativo")
    @Max(value = 99, message = "El número máximo de transacciones no puede exceder 99")
    @Schema(description = "Número máximo de transacciones permitidas", example = "10")
    private Integer maxTransacciones;

    @Min(value = 0, message = "El período en minutos no puede ser negativo")
    @Max(value = 99, message = "El período en minutos no puede exceder 99")
    @Schema(description = "Período de tiempo en minutos para aplicar la regla", example = "60")
    private Integer periodoMinutos;

    @NotNull(message = "El estado es requerido")
    @Schema(description = "Estado de la regla (activa/inactiva)", example = "true")
    private Boolean estado;
} 