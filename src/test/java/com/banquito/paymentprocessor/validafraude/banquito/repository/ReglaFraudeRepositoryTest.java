package com.banquito.paymentprocessor.validafraude.banquito.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReglaFraudeRepositoryTest {

    @Mock
    private RedisTemplate<String, ReglaFraudeDTO> redisTemplate;

    @Mock
    private ValueOperations<String, ReglaFraudeDTO> valueOperations;

    @InjectMocks
    private ReglaFraudeRepository reglaFraudeRepository;

    private ReglaFraudeDTO reglaMontoLimite;
    private ReglaFraudeDTO reglaFrecuencia;
    private List<ReglaFraudeDTO> reglasCompletas;
    private static final String KEY_PREFIX = "regla:fraude:";

    @BeforeEach
    void setUp() {
        reglaMontoLimite = new ReglaFraudeDTO();
        reglaMontoLimite.setCodigo("RGL001");
        reglaMontoLimite.setTipo("MON");
        reglaMontoLimite.setDescripcion("Regla de monto límite");
        reglaMontoLimite.setEstado(true);
        reglaMontoLimite.setMontoLimite(new BigDecimal("5000.00"));

        reglaFrecuencia = new ReglaFraudeDTO();
        reglaFrecuencia.setCodigo("RGL002");
        reglaFrecuencia.setTipo("FRQ");
        reglaFrecuencia.setDescripcion("Regla de frecuencia de transacciones");
        reglaFrecuencia.setEstado(true);
        reglaFrecuencia.setMaxTransaccionesPorMinuto(5);

        reglasCompletas = Arrays.asList(reglaMontoLimite, reglaFrecuencia);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void findById_reglaExistente_retornaRegla() {
        String key = KEY_PREFIX + "RGL001";
        when(valueOperations.get(key)).thenReturn(reglaMontoLimite);

        ReglaFraudeDTO resultado = reglaFraudeRepository.findById("RGL001");

        assertNotNull(resultado);
        assertEquals("RGL001", resultado.getCodigo());
        assertEquals("MON", resultado.getTipo());
        verify(valueOperations, times(1)).get(eq(key));
    }

    @Test
    void findById_reglaNoExistente_retornaNull() {
        String key = KEY_PREFIX + "REGLA_INEXISTENTE";
        when(valueOperations.get(key)).thenReturn(null);

        ReglaFraudeDTO resultado = reglaFraudeRepository.findById("REGLA_INEXISTENTE");

        assertNull(resultado);
        verify(valueOperations, times(1)).get(eq(key));
    }

    @Test
    void findAll_retornaTodasLasReglas() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + "RGL001");
        keys.add(KEY_PREFIX + "RGL002");
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(KEY_PREFIX + "RGL001")).thenReturn(reglaMontoLimite);
        when(valueOperations.get(KEY_PREFIX + "RGL002")).thenReturn(reglaFrecuencia);

        List<ReglaFraudeDTO> resultados = reglaFraudeRepository.findAll();

        assertNotNull(resultados);
        assertEquals(2, resultados.size());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
        verify(valueOperations, times(1)).get(eq(KEY_PREFIX + "RGL001"));
        verify(valueOperations, times(1)).get(eq(KEY_PREFIX + "RGL002"));
    }

    @Test
    void findByEstadoTrue_retornaReglasActivas() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + "RGL001");
        keys.add(KEY_PREFIX + "RGL002");
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(KEY_PREFIX + "RGL001")).thenReturn(reglaMontoLimite);
        when(valueOperations.get(KEY_PREFIX + "RGL002")).thenReturn(reglaFrecuencia);

        List<ReglaFraudeDTO> resultados = reglaFraudeRepository.findByEstadoTrue();

        assertNotNull(resultados);
        assertEquals(2, resultados.size());
        assertEquals("RGL001", resultados.get(0).getCodigo());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }

    @Test
    void findByTipoReglaAndEstadoTrue_retornaReglasFiltradas() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + "RGL001");
        keys.add(KEY_PREFIX + "RGL002");
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(KEY_PREFIX + "RGL001")).thenReturn(reglaMontoLimite);
        when(valueOperations.get(KEY_PREFIX + "RGL002")).thenReturn(reglaFrecuencia);

        List<ReglaFraudeDTO> resultados = reglaFraudeRepository.findByTipoReglaAndEstadoTrue("MON");

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals("RGL001", resultados.get(0).getCodigo());
        assertEquals("MON", resultados.get(0).getTipo());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }

    @Test
    void save_guardaReglaNueva() {
        doNothing().when(valueOperations).set(eq(KEY_PREFIX + "RGL001"), eq(reglaMontoLimite));

        reglaFraudeRepository.save(reglaMontoLimite);

        verify(valueOperations, times(1)).set(eq(KEY_PREFIX + "RGL001"), eq(reglaMontoLimite));
    }

    @Test
    void deleteById_eliminaReglaExistente() {
        when(redisTemplate.delete(KEY_PREFIX + "RGL001")).thenReturn(true);

        reglaFraudeRepository.deleteById("RGL001");

        verify(redisTemplate, times(1)).delete(eq(KEY_PREFIX + "RGL001"));
    }
} 