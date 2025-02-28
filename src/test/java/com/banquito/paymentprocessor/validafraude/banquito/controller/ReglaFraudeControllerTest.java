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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.validafraude.banquito.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.exception.ReglaFraudeNotFoundException;
import com.banquito.paymentprocessor.validafraude.banquito.service.ReglaFraudeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ReglaFraudeController.class)
public class ReglaFraudeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReglaFraudeService reglaFraudeService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReglaFraudeDTO reglaFraudeDTO;
    private List<ReglaFraudeDTO> reglasDto;

    @BeforeEach
    void setUp() {
        reglaFraudeDTO = new ReglaFraudeDTO();
        reglaFraudeDTO.setCodigo("RGL001");
        reglaFraudeDTO.setTipo("MON");
        reglaFraudeDTO.setDescripcion("Regla de monto límite");
        reglaFraudeDTO.setEstado(true);
        reglaFraudeDTO.setMontoLimite(new BigDecimal("5000.00"));

        ReglaFraudeDTO reglaFraudeDTO2 = new ReglaFraudeDTO();
        reglaFraudeDTO2.setCodigo("RGL002");
        reglaFraudeDTO2.setTipo("FRQ");
        reglaFraudeDTO2.setDescripcion("Regla de frecuencia de transacciones");
        reglaFraudeDTO2.setEstado(true);
        reglaFraudeDTO2.setMaxTransaccionesPorMinuto(5);

        ReglaFraudeDTO reglaFraudeDTO3 = new ReglaFraudeDTO();
        reglaFraudeDTO3.setCodigo("RGL003");
        reglaFraudeDTO3.setTipo("PAT");
        reglaFraudeDTO3.setDescripcion("Regla de patrón de tiempo");
        reglaFraudeDTO3.setEstado(false);
        reglaFraudeDTO3.setMaxTransaccionesPorMinuto(10);
        reglaFraudeDTO3.setPeriodoEvaluacion(30);

        reglasDto = Arrays.asList(reglaFraudeDTO, reglaFraudeDTO2, reglaFraudeDTO3);
    }

    @Test
    void obtenerTodasLasReglasFraude_debeRetornarListado() throws Exception {
        when(reglaFraudeService.obtenerTodas()).thenReturn(reglasDto);

        mockMvc.perform(get("/api/v1/reglas-fraude/listado/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].codigo", is("RGL001")))
                .andExpect(jsonPath("$[1].codigo", is("RGL002")))
                .andExpect(jsonPath("$[2].codigo", is("RGL003")));
    }

    @Test
    void consultarReglaPorCodigo_reglaExistente_debeRetornarRegla() throws Exception {
        when(reglaFraudeService.buscarPorCodigo("RGL001")).thenReturn(reglaFraudeDTO);

        mockMvc.perform(get("/api/v1/reglas-fraude/consulta/RGL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("RGL001")))
                .andExpect(jsonPath("$.tipo", is("MON")))
                .andExpect(jsonPath("$.descripcion", is("Regla de monto límite")))
                .andExpect(jsonPath("$.estado", is(true)));
    }

    @Test
    void consultarReglaPorCodigo_reglaNoExistente_debeRetornarNotFound() throws Exception {
        when(reglaFraudeService.buscarPorCodigo("REGLA_INEXISTENTE"))
                .thenThrow(new ReglaFraudeNotFoundException("REGLA_INEXISTENTE"));

        mockMvc.perform(get("/api/v1/reglas-fraude/consulta/REGLA_INEXISTENTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarNuevaRegla_datosValidos_debeRetornarReglaCreada() throws Exception {
        doNothing().when(reglaFraudeService).registrarNueva(any(ReglaFraudeDTO.class));

        mockMvc.perform(post("/api/v1/reglas-fraude/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaFraudeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("RGL001")))
                .andExpect(jsonPath("$.tipo", is("MON")))
                .andExpect(jsonPath("$.descripcion", is("Regla de monto límite")));
    }

    @Test
    void actualizarReglaExistente_reglaExistente_debeRetornarReglaActualizada() throws Exception {
        doNothing().when(reglaFraudeService).actualizarExistente(anyString(), any(ReglaFraudeDTO.class));

        ReglaFraudeDTO reglaActualizada = reglaFraudeDTO;
        reglaActualizada.setDescripcion("Regla actualizada");
        reglaActualizada.setMontoLimite(new BigDecimal("7000.00"));

        mockMvc.perform(put("/api/v1/reglas-fraude/actualizacion/RGL001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaActualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("RGL001")))
                .andExpect(jsonPath("$.descripcion", is("Regla actualizada")));
    }

    @Test
    void actualizarReglaExistente_reglaNoExistente_debeRetornarNotFound() throws Exception {
        doThrow(new ReglaFraudeNotFoundException("REGLA_INEXISTENTE"))
                .when(reglaFraudeService).actualizarExistente(anyString(), any(ReglaFraudeDTO.class));

        mockMvc.perform(put("/api/v1/reglas-fraude/actualizacion/REGLA_INEXISTENTE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reglaFraudeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarRegla_reglaExistente_debeRetornarNoContent() throws Exception {
        doNothing().when(reglaFraudeService).eliminar(anyString());

        mockMvc.perform(delete("/api/v1/reglas-fraude/eliminacion/RGL001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarRegla_reglaNoExistente_debeRetornarNotFound() throws Exception {
        doThrow(new ReglaFraudeNotFoundException("REGLA_INEXISTENTE"))
                .when(reglaFraudeService).eliminar(anyString());

        mockMvc.perform(delete("/api/v1/reglas-fraude/eliminacion/REGLA_INEXISTENTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerReglasActivas_debeRetornarListadoFiltrado() throws Exception {
        List<ReglaFraudeDTO> reglasActivas = Arrays.asList(reglasDto.get(0), reglasDto.get(1));
        when(reglaFraudeService.obtenerReglasActivas()).thenReturn(reglasActivas);

        mockMvc.perform(get("/api/v1/reglas-fraude/listado/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigo", is("RGL001")))
                .andExpect(jsonPath("$[1].codigo", is("RGL002")));
    }
} 