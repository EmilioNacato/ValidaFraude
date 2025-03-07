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
    private ValidacionFraudeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new ValidacionFraudeRequestDTO();
        requestDTO.setNumeroTarjeta("4532123456789012");
        requestDTO.setMonto(new BigDecimal("100.50"));
        requestDTO.setCodigoComercio("COM123");
        requestDTO.setCodigoUnico("TRX123456");
        requestDTO.setTipoTransaccion("PEN");

        responseDTO = new ValidacionFraudeResponseDTO();
        responseDTO.setEsFraude(false);
        responseDTO.setCodigoRegla(null);
        responseDTO.setMensaje("Transacción válida");
        responseDTO.setNivelRiesgo("BAJO");
    }

    @Test
    void validarTransaccion_cuandoTransaccionValida_retornaRespuestaExitosa() throws Exception {
        when(validacionFraudeService.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFraude").value(false))
                .andExpect(jsonPath("$.mensaje").value("Transacción válida"))
                .andExpect(jsonPath("$.nivelRiesgo").value("BAJO"));
    }

    @Test
    void validarTransaccion_cuandoTransaccionFraudulenta_retornaRespuestaFraude() throws Exception {
        ValidacionFraudeResponseDTO respuestaFraude = new ValidacionFraudeResponseDTO();
        respuestaFraude.setEsFraude(true);
        respuestaFraude.setCodigoRegla("MONTO_LIMITE");
        respuestaFraude.setMensaje("Monto excede el límite permitido");
        respuestaFraude.setNivelRiesgo("ALTO");

        when(validacionFraudeService.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(respuestaFraude);

        mockMvc.perform(post("/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFraude").value(true))
                .andExpect(jsonPath("$.codigoRegla").value("MONTO_LIMITE"))
                .andExpect(jsonPath("$.mensaje").value("Monto excede el límite permitido"))
                .andExpect(jsonPath("$.nivelRiesgo").value("ALTO"));
    }

    @Test
    void validarTransaccion_cuandoDatosInvalidos_retornaBadRequest() throws Exception {
        ValidacionFraudeRequestDTO requestInvalido = new ValidacionFraudeRequestDTO();
        requestInvalido.setNumeroTarjeta("123");
        requestInvalido.setMonto(new BigDecimal("100.50"));
        requestInvalido.setCodigoComercio("COM123");
        requestInvalido.setCodigoUnico("TRX123456");
        requestInvalido.setTipoTransaccion("PEN");

        mockMvc.perform(post("/v1/fraude/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
} 