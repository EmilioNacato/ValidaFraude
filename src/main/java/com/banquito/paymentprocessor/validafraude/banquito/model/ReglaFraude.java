package com.banquito.paymentprocessor.validafraude.banquito.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RedisHash("regla_fraude")
@Schema(description = "Modelo para las reglas de fraude")
public class ReglaFraude {
    
    @Id
    @Schema(description = "Código único de la regla de fraude", example = "REGLA001")
    private String codReglaFraude;
    
    @Indexed
    @Schema(description = "Nombre de la regla", example = "Límite de monto por transacción")
    private String nombreRegla;
    
    @Schema(description = "Descripción detallada de la regla", example = "Valida que el monto de la transacción no exceda el límite establecido")
    private String descripcion;
    
    @Schema(description = "Tipo de regla (MONTO, FRECUENCIA, etc)", example = "MONTO")
    private String tipoRegla;
    
    @Schema(description = "Monto límite para la regla", example = "1000.00")
    private BigDecimal montoLimite;
    
    @Schema(description = "Máximo número de transacciones permitidas", example = "5")
    private Integer maxTransacciones;
    
    @Schema(description = "Período de tiempo en minutos para la validación", example = "1440")
    private Integer periodoMinutos;
    
    @Schema(description = "Estado de la regla (activa/inactiva)", example = "true")
    private Boolean estado;
} 