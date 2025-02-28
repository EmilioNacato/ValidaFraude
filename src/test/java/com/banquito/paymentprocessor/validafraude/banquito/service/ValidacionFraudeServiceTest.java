package com.banquito.paymentprocessor.validafraude.banquito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.TransaccionTemporalDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.dto.ValidacionFraudeResponseDTO;
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
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("10000.00"));
        requestDTO.setCodigoComercio("COM123");
        requestDTO.setCodigoUnico("TRX123456");
        requestDTO.setTipoTransaccion("COMPRA");

        ReglaFraudeDTO reglaMontoLimite = new ReglaFraudeDTO();
        reglaMontoLimite.setCodigo("RGL001");
        reglaMontoLimite.setTipo("MON");
        reglaMontoLimite.setDescripcion("Regla de monto límite");
        reglaMontoLimite.setEstado(true);
        reglaMontoLimite.setMontoLimite(new BigDecimal("5000.00"));
        reglasMontoLimite = Arrays.asList(reglaMontoLimite);

        ReglaFraudeDTO reglaFrecuencia = new ReglaFraudeDTO();
        reglaFrecuencia.setCodigo("RGL002");
        reglaFrecuencia.setTipo("FRQ");
        reglaFrecuencia.setDescripcion("Regla de frecuencia de transacciones");
        reglaFrecuencia.setEstado(true);
        reglaFrecuencia.setMaxTransaccionesPorMinuto(3);
        reglasFrecuencia = Arrays.asList(reglaFrecuencia);

        ReglaFraudeDTO reglaPatron = new ReglaFraudeDTO();
        reglaPatron.setCodigo("RGL003");
        reglaPatron.setTipo("PAT");
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
    void validarTransaccion_sinFraude_retornaTransaccionValida() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenReturn(new ArrayList<>());
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("FRQ")).thenReturn(new ArrayList<>());
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("PAT")).thenReturn(new ArrayList<>());

        ValidacionFraudeResponseDTO resultado = validacionFraudeService.validarTransaccion(requestDTO);

        assertFalse(resultado.isEsFraude());
        assertEquals("VALIDA", resultado.getCodigoRegla());
    }

    @Test
    void validarTransaccion_montoExcedeLimite_detectaFraude() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenReturn(reglasMontoLimite);

        ValidacionFraudeResponseDTO resultado = validacionFraudeService.validarTransaccion(requestDTO);

        assertTrue(resultado.isEsFraude());
        assertEquals("MONTO_LIMITE", resultado.getCodigoRegla());
    }

    @Test
    void validarTransaccion_frecuenciaExcesiva_detectaFraude() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenReturn(new ArrayList<>());
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("FRQ")).thenReturn(reglasFrecuencia);
        when(valueOperations.increment(anyString())).thenReturn(5L);

        ValidacionFraudeResponseDTO resultado = validacionFraudeService.validarTransaccion(requestDTO);

        assertTrue(resultado.isEsFraude());
        assertEquals("FRECUENCIA", resultado.getCodigoRegla());
        verify(redisTemplate).expire(anyString(), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void validarTransaccion_patronTiempoSospechoso_detectaFraude() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenReturn(new ArrayList<>());
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("FRQ")).thenReturn(new ArrayList<>());
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("PAT")).thenReturn(reglasPatron);
        when(transaccionTemporalRepository.findByNumeroTarjeta(anyString())).thenReturn(transaccionesRecientes);
        
        // Asegurar que las transacciones están dentro del período
        LocalDateTime tiempoLimite = LocalDateTime.now().minusMinutes(reglasPatron.get(0).getPeriodoEvaluacion());
        for (TransaccionTemporalDTO transaccion : transaccionesRecientes) {
            transaccion.setFechaTransaccion(tiempoLimite.plusMinutes(1)); // Todas dentro del período
        }

        ValidacionFraudeResponseDTO resultado = validacionFraudeService.validarTransaccion(requestDTO);

        assertTrue(resultado.isEsFraude());
        assertEquals("PATRON_TIEMPO", resultado.getCodigoRegla());
    }

    @Test
    void validarTransaccion_excepcionOcurre_retornaError() {
        when(reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON")).thenThrow(new RuntimeException("Error simulado"));

        ValidacionFraudeResponseDTO resultado = validacionFraudeService.validarTransaccion(requestDTO);

        assertTrue(resultado.isEsFraude());
        assertEquals("ERROR", resultado.getCodigoRegla());
    }

    @Test
    void guardarRegla_llamaRepositorioCorrectamente() {
        ReglaFraudeDTO regla = new ReglaFraudeDTO();
        regla.setCodigo("RGL001");

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