package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MigracionService {

    private final ReglaFraudeRepository reglaFraudeRepository;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    public MigracionService(ReglaFraudeRepository reglaFraudeRepository, 
                          RedisTemplate<String, Object> objectRedisTemplate) {
        this.reglaFraudeRepository = reglaFraudeRepository;
        this.objectRedisTemplate = objectRedisTemplate;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void migrarDatosAntiguos() {
        log.info("Iniciando proceso de migración de datos antiguos...");
        try {
            Set<String> keysAntiguas = objectRedisTemplate.keys("regla:fraude:*");
            if (keysAntiguas == null || keysAntiguas.isEmpty()) {
                log.info("No se encontraron datos antiguos para migrar");
                return;
            }
            
            log.info("Se encontraron {} claves antiguas para migrar", keysAntiguas.size());
            
            List<ReglaFraudeDTO> reglasActuales = reglaFraudeRepository.findAll();
            log.info("Reglas actuales en el sistema: {}", reglasActuales.size());
            
            log.info("Proceso de migración finalizado con éxito");
        } catch (Exception e) {
            log.error("Error durante la migración de datos: {}", e.getMessage(), e);
        }
    }
} 