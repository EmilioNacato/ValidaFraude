package com.banquito.paymentprocessor.validafraude.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReglaFraudeDTO {
    private String codigo;
    private String tipo;
    private String descripcion;
    private boolean estado;
    private BigDecimal montoLimite;
    private Integer maxTransaccionesPorMinuto;
    private Integer periodoEvaluacion;
} 