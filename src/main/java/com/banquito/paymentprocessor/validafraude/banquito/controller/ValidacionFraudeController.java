package com.banquito.paymentprocessor.validafraude.banquito.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.service.ValidacionFraudeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/fraude")
@Tag(name = "Validación de Fraude", 
     description = "API para la validación y detección de posible fraude en transacciones")
public class ValidacionFraudeController {
    
    private final ValidacionFraudeService service;

    public ValidacionFraudeController(ValidacionFraudeService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    @Operation(
        summary = "Valida una transacción por posible fraude", 
        description = "Analiza una transacción contra las reglas de fraude configuradas en el sistema. " +
                     "Verifica patrones sospechosos, límites de montos, frecuencia de transacciones y " +
                     "otros indicadores de posible actividad fraudulenta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validación completada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidacionFraudeResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de la transacción inválidos o incompletos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "422",
            description = "No se pudo procesar la validación de fraude",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ValidacionFraudeResponseDTO> validarTransaccion(
            @Parameter(
                description = "Datos de la transacción a validar", 
                required = true,
                schema = @Schema(implementation = ValidacionFraudeRequestDTO.class)
            )
            @Valid @RequestBody ValidacionFraudeRequestDTO request) {
        
        log.info("Recibida solicitud de validación de fraude para tarjeta: {}", 
            request.getNumeroTarjeta());
        
        ValidacionFraudeResponseDTO response = service.validarTransaccion(request);
        
        log.info("Validación de fraude completada para la transacción: {}. Es fraude: {}", 
            request.getNumeroTarjeta(), response.isEsFraude());
        
        return ResponseEntity.ok(response);
    }
} 