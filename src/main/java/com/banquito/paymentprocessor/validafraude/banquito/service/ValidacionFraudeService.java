package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.TransaccionTemporalDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;
import com.banquito.paymentprocessor.validafraude.banquito.repository.TransaccionTemporalRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ValidacionFraudeService {

    private static final String KEY_CONTADOR = "fraude:contador:";

    private final ReglaFraudeRepository reglaFraudeRepository;
    private final TransaccionTemporalRepository transaccionTemporalRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ValidacionFraudeService(ReglaFraudeRepository reglaFraudeRepository,
                                 TransaccionTemporalRepository transaccionTemporalRepository,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.reglaFraudeRepository = reglaFraudeRepository;
        this.transaccionTemporalRepository = transaccionTemporalRepository;
        this.redisTemplate = redisTemplate;
    }

    public ValidacionFraudeResponseDTO validarTransaccion(ValidacionFraudeRequestDTO request) {
        log.info("Iniciando validación de fraude para transacción: {}", request.getNumeroTarjeta());
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();

        try {
            if (validarMontoLimite(request.getMonto())) {
                return crearRespuestaFraude("Monto excede el límite permitido", "MONTO_LIMITE");
            }

            if (validarFrecuenciaTransacciones(request.getNumeroTarjeta())) {
                return crearRespuestaFraude("Excede el número de transacciones permitidas por minuto", "FRECUENCIA");
            }

            if (validarPatronTiempo(request.getNumeroTarjeta())) {
                return crearRespuestaFraude("Patrón de transacciones sospechoso", "PATRON_TIEMPO");
            }

            response.setEsFraude(false);
            response.setMensaje("Transacción válida");
            response.setCodigoRegla("VALIDA");

        } catch (Exception e) {
            log.error("Error en validación de fraude: {}", e.getMessage(), e);
            response.setEsFraude(true);
            response.setMensaje("Error en validación de fraude: " + e.getMessage());
            response.setCodigoRegla("ERROR");
        }

        return response;
    }

    private boolean validarMontoLimite(BigDecimal monto) {
        List<ReglaFraudeDTO> reglas = reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON");
        if (reglas.isEmpty()) {
            log.warn("No se encontraron reglas de tipo MON para validación de monto");
            return false;
        }
        
        return reglas.stream()
                .anyMatch(regla -> regla.getMontoLimite() != null && 
                          monto.compareTo(regla.getMontoLimite()) > 0);
    }

    private boolean validarFrecuenciaTransacciones(String numeroTarjeta) {
        String key = KEY_CONTADOR + numeroTarjeta;
        Long contador = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);

        List<ReglaFraudeDTO> reglas = reglaFraudeRepository.findByTipoReglaAndEstadoTrue("FRQ");
        if (reglas.isEmpty()) {
            log.warn("No se encontraron reglas de tipo FRQ para validación de frecuencia");
            return false;
        }
        
        // Buscar si alguna regla se cumple
        return reglas.stream()
                .anyMatch(regla -> regla.getMaxTransaccionesPorMinuto() != null && 
                          contador > regla.getMaxTransaccionesPorMinuto());
    }

    private boolean validarPatronTiempo(String numeroTarjeta) {
        List<TransaccionTemporalDTO> transaccionesRecientes = 
                transaccionTemporalRepository.findByNumeroTarjeta(numeroTarjeta);
        
        if (transaccionesRecientes.isEmpty()) {
            log.debug("No hay transacciones recientes para la tarjeta {}", numeroTarjeta);
            return false;
        }
        
        List<ReglaFraudeDTO> reglas = reglaFraudeRepository.findByTipoReglaAndEstadoTrue("PAT");
        if (reglas.isEmpty()) {
            log.warn("No se encontraron reglas de tipo PAT para validación de patrón de tiempo");
            return false;
        }
        
        for (ReglaFraudeDTO regla : reglas) {
            if (regla.getPeriodoEvaluacion() != null && regla.getMaxTransaccionesPorMinuto() != null) {
                LocalDateTime tiempoLimite = LocalDateTime.now().minusMinutes(regla.getPeriodoEvaluacion());
                
                long countRecent = transaccionesRecientes.stream()
                        .filter(t -> t.getFechaTransaccion().isAfter(tiempoLimite))
                        .count();
                
                if (countRecent > regla.getMaxTransaccionesPorMinuto() * regla.getPeriodoEvaluacion()) {
                    log.info("Patrón de tiempo sospechoso detectado: {} transacciones en {} minutos", 
                            countRecent, regla.getPeriodoEvaluacion());
                    return true;
                }
            }
        }
        
        return false;
    }

    private ValidacionFraudeResponseDTO crearRespuestaFraude(String mensaje, String codigoRegla) {
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();
        response.setEsFraude(true);
        response.setCodigoRegla(codigoRegla);
        response.setMensaje(mensaje);
        return response;
    }

    public void guardarRegla(ReglaFraudeDTO reglaDTO) {
        reglaFraudeRepository.save(reglaDTO);
    }

    public List<ReglaFraudeDTO> obtenerTodasLasReglas() {
        return reglaFraudeRepository.findAll();
    }
    
    public List<ReglaFraudeDTO> obtenerReglasActivas() {
        return reglaFraudeRepository.findByEstadoTrue();
    }

    public void eliminarRegla(String codigo) {
        reglaFraudeRepository.deleteById(codigo);
    }
} 