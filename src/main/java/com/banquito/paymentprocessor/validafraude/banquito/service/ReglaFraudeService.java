package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Optional;

import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;

@Service
@Slf4j
public class ReglaFraudeService {

    private static final String KEY_REGLAS = "fraude:reglas";
    private final RedisTemplate<String, ReglaFraudeDTO> redisTemplate;
    private final ReglaFraudeRepository reglaFraudeRepository;

    public ReglaFraudeService(RedisTemplate<String, ReglaFraudeDTO> redisTemplate, ReglaFraudeRepository reglaFraudeRepository) {
        this.redisTemplate = redisTemplate;
        this.reglaFraudeRepository = reglaFraudeRepository;
    }

    public void guardarRegla(ReglaFraudeDTO regla) {
        log.info("Guardando regla de fraude: {}", regla.getCodigo());
        redisTemplate.opsForHash().put(KEY_REGLAS, regla.getCodigo(), regla);
    }

    public ReglaFraudeDTO obtenerRegla(String codigo) {
        log.info("Obteniendo regla de fraude: {}", codigo);
        return (ReglaFraudeDTO) redisTemplate.opsForHash().get(KEY_REGLAS, codigo);
    }

    public List<ReglaFraudeDTO> obtenerTodasLasReglas() {
        log.info("Obteniendo todas las reglas de fraude");
        return redisTemplate.opsForHash().values(KEY_REGLAS)
                .stream()
                .map(obj -> (ReglaFraudeDTO) obj)
                .collect(Collectors.toList());
    }

    public void eliminarRegla(String codigo) {
        log.info("Eliminando regla de fraude: {}", codigo);
        redisTemplate.opsForHash().delete(KEY_REGLAS, codigo);
    }

    public List<ReglaFraude> obtenerTodas() {
        log.debug("Obteniendo todas las reglas de fraude del sistema");
        List<ReglaFraude> reglas = new ArrayList<>();
        reglaFraudeRepository.findAll().forEach(reglas::add);
        return reglas;
    }

    public ReglaFraude buscarPorCodigo(String codigoRegla) {
        log.debug("Buscando regla de fraude por código: {}", codigoRegla);
        return reglaFraudeRepository.findById(codigoRegla)
                .orElseThrow(() -> new ReglaFraudeNotFoundException(codigoRegla));
    }

    public ReglaFraude registrarNueva(ReglaFraude reglaFraude) {
        log.debug("Registrando nueva regla de fraude en el sistema: {}", reglaFraude);
        return reglaFraudeRepository.save(reglaFraude);
    }

    public ReglaFraude actualizarExistente(String codigoRegla, ReglaFraude reglaFraude) {
        log.debug("Actualizando regla de fraude existente: {}", codigoRegla);
        if (!reglaFraudeRepository.existsById(codigoRegla)) {
            throw new ReglaFraudeNotFoundException(codigoRegla);
        }
        reglaFraude.setCodReglaFraude(codigoRegla);
        return reglaFraudeRepository.save(reglaFraude);
    }

    public void eliminar(String codigoRegla) {
        log.debug("Eliminando regla de fraude del sistema: {}", codigoRegla);
        if (!reglaFraudeRepository.existsById(codigoRegla)) {
            throw new ReglaFraudeNotFoundException(codigoRegla);
        }
        reglaFraudeRepository.deleteById(codigoRegla);
    }

    public List<ReglaFraude> obtenerReglasActivas() {
        log.debug("Obteniendo listado de reglas de fraude activas");
        return reglaFraudeRepository.findByEstadoTrue();
    }

    public List<ReglaFraude> obtenerReglasActivasPorTipo(String tipoRegla) {
        log.debug("Obteniendo listado de reglas de fraude activas por tipo: {}", tipoRegla);
        return reglaFraudeRepository.findByTipoReglaAndEstadoTrue(tipoRegla);
    }
} 