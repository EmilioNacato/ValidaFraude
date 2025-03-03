package com.banquito.paymentprocessor.validafraude.banquito.repository;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.TransaccionTemporalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Repository
@Slf4j
public class TransaccionTemporalRepository {

    private static final String KEY_PREFIX = "transaccion:temporal:";
    private final RedisTemplate<String, TransaccionTemporalDTO> redisTemplate;

    @Autowired
    public TransaccionTemporalRepository(RedisTemplate<String, TransaccionTemporalDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public TransaccionTemporalDTO findByCodTransaccion(String codTransaccion) {
        String key = KEY_PREFIX + codTransaccion;
        log.info("Buscando transacción temporal en Redis: {}", key);
        return redisTemplate.opsForValue().get(key);
    }

    public List<TransaccionTemporalDTO> findByNumeroTarjeta(String numeroTarjeta) {
        log.info("Buscando transacciones por número de tarjeta: {}", numeroTarjeta);
        // Obtener todas las claves que coinciden con el patrón
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Filtrar por número de tarjeta
        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(transaccion -> transaccion != null && 
                        transaccion.getNumeroTarjeta().equals(numeroTarjeta))
                .collect(Collectors.toList());
    }

    public List<TransaccionTemporalDTO> findRecent(int minutes) {
        log.info("Buscando transacciones de los últimos {} minutos", minutes);
        // Obtener todas las claves que coinciden con el patrón
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Filtrar por tiempo
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(transaccion -> transaccion != null && 
                        transaccion.getFechaTransaccion().isAfter(cutoffTime))
                .collect(Collectors.toList());
    }

    public List<TransaccionTemporalDTO> findAll() {
        log.info("Obteniendo todas las transacciones temporales");
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(transaccion -> transaccion != null)
                .collect(Collectors.toList());
    }
} 