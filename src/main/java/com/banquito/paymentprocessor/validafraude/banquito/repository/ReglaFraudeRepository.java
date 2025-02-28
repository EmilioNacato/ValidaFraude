package com.banquito.paymentprocessor.validafraude.banquito.repository;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ReglaFraudeRepository {

    private static final String KEY_PREFIX = "regla:fraude:";
    private final RedisTemplate<String, ReglaFraudeDTO> redisTemplate;

    @Autowired
    public ReglaFraudeRepository(RedisTemplate<String, ReglaFraudeDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(ReglaFraudeDTO regla) {
        String key = KEY_PREFIX + regla.getCodigo();
        log.info("Guardando regla de fraude en Redis: {}", key);
        redisTemplate.opsForValue().set(key, regla);
        // No se establece tiempo de expiración ya que es persistente
    }

    public ReglaFraudeDTO findById(String codigo) {
        String key = KEY_PREFIX + codigo;
        log.info("Buscando regla de fraude en Redis: {}", key);
        return redisTemplate.opsForValue().get(key);
    }

    public List<ReglaFraudeDTO> findAll() {
        log.info("Obteniendo todas las reglas de fraude");
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(regla -> regla != null)
                .collect(Collectors.toList());
    }

    public void deleteById(String codigo) {
        String key = KEY_PREFIX + codigo;
        log.info("Eliminando regla de fraude de Redis: {}", key);
        redisTemplate.delete(key);
    }
    
    public List<ReglaFraudeDTO> findByEstadoTrue() {
        log.info("Obteniendo reglas de fraude activas");
        return findAll().stream()
                .filter(ReglaFraudeDTO::isEstado)
                .collect(Collectors.toList());
    }
    
    public List<ReglaFraudeDTO> findByTipoReglaAndEstadoTrue(String tipoRegla) {
        log.info("Obteniendo reglas de fraude activas por tipo: {}", tipoRegla);
        return findAll().stream()
                .filter(regla -> regla.isEstado() && tipoRegla.equals(regla.getTipo()))
                .collect(Collectors.toList());
    }
} 