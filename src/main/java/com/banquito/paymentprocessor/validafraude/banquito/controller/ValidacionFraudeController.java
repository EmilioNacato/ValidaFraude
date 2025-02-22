package com.banquito.paymentprocessor.validafraude.banquito.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.service.ValidacionFraudeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/validacion")
@Tag(name = "Validación de Fraude", description = "API para validar posible fraude en transacciones")
public class ValidacionFraudeController {
    
    private static final Logger log = LoggerFactory.getLogger(ValidacionFraudeController.class);
    private final ValidacionFraudeService service;

    public ValidacionFraudeController(ValidacionFraudeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Valida una transacción por posible fraude", 
              description = "Analiza una transacción contra las reglas de fraude configuradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la transacción inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ValidacionFraudeResponseDTO> validarTransaccion(
            @Valid @RequestBody ValidacionFraudeRequestDTO request) {
        log.info("Recibida solicitud de validación de fraude para la transacción: {}", 
            request.getCodigoTransaccion());
        
        ValidacionFraudeResponseDTO response = service.validarTransaccion(request);
        
        log.info("Validación de fraude completada para la transacción: {}. Resultado: {}", 
            request.getCodigoTransaccion(), response.getTransaccionValida());
        
        return ResponseEntity.ok(response);
    }
} 