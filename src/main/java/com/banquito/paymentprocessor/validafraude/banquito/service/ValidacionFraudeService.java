package com.banquito.paymentprocessor.validafraude.banquito.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;

@Service
public class ValidacionFraudeService {
    
    private static final Logger log = LoggerFactory.getLogger(ValidacionFraudeService.class);
    private final ReglaFraudeRepository reglaFraudeRepository;

    public ValidacionFraudeService(ReglaFraudeRepository reglaFraudeRepository) {
        this.reglaFraudeRepository = reglaFraudeRepository;
    }

    public ValidacionFraudeResponseDTO validarTransaccion(ValidacionFraudeRequestDTO request) {
        log.info("Iniciando validación de fraude para la transacción: {}", request.getCodigoTransaccion());
        
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();
        response.setTransaccionValida(true);
        
        // Obtener reglas activas
        List<ReglaFraude> reglasActivas = reglaFraudeRepository.findByEstadoTrue();
        
        for (ReglaFraude regla : reglasActivas) {
            if (!validarRegla(regla, request)) {
                log.warn("Regla de fraude violada: {} para la transacción: {}", 
                    regla.getCodReglaFraude(), request.getCodigoTransaccion());
                
                response.setTransaccionValida(false);
                response.setCodReglaFraude(regla.getCodReglaFraude());
                response.setMensaje("Transacción rechazada: " + regla.getDescripcion());
                return response;
            }
        }
        
        log.info("Validación de fraude exitosa para la transacción: {}", request.getCodigoTransaccion());
        response.setMensaje("Transacción válida");
        return response;
    }

    private boolean validarRegla(ReglaFraude regla, ValidacionFraudeRequestDTO request) {
        switch (regla.getTipoRegla()) {
            case "MONTO":
                return validarMonto(regla, request.getMonto());
            case "FRECUENCIA":
                return validarFrecuencia(regla, request.getNumeroTarjeta());
            default:
                log.warn("Tipo de regla no soportado: {}", regla.getTipoRegla());
                return true;
        }
    }

    private boolean validarMonto(ReglaFraude regla, BigDecimal monto) {
        return monto.compareTo(regla.getMontoLimite()) <= 0;
    }

    private boolean validarFrecuencia(ReglaFraude regla, String numeroTarjeta) {
        // TODO: Implementar validación de frecuencia consultando historial de transacciones
        // Por ahora retornamos true
        return true;
    }
} 