package com.banquito.paymentprocessor.validafraude.banquito.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponseDTO {
    private String codigo;
    private String mensaje;
    private Long timestamp = System.currentTimeMillis();
} 