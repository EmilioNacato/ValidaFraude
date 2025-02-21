package com.banquito.paymentprocessor.validafraude.banquito.service;

import org.springframework.stereotype.Service;

import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReglaFraudeService {

    private final ReglaFraudeRepository reglaFraudeRepository;

    public ReglaFraudeService(ReglaFraudeRepository reglaFraudeRepository) {
        this.reglaFraudeRepository = reglaFraudeRepository;
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