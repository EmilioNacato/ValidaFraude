package com.banquito.paymentprocessor.validafraude.banquito.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.service.ReglaFraudeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ReglaFraudeController.class)
class ReglaFraudeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReglaFraudeService reglaFraudeService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReglaFraudeDTO reglaFraudeDTO;
    private List<ReglaFraudeDTO> reglaFraudeDTOList;

    @BeforeEach
    void setUp() {
        // Configurar una regla de fraude para las pruebas
        reglaFraudeDTO = new ReglaFraudeDTO();
        reglaFraudeDTO.setCodReglaFraude("REGLA001");
        reglaFraudeDTO.setDescripcion("Regla que verifica si el monto excede el límite permitido");
        reglaFraudeDTO.setTipoRegla("MON");
        reglaFraudeDTO.setMontoLimite(new BigDecimal("5000.0"));
        reglaFraudeDTO.setEstado(true);

        // Lista de reglas de fraude
        reglaFraudeDTOList = Arrays.asList(reglaFraudeDTO);
    }

    @Test
    void obtenerTodasLasReglas_retornaListaReglas() throws Exception {
        when(reglaFraudeService.obtenerTodas()).thenReturn(reglaFraudeDTOList);

        mockMvc.perform(get("/v1/reglas-fraude")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("REGLA001"));
    }

    @Test
    void obtenerReglaPorCodigo_cuandoExiste_retornaRegla() throws Exception {
        when(reglaFraudeService.buscarPorCodigo(anyString())).thenReturn(reglaFraudeDTO);

        mockMvc.perform(get("/v1/reglas-fraude/REGLA001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("REGLA001"));
    }

    @Test
    void obtenerReglaPorCodigo_cuandoNoExiste_retornaNotFound() throws Exception {
        when(reglaFraudeService.buscarPorCodigo(anyString())).thenReturn(null);

        mockMvc.perform(get("/v1/reglas-fraude/NOEXISTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearRegla_conDatosValidos_retornaCreated() throws Exception {
        doNothing().when(reglaFraudeService).registrarNueva(any(ReglaFraudeDTO.class));

        mockMvc.perform(post("/v1/reglas-fraude")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaFraudeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void actualizarRegla_cuandoExiste_retornaOk() throws Exception {
        doNothing().when(reglaFraudeService).actualizarExistente(anyString(), any(ReglaFraudeDTO.class));

        mockMvc.perform(put("/v1/reglas-fraude/REGLA001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaFraudeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarRegla_cuandoNoExiste_retornaNotFound() throws Exception {
        doNothing().when(reglaFraudeService).actualizarExistente(anyString(), any(ReglaFraudeDTO.class));

        mockMvc.perform(put("/v1/reglas-fraude/NOEXISTE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaFraudeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarRegla_cuandoExiste_retornaNoContent() throws Exception {
        doNothing().when(reglaFraudeService).eliminar(anyString());

        mockMvc.perform(delete("/v1/reglas-fraude/REGLA001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerReglasPorTipo_retornaListaFiltrada() throws Exception {
        when(reglaFraudeService.obtenerReglasActivasPorTipo(anyString())).thenReturn(reglaFraudeDTOList);

        mockMvc.perform(get("/v1/reglas-fraude/tipo/MON")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoRegla").value("MON"));
    }

    @Test
    void obtenerReglasPorTipo_cuandoNoHayReglas_retornaListaVacia() throws Exception {
        when(reglaFraudeService.obtenerReglasActivasPorTipo(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/reglas-fraude/tipo/NOEXISTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 