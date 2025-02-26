package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ValidaFraudeService {

    @Autowired
    private RedisTemplate<String, ReglaFraudeDTO> redisTemplate;
    
    public ValidacionFraudeResponseDTO validarTransaccion(ValidacionFraudeRequestDTO request) {
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();
        
        try {
            List<ReglaFraudeDTO> reglas = obtenerReglasFraude();
            
            for (ReglaFraudeDTO regla : reglas) {
                if (aplicarReglaFraude(regla, request)) {
                    response.setEsFraude(true);
                    response.setCodigoRegla(regla.getCodigo());
                    response.setMensaje("Transacción marcada como fraude por regla: " + regla.getCodigo());
                    return response;
                }
            }
            
            response.setEsFraude(false);
            response.setMensaje("Transacción válida");
            
        } catch (Exception e) {
            log.error("Error al validar fraude: {}", e.getMessage());
            response.setEsFraude(true);
            response.setMensaje("Error en validación de fraude");
        }
        
        return response;
    }

    private List<ReglaFraudeDTO> obtenerReglasFraude() {
        return redisTemplate.opsForList().range("fraude:reglas", 0, -1);
    }

    private boolean aplicarReglaFraude(ReglaFraudeDTO regla, ValidacionFraudeRequestDTO transaccion) {
        switch (regla.getTipo()) {
            case "MONTO_LIMITE":
                return transaccion.getMonto().compareTo(regla.getMontoLimite()) > 0;
            case "FRECUENCIA":
                return validarFrecuencia(transaccion, regla);
            default:
                return false;
        }
    }

    private boolean validarFrecuencia(ValidacionFraudeRequestDTO transaccion, ReglaFraudeDTO regla) {
        String key = "transaccion:contador:" + transaccion.getNumeroTarjeta();
        Long contador = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        
        return contador > regla.getMaxTransaccionesPorMinuto();
    }
} 