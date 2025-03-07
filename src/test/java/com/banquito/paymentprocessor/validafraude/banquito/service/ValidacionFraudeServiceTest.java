package com.banquito.paymentprocessor.validafraude.banquito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.TransaccionTemporalDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.repository.ReglaFraudeRepository;
import com.banquito.paymentprocessor.validafraude.banquito.repository.TransaccionTemporalRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidacionFraudeServiceTest {

    @Mock
    private ReglaFraudeRepository reglaFraudeRepository;

    @Mock
    private TransaccionTemporalRepository transaccionTemporalRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ValidacionFraudeService validacionFraudeService;

    private ValidacionFraudeRequestDTO requestDTO;
    private List<ReglaFraudeDTO> reglasMontoLimite;
    private List<ReglaFraudeDTO> reglasFrecuencia;
    private List<ReglaFraudeDTO> reglasPatron;
    private List<TransaccionTemporalDTO> transaccionesRecientes;

    @BeforeEach
    void setUp() {
        requestDTO = new ValidacionFraudeRequestDTO();
        requestDTO.setNumeroTarjeta("4532123456789012");
        requestDTO.setMonto(new BigDecimal("100.50"));
        requestDTO.setCodigoComercio("COM123");
        requestDTO.setCodigoUnico("TRX123456");
        requestDTO.setTipoTransaccion("PEN");

        ReglaFraudeDTO reglaMontoLimite = new ReglaFraudeDTO();
        reglaMontoLimite.setCodReglaFraude("RGL001");
        reglaMontoLimite.setTipoRegla("MON");
        reglaMontoLimite.setDescripcion("Regla de monto límite");
        reglaMontoLimite.setEstado(true);
        reglaMontoLimite.setMontoLimite(new BigDecimal("5000.00"));
        reglasMontoLimite = Arrays.asList(reglaMontoLimite);

        ReglaFraudeDTO reglaFrecuencia = new ReglaFraudeDTO();
        reglaFrecuencia.setCodReglaFraude("RGL002");
        reglaFrecuencia.setTipoRegla("FRQ");
        reglaFrecuencia.setDescripcion("Regla de frecuencia de transacciones");
        reglaFrecuencia.setEstado(true);
        reglaFrecuencia.setMaxTransaccionesPorMinuto(3);
        reglasFrecuencia = Arrays.asList(reglaFrecuencia);

        ReglaFraudeDTO reglaPatron = new ReglaFraudeDTO();
        reglaPatron.setCodReglaFraude("RGL003");
        reglaPatron.setTipoRegla("PAT");
        reglaPatron.setDescripcion("Regla de patrón de tiempo");
        reglaPatron.setEstado(true);
        reglaPatron.setMaxTransaccionesPorMinuto(5);
        reglaPatron.setPeriodoEvaluacion(15);
        reglasPatron = Arrays.asList(reglaPatron);

        transaccionesRecientes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TransaccionTemporalDTO transaccion = new TransaccionTemporalDTO();
            transaccion.setNumeroTarjeta("4111111111111111");
            transaccion.setFechaTransaccion(LocalDateTime.now().minusMinutes(i));
            transaccionesRecientes.add(transaccion);
        }

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void validarTransaccion_cuandoTransaccionValida_retornaNoFraude() {
        when(reglaFraudeRepository.findAll()).thenReturn(Collections.emptyList());
        when(transaccionTemporalRepository.findAll()).thenReturn(Collections.emptyList());
        when(valueOperations.get(anyString())).thenReturn(null);

        ValidacionFraudeResponseDTO response = validacionFraudeService.validarTransaccion(requestDTO);

        assertNotNull(response);
        assertFalse(response.isEsFraude());
        assertEquals("BAJO", response.getNivelRiesgo());
        assertTrue(response.getMensaje().contains("válida") || response.getMensaje().contains("Transacción"));

        verify(reglaFraudeRepository).findAll();
    }

    @Test
    void validarTransaccion_cuandoMontoExcesivo_retornaFraude() {
        requestDTO.setMonto(new BigDecimal("50000.00"));

        ValidacionFraudeResponseDTO response = validacionFraudeService.validarTransaccion(requestDTO);

        assertNotNull(response);
        assertTrue(response.isEsFraude());
        assertEquals("MONTO_LIMITE", response.getCodigoRegla());
        assertEquals("ALTO", response.getNivelRiesgo());
        assertTrue(response.getMensaje().contains("Monto excede") || response.getMensaje().contains("límite"));

        verify(reglaFraudeRepository, times(0)).findAll();
    }

    @Test
    void validarTransaccion_cuandoFrecuenciaExcesiva_retornaFraude() {
        when(valueOperations.get(anyString())).thenReturn(5);

        ValidacionFraudeResponseDTO response = validacionFraudeService.validarTransaccion(requestDTO);

        assertNotNull(response);
        assertTrue(response.isEsFraude());
        assertEquals("FRECUENCIA", response.getCodigoRegla());
        assertEquals("ALTO", response.getNivelRiesgo());
        assertTrue(response.getMensaje().contains("Excede") || response.getMensaje().contains("transacciones"));

        verify(valueOperations).increment(anyString());
    }

    @Test
    void validarTransaccion_cuandoErrorEnProcesamiento_manejaExcepcion() {
        when(reglaFraudeRepository.findAll()).thenThrow(new RuntimeException("Error de conexión"));

        ValidacionFraudeResponseDTO response = validacionFraudeService.validarTransaccion(requestDTO);

        assertNotNull(response);
        assertTrue(response.isEsFraude());
        assertEquals("ERROR", response.getCodigoRegla());
        assertEquals("ALTO", response.getNivelRiesgo());
        assertTrue(response.getMensaje().contains("Error") || response.getMensaje().contains("validación"));
    }

    @Test
    void guardarRegla_llamaRepositorioCorrectamente() {
        ReglaFraudeDTO regla = new ReglaFraudeDTO();
        regla.setCodReglaFraude("RGL001");

        validacionFraudeService.guardarRegla(regla);

        verify(reglaFraudeRepository, times(1)).save(regla);
    }

    @Test
    void obtenerTodasLasReglas_retornaListadoCompleto() {
        List<ReglaFraudeDTO> reglas = new ArrayList<>();
        reglas.addAll(reglasMontoLimite);
        reglas.addAll(reglasFrecuencia);
        reglas.addAll(reglasPatron);

        when(reglaFraudeRepository.findAll()).thenReturn(reglas);

        List<ReglaFraudeDTO> resultado = validacionFraudeService.obtenerTodasLasReglas();

        assertEquals(3, resultado.size());
        verify(reglaFraudeRepository, times(1)).findAll();
    }

    @Test
    void obtenerReglasActivas_retornaReglasActivas() {
        when(reglaFraudeRepository.findByEstadoTrue()).thenReturn(Arrays.asList(reglasMontoLimite.get(0), reglasFrecuencia.get(0)));

        List<ReglaFraudeDTO> resultado = validacionFraudeService.obtenerReglasActivas();

        assertEquals(2, resultado.size());
        verify(reglaFraudeRepository, times(1)).findByEstadoTrue();
    }

    @Test
    void eliminarRegla_llamaRepositorioCorrectamente() {
        String codigoRegla = "RGL001";

        validacionFraudeService.eliminarRegla(codigoRegla);

        verify(reglaFraudeRepository, times(1)).deleteById(codigoRegla);
    }
} 