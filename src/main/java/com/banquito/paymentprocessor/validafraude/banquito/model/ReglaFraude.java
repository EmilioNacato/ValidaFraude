package com.banquito.paymentprocessor.validafraude.banquito.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "REGLA_FRAUDE")
public class ReglaFraude {
    
    @Id
    @Column(name = "COD_REGLA_FRAUDE", length = 10)
    private String codReglaFraude;
    
    @Column(name = "TIPO_REGLA", length = 3, nullable = false)
    private String tipoRegla;
    
    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;
    
    @Column(name = "ESTADO", nullable = false)
    private boolean estado;
    
    @Column(name = "MONTO_LIMITE", precision = 18, scale = 2)
    private BigDecimal montoLimite;
    
    @Column(name = "MAX_TRANSACCIONES")
    private Integer maxTransaccionesPorMinuto;
    
    @Column(name = "PERIODO_EVALUACION")
    private Integer periodoEvaluacion;
} 