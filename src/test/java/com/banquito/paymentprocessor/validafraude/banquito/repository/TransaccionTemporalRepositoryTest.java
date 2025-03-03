package com.banquito.paymentprocessor.validafraude.banquito.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.TransaccionTemporalDTO;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransaccionTemporalRepositoryTest {

    @Mock
    private RedisTemplate<String, TransaccionTemporalDTO> redisTemplate;

    @Mock
    private ValueOperations<String, TransaccionTemporalDTO> valueOperations;

    @InjectMocks
    private TransaccionTemporalRepository transaccionTemporalRepository;

    private TransaccionTemporalDTO transaccion;
    private static final String KEY_PREFIX = "transaccion:temporal:";
    private static final String COD_TRANSACCION = "TRX123";

    @BeforeEach
    void setUp() {
        transaccion = new TransaccionTemporalDTO();
        transaccion.setId(1L);
        transaccion.setCodTransaccion(COD_TRANSACCION);
        transaccion.setNumeroTarjeta("1234567890123456");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.00"));
        transaccion.setEstablecimiento("Tienda XYZ");
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("APR");
        transaccion.setSwiftBanco("SWIFT001");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void findByCodTransaccion_retornaTransaccionSiExiste() {
        String key = KEY_PREFIX + COD_TRANSACCION;
        when(valueOperations.get(key)).thenReturn(transaccion);

        TransaccionTemporalDTO resultado = transaccionTemporalRepository.findByCodTransaccion(COD_TRANSACCION);

        assertNotNull(resultado);
        assertEquals(COD_TRANSACCION, resultado.getCodTransaccion());
        assertEquals("1234567890123456", resultado.getNumeroTarjeta());
        verify(valueOperations, times(1)).get(eq(key));
    }

    @Test
    void findByNumeroTarjeta_retornaListadoDeTransacciones() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + COD_TRANSACCION);
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(anyString())).thenReturn(transaccion);

        List<TransaccionTemporalDTO> resultados = transaccionTemporalRepository.findByNumeroTarjeta("1234567890123456");

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals(COD_TRANSACCION, resultados.get(0).getCodTransaccion());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }

    @Test
    void findRecent_retornaTransaccionesRecientes() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + COD_TRANSACCION);
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(anyString())).thenReturn(transaccion);

        List<TransaccionTemporalDTO> resultados = transaccionTemporalRepository.findRecent(5);

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals(COD_TRANSACCION, resultados.get(0).getCodTransaccion());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }

    @Test
    void findAll_retornaTodasLasTransacciones() {
        Set<String> keys = new HashSet<>();
        keys.add(KEY_PREFIX + COD_TRANSACCION);
        
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(keys);
        when(valueOperations.get(anyString())).thenReturn(transaccion);

        List<TransaccionTemporalDTO> resultados = transaccionTemporalRepository.findAll();

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals(COD_TRANSACCION, resultados.get(0).getCodTransaccion());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }

    @Test
    void findAll_sinDatos_retornaListaVacia() {
        when(redisTemplate.keys(KEY_PREFIX + "*")).thenReturn(new HashSet<>());

        List<TransaccionTemporalDTO> resultados = transaccionTemporalRepository.findAll();

        assertNotNull(resultados);
        assertEquals(0, resultados.size());
        verify(redisTemplate, times(1)).keys(eq(KEY_PREFIX + "*"));
    }
} 