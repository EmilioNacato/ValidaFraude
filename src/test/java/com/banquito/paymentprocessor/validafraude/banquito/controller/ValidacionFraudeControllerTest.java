package com.banquito.paymentprocessor.validafraude.banquito.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.validafraude.banquito.service.ValidacionFraudeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ValidacionFraudeController.class)
public class ValidacionFraudeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidacionFraudeService validacionFraudeService;

    @Autowired
    private ObjectMapper objectMapper;

    private ValidacionFraudeRequestDTO requestDTO;
    private ValidacionFraudeResponseDTO fraudeDetectadoResponseDTO;
    private ValidacionFraudeResponseDTO transaccionValidaResponseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new ValidacionFraudeRequestDTO();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("1000.00"));
        requestDTO.setCodigoComercio("COM123");
        requestDTO.setCodigoUnico("TRX123456");
        requestDTO.setTipoTransaccion("COMPRA");

        fraudeDetectadoResponseDTO = new ValidacionFraudeResponseDTO();
        fraudeDetectadoResponseDTO.setEsFraude(true);
        fraudeDetectadoResponseDTO.setCodigoRegla("MONTO_LIMITE");
        fraudeDetectadoResponseDTO.setMensaje("Monto excede el límite permitido");
        fraudeDetectadoResponseDTO.setNivelRiesgo("ALTO");

        transaccionValidaResponseDTO = new ValidacionFraudeResponseDTO();
        transaccionValidaResponseDTO.setEsFraude(false);
        transaccionValidaResponseDTO.setCodigoRegla("VALIDA");
        transaccionValidaResponseDTO.setMensaje("Transacción válida");
        transaccionValidaResponseDTO.setNivelRiesgo("BAJO");
    }

    @Test
    void validarTransaccion_sinFraude_retornaOK() throws Exception {
        when(validacionFraudeService.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(transaccionValidaResponseDTO);

        mockMvc.perform(post("/api/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFraude").value(false))
                .andExpect(jsonPath("$.codigoRegla").value("VALIDA"))
                .andExpect(jsonPath("$.mensaje").value("Transacción válida"));
    }

    @Test
    void validarTransaccion_conFraude_retornaOK() throws Exception {
        when(validacionFraudeService.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(fraudeDetectadoResponseDTO);

        mockMvc.perform(post("/api/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFraude").value(true))
                .andExpect(jsonPath("$.codigoRegla").value("MONTO_LIMITE"))
                .andExpect(jsonPath("$.mensaje").value("Monto excede el límite permitido"));
    }

    @Test
    void validarTransaccion_errorEnServicio_retornaOK() throws Exception {
        ValidacionFraudeResponseDTO errorResponseDTO = new ValidacionFraudeResponseDTO();
        errorResponseDTO.setEsFraude(true);
        errorResponseDTO.setCodigoRegla("ERROR");
        errorResponseDTO.setMensaje("Error en validación de fraude: Error de conexión");

        when(validacionFraudeService.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(errorResponseDTO);

        mockMvc.perform(post("/api/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFraude").value(true))
                .andExpect(jsonPath("$.codigoRegla").value("ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Error en validación de fraude: Error de conexión"));
    }
} 