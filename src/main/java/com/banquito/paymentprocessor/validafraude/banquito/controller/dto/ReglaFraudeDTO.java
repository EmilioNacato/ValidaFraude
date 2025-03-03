package com.banquito.paymentprocessor.validafraude.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la gestión de reglas de fraude")
public class ReglaFraudeDTO implements Serializable {
    
    @Schema(description = "Código único de la regla de fraude", example = "RF001")
    private String codReglaFraude;

    @NotBlank(message = "El tipo de regla es requerido")
    @Size(max = 3, message = "El tipo de regla no puede exceder los 3 caracteres")
    @Schema(description = "Tipo de regla (MON: Monto, TRX: Transacciones)", example = "MON")
    private String tipoRegla;

    @Size(max = 200, message = "La descripción no puede exceder los 200 caracteres")
    @Schema(description = "Descripción detallada de la regla", example = "Límite de monto para transacciones diarias por tarjeta")
    private String descripcion;

    @Schema(description = "Estado de la regla (activa/inactiva)")
    private boolean estado;

    @DecimalMin(value = "0.0", message = "El monto límite no puede ser negativo")
    @DecimalMax(value = "999999999.99", message = "El monto límite excede el valor permitido")
    @Schema(description = "Monto límite para la regla", example = "1000.00")
    private BigDecimal montoLimite;

    @Min(value = 0, message = "El número máximo de transacciones no puede ser negativo")
    @Max(value = 99, message = "El número máximo de transacciones no puede exceder 99")
    @Schema(description = "Número máximo de transacciones permitidas", example = "10")
    private Integer maxTransaccionesPorMinuto;

    @Min(value = 0, message = "El periodo de evaluación no puede ser negativo")
    @Max(value = 1440, message = "El periodo de evaluación no puede exceder 24 horas (1440 minutos)")
    @Schema(description = "Periodo de evaluación en minutos", example = "60")
    private Integer periodoEvaluacion;
} 