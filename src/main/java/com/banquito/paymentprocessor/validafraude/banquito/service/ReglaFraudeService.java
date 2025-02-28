package com.banquito.paymentprocessor.validafraude.banquito.service;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
public class ReglaFraudeService {

    private final ReglaFraudeRepository reglaFraudeRepository;

    public ReglaFraudeService(ReglaFraudeRepository reglaFraudeRepository) {
        this.reglaFraudeRepository = reglaFraudeRepository;
    }

    public List<ReglaFraudeDTO> obtenerTodas() {
        log.debug("Obteniendo todas las reglas de fraude del sistema");
        return reglaFraudeRepository.findAll();
    }

    public ReglaFraudeDTO buscarPorCodigo(String codigoRegla) {
        log.debug("Buscando regla de fraude por código: {}", codigoRegla);
        ReglaFraudeDTO regla = reglaFraudeRepository.findById(codigoRegla);
        if (regla == null) {
            throw new ReglaFraudeNotFoundException(codigoRegla);
        }
        return regla;
    }

    public void registrarNueva(ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Registrando nueva regla de fraude en el sistema: {}", reglaFraudeDTO);
        reglaFraudeRepository.save(reglaFraudeDTO);
    }

    public void actualizarExistente(String codigoRegla, ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Actualizando regla de fraude existente: {}", codigoRegla);
        ReglaFraudeDTO existente = reglaFraudeRepository.findById(codigoRegla);
        if (existente == null) {
            throw new ReglaFraudeNotFoundException(codigoRegla);
        }
        reglaFraudeDTO.setCodigo(codigoRegla);
        reglaFraudeRepository.save(reglaFraudeDTO);
    }

    public void eliminar(String codigoRegla) {
        log.debug("Eliminando regla de fraude del sistema: {}", codigoRegla);
        ReglaFraudeDTO existente = reglaFraudeRepository.findById(codigoRegla);
        if (existente == null) {
            throw new ReglaFraudeNotFoundException(codigoRegla);
        }
        reglaFraudeRepository.deleteById(codigoRegla);
    }

    public List<ReglaFraudeDTO> obtenerReglasActivas() {
        log.debug("Obteniendo listado de reglas de fraude activas");
        return reglaFraudeRepository.findByEstadoTrue();
    }

    public List<ReglaFraudeDTO> obtenerReglasActivasPorTipo(String tipoRegla) {
        log.debug("Obteniendo listado de reglas de fraude activas por tipo: {}", tipoRegla);
        return reglaFraudeRepository.findByTipoReglaAndEstadoTrue(tipoRegla);
    }
} 