package com.banquito.paymentprocessor.validafraude.banquito.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ErrorResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(ReglaFraudeNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleReglaFraudeNotFoundException(ReglaFraudeNotFoundException ex) {
        log.error("Regla de fraude no encontrada: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setCodigo("NOT_FOUND");
        errorResponse.setMensaje(ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        log.error("Error no manejado: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setCodigo("INTERNAL_ERROR");
        errorResponse.setMensaje("Error interno del servidor: " + ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 