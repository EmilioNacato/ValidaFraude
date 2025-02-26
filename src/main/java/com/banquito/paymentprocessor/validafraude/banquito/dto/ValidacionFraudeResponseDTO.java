package com.banquito.paymentprocessor.validafraude.banquito.dto;

import lombok.Data;

@Data
public class ValidacionFraudeResponseDTO {
    private boolean esFraude;
    private String codigoRegla;
    private String mensaje;
    private String nivelRiesgo;
} 