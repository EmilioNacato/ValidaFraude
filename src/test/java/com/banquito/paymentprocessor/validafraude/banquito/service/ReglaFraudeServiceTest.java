package com.banquito.paymentprocessor.validafraude.banquito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReglaFraudeServiceTest {

    @Mock
    private ReglaFraudeRepository reglaFraudeRepository;

    @InjectMocks
    private ReglaFraudeService reglaFraudeService;

    private ReglaFraudeDTO reglaMontoLimite;
    private ReglaFraudeDTO reglaFrecuencia;
    private List<ReglaFraudeDTO> reglasCompletas;

    @BeforeEach
    void setUp() {
        reglaMontoLimite = new ReglaFraudeDTO();
        reglaMontoLimite.setCodReglaFraude("RGL001");
        reglaMontoLimite.setTipoRegla("MON");
        reglaMontoLimite.setDescripcion("Regla de monto límite");
        reglaMontoLimite.setEstado(true);
        reglaMontoLimite.setMontoLimite(new BigDecimal("5000.00"));

        reglaFrecuencia = new ReglaFraudeDTO();
        reglaFrecuencia.setCodReglaFraude("RGL002");
        reglaFrecuencia.setTipoRegla("FRQ");
        reglaFrecuencia.setDescripcion("Regla de frecuencia de transacciones");
        reglaFrecuencia.setEstado(true);
        reglaFrecuencia.setMaxTransaccionesPorMinuto(5);

        reglasCompletas = Arrays.asList(reglaMontoLimite, reglaFrecuencia);
    }

    @Test
    void obtenerTodas_retornaListadoCompleto() {
        when(reglaFraudeRepository.findAll()).thenReturn(reglasCompletas);

        List<ReglaFraudeDTO> resultado = reglaFraudeService.obtenerTodas();

        assertEquals(2, resultado.size());
        assertEquals("RGL001", resultado.get(0).getCodReglaFraude());
        assertEquals("RGL002", resultado.get(1).getCodReglaFraude());
        verify(reglaFraudeRepository, times(1)).findAll();
    }

    @Test
    void buscarPorCodigo_reglaExistente_retornaRegla() {
        when(reglaFraudeRepository.findById("RGL001")).thenReturn(reglaMontoLimite);

        ReglaFraudeDTO resultado = reglaFraudeService.buscarPorCodigo("RGL001");

        assertEquals("RGL001", resultado.getCodReglaFraude());
        assertEquals("MON", resultado.getTipoRegla());
        verify(reglaFraudeRepository, times(1)).findById("RGL001");
    }

    @Test
    void buscarPorCodigo_reglaNoExistente_lanzaExcepcion() {
        when(reglaFraudeRepository.findById("REGLA_INEXISTENTE")).thenReturn(null);

        assertThrows(ReglaFraudeNotFoundException.class, () -> {
            reglaFraudeService.buscarPorCodigo("REGLA_INEXISTENTE");
        });
        verify(reglaFraudeRepository, times(1)).findById("REGLA_INEXISTENTE");
    }

    @Test
    void registrarNueva_guardaReglaCorrectamente() {
        doNothing().when(reglaFraudeRepository).save(reglaMontoLimite);

        reglaFraudeService.registrarNueva(reglaMontoLimite);

        verify(reglaFraudeRepository, times(1)).save(reglaMontoLimite);
    }

    @Test
    void actualizarExistente_reglaExistente_actualizaCorrectamente() {
        when(reglaFraudeRepository.findById("RGL001")).thenReturn(reglaMontoLimite);
        doNothing().when(reglaFraudeRepository).save(any(ReglaFraudeDTO.class));

        ReglaFraudeDTO reglaModificada = new ReglaFraudeDTO();
        reglaModificada.setTipoRegla("MON");
        reglaModificada.setDescripcion("Descripción actualizada");
        reglaModificada.setEstado(false);
        reglaModificada.setMontoLimite(new BigDecimal("10000.00"));

        reglaFraudeService.actualizarExistente("RGL001", reglaModificada);

        verify(reglaFraudeRepository, times(1)).findById("RGL001");
        verify(reglaFraudeRepository, times(1)).save(reglaModificada);
        assertEquals("RGL001", reglaModificada.getCodReglaFraude());
    }

    @Test
    void actualizarExistente_reglaNoExistente_lanzaExcepcion() {
        when(reglaFraudeRepository.findById("REGLA_INEXISTENTE")).thenReturn(null);

        ReglaFraudeDTO reglaModificada = new ReglaFraudeDTO();
        reglaModificada.setDescripcion("Descripción actualizada");

        assertThrows(ReglaFraudeNotFoundException.class, () -> {
            reglaFraudeService.actualizarExistente("REGLA_INEXISTENTE", reglaModificada);
        });
        verify(reglaFraudeRepository, times(1)).findById("REGLA_INEXISTENTE");
    }

    @Test
    void eliminar_reglaExistente_eliminaCorrectamente() {
        when(reglaFraudeRepository.findById("RGL001")).thenReturn(reglaMontoLimite);
        doNothing().when(reglaFraudeRepository).deleteById("RGL001");

        reglaFraudeService.eliminar("RGL001");

        verify(reglaFraudeRepository, times(1)).findById("RGL001");
        verify(reglaFraudeRepository, times(1)).deleteById("RGL001");
    }

    @Test
    void eliminar_reglaNoExistente_lanzaExcepcion() {
        when(reglaFraudeRepository.findById("REGLA_INEXISTENTE")).thenReturn(null);

        assertThrows(ReglaFraudeNotFoundException.class, () -> {
            reglaFraudeService.eliminar("REGLA_INEXISTENTE");
        });
        verify(reglaFraudeRepository, times(1)).findById("REGLA_INEXISTENTE");
    }

    @Test
    void obtenerReglasActivas_retornaReglasActivasCorrectas() {
        when(reglaFraudeRepository.findByEstadoTrue()).thenReturn(reglasCompletas);

        List<ReglaFraudeDTO> resultado = reglaFraudeService.obtenerReglasActivas();

        assertEquals(2, resultado.size());
        assertEquals("RGL001", resultado.get(0).getCodReglaFraude());
        assertEquals("RGL002", resultado.get(1).getCodReglaFraude());
        verify(reglaFraudeRepository, times(1)).findByEstadoTrue();
    }

    @Test
    void obtenerReglasActivasPorTipo_filtraCorrectamente() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenReturn(Arrays.asList(reglaMontoLimite));

        List<ReglaFraudeDTO> resultado = reglaFraudeService.obtenerReglasActivasPorTipo("MON");

        assertEquals(1, resultado.size());
        assertEquals("RGL001", resultado.get(0).getCodReglaFraude());
        assertEquals("MON", resultado.get(0).getTipoRegla());
        verify(reglaFraudeRepository, times(1)).findByTipoReglaAndEstadoTrue("MON");
    }
} 