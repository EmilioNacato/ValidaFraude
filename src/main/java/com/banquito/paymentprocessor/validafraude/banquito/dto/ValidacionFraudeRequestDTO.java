package com.banquito.paymentprocessor.validafraude.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ValidacionFraudeRequestDTO {
    private String numeroTarjeta;
    private BigDecimal monto;
    private String codigoComercio;
    private String swiftBanco;
} 