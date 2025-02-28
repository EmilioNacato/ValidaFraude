package com.banquito.paymentprocessor.validafraude.banquito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransaccionTemporalDTO {
    
    private Long id;
    private String codTransaccion;
    private String numeroTarjeta;
    private String cvv;
    private String fechaCaducidad;
    private BigDecimal monto;
    private String establecimiento;
    private LocalDateTime fechaTransaccion;
    private String estado;
    private String swiftBanco;
} 