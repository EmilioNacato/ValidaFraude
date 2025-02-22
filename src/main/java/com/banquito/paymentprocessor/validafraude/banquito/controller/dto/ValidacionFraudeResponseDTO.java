package com.banquito.paymentprocessor.validafraude.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la respuesta de validación de fraude")
public class ValidacionFraudeResponseDTO {
    
    @Schema(description = "Indica si la transacción es válida", example = "true")
    private Boolean transaccionValida;
    
    @Schema(description = "Código de la regla de fraude que se violó", example = "REGLA001")
    private String codReglaFraude;
    
    @Schema(description = "Mensaje informativo del proceso", example = "Transacción válida")
    private String mensaje;
} 