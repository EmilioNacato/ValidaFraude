package com.banquito.paymentprocessor.validafraude.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.io.Serializable;

@Data
public class ReglaFraudeDTO implements Serializable {
    private String codReglaFraude;
    private String tipoRegla;
    private String descripcion;
    private boolean estado;
    private BigDecimal montoLimite;
    private Integer maxTransaccionesPorMinuto;
    private Integer periodoEvaluacion;
} 