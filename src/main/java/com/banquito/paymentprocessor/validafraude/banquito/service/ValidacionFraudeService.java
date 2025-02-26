package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ValidacionFraudeService {

    private static final String KEY_REGLAS = "fraude:reglas";
    private static final String KEY_CONTADOR = "fraude:contador:";
    private static final String KEY_TRANSACCIONES = "fraude:transacciones:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ValidacionFraudeResponseDTO validarTransaccion(ValidacionFraudeRequestDTO request) {
        log.info("Iniciando validación de fraude para transacción: {}", request.getNumeroTarjeta());
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();

        try {
            // 1. Validar monto límite
            if (validarMontoLimite(request.getMonto())) {
                return crearRespuestaFraude("Monto excede el límite permitido", "MONTO_LIMITE");
            }

            // 2. Validar frecuencia de transacciones
            if (validarFrecuenciaTransacciones(request.getNumeroTarjeta())) {
                return crearRespuestaFraude("Excede el número de transacciones permitidas por minuto", "FRECUENCIA");
            }

            // 3. Validar patrón de tiempo
            if (validarPatronTiempo(request.getNumeroTarjeta())) {
                return crearRespuestaFraude("Patrón de transacciones sospechoso", "PATRON_TIEMPO");
            }

            // Si pasa todas las validaciones
            registrarTransaccion(request);
            response.setEsFraude(false);
            response.setMensaje("Transacción válida");

        } catch (Exception e) {
            log.error("Error en validación de fraude: {}", e.getMessage());
            response.setEsFraude(true);
            response.setMensaje("Error en validación de fraude");
        }

        return response;
    }

    private boolean validarMontoLimite(BigDecimal monto) {
        ReglaFraudeDTO regla = obtenerRegla("MONTO_LIMITE");
        return regla != null && monto.compareTo(regla.getMontoLimite()) > 0;
    }

    private boolean validarFrecuenciaTransacciones(String numeroTarjeta) {
        String key = KEY_CONTADOR + numeroTarjeta;
        Long contador = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);

        ReglaFraudeDTO regla = obtenerRegla("FRECUENCIA");
        return regla != null && contador > regla.getMaxTransaccionesPorMinuto();
    }

    private boolean validarPatronTiempo(String numeroTarjeta) {
        String key = KEY_TRANSACCIONES + numeroTarjeta;
        
        // Agregar timestamp actual
        redisTemplate.opsForZSet().add(key, LocalDateTime.now().toString(), System.currentTimeMillis());
        
        // Mantener solo últimos 5 minutos
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));
        
        // Contar transacciones en ventana de tiempo
        Long cantidadTransacciones = redisTemplate.opsForZSet().size(key);
        
        ReglaFraudeDTO regla = obtenerRegla("PATRON_TIEMPO");
        return regla != null && cantidadTransacciones > regla.getMaxTransaccionesPorMinuto() * 5;
    }

    private void registrarTransaccion(ValidacionFraudeRequestDTO request) {
        String key = KEY_TRANSACCIONES + request.getNumeroTarjeta();
        redisTemplate.opsForZSet().add(key, request, System.currentTimeMillis());
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    private ReglaFraudeDTO obtenerRegla(String tipo) {
        return (ReglaFraudeDTO) redisTemplate.opsForHash().get(KEY_REGLAS, tipo);
    }

    private ValidacionFraudeResponseDTO crearRespuestaFraude(String mensaje, String codigoRegla) {
        ValidacionFraudeResponseDTO response = new ValidacionFraudeResponseDTO();
        response.setEsFraude(true);
        response.setCodigoRegla(codigoRegla);
        response.setMensaje(mensaje);
        return response;
    }

    // Métodos para administración de reglas
    public void guardarRegla(ReglaFraudeDTO regla) {
        redisTemplate.opsForHash().put(KEY_REGLAS, regla.getTipo(), regla);
    }

    public ReglaFraudeDTO obtenerRegla(ReglaFraudeDTO regla) {
        return (ReglaFraudeDTO) redisTemplate.opsForHash().get(KEY_REGLAS, regla.getTipo());
    }

    public List<ReglaFraudeDTO> obtenerTodasLasReglas() {
        return redisTemplate.opsForHash().values(KEY_REGLAS)
                .stream()
                .map(obj -> (ReglaFraudeDTO) obj)
                .toList();
    }

    public void eliminarRegla(String tipo) {
        redisTemplate.opsForHash().delete(KEY_REGLAS, tipo);
    }
} 