package com.banquito.paymentprocessor.validafraude.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.mapper.ReglaFraudeMapper;
import com.banquito.paymentprocessor.validafraude.banquito.model.ReglaFraude;
import com.banquito.paymentprocessor.validafraude.banquito.service.ReglaFraudeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reglas-fraude")
@Tag(name = "Reglas de Fraude", description = "API para la gestión de reglas de fraude")
@Slf4j
public class ReglaFraudeController {

    private final ReglaFraudeService servicio;
    private final ReglaFraudeMapper mapeador;

    public ReglaFraudeController(ReglaFraudeService servicio, ReglaFraudeMapper mapeador) {
        this.servicio = servicio;
        this.mapeador = mapeador;
    }

    @GetMapping("/listado/todas")
    @Operation(summary = "Obtener listado completo de reglas de fraude",
            description = "Retorna el listado completo de todas las reglas de fraude registradas en el sistema")
    @ApiResponse(responseCode = "200", description = "Listado de reglas de fraude obtenido exitosamente")
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerTodasLasReglasFraude() {
        log.debug("Petición REST para obtener todas las reglas de fraude");
        List<ReglaFraude> reglas = servicio.obtenerTodas();
        List<ReglaFraudeDTO> dtos = reglas.stream()
                .map(mapeador::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/consulta/{codigoRegla}")
    @Operation(summary = "Consultar regla de fraude por código",
            description = "Retorna la información detallada de una regla de fraude específica según su código")
    @ApiResponse(responseCode = "200", description = "Regla de fraude encontrada exitosamente")
    @ApiResponse(responseCode = "404", description = "Regla de fraude no encontrada en el sistema")
    public ResponseEntity<ReglaFraudeDTO> consultarReglaPorCodigo(
            @Parameter(description = "Código único de la regla de fraude", required = true)
            @PathVariable("codigoRegla") String codigoRegla) {
        log.debug("Petición REST para consultar la regla de fraude con código: {}", codigoRegla);
        ReglaFraude regla = servicio.buscarPorCodigo(codigoRegla);
        return ResponseEntity.ok(mapeador.toDTO(regla));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nueva regla de fraude",
            description = "Crea y registra una nueva regla de fraude en el sistema con los datos proporcionados")
    @ApiResponse(responseCode = "200", description = "Regla de fraude registrada exitosamente")
    public ResponseEntity<ReglaFraudeDTO> registrarNuevaRegla(
            @Parameter(description = "Datos de la nueva regla de fraude", required = true)
            @Valid @RequestBody ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Petición REST para registrar nueva regla de fraude: {}", reglaFraudeDTO);
        ReglaFraude regla = mapeador.toModel(reglaFraudeDTO);
        ReglaFraude reglaGuardada = servicio.registrarNueva(regla);
        return ResponseEntity.ok(mapeador.toDTO(reglaGuardada));
    }

    @PutMapping("/actualizacion/{codigoRegla}")
    @Operation(summary = "Actualizar regla de fraude existente",
            description = "Actualiza la información de una regla de fraude existente en el sistema")
    @ApiResponse(responseCode = "200", description = "Regla de fraude actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Regla de fraude no encontrada en el sistema")
    public ResponseEntity<ReglaFraudeDTO> actualizarReglaExistente(
            @Parameter(description = "Código único de la regla de fraude", required = true)
            @PathVariable("codigoRegla") String codigoRegla,
            @Parameter(description = "Datos actualizados de la regla de fraude", required = true)
            @Valid @RequestBody ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Petición REST para actualizar la regla de fraude con código: {}", codigoRegla);
        ReglaFraude regla = mapeador.toModel(reglaFraudeDTO);
        ReglaFraude reglaActualizada = servicio.actualizarExistente(codigoRegla, regla);
        return ResponseEntity.ok(mapeador.toDTO(reglaActualizada));
    }

    @DeleteMapping("/eliminacion/{codigoRegla}")
    @Operation(summary = "Eliminar regla de fraude",
            description = "Elimina una regla de fraude específica del sistema según su código")
    @ApiResponse(responseCode = "200", description = "Regla de fraude eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Regla de fraude no encontrada en el sistema")
    public ResponseEntity<Void> eliminarRegla(
            @Parameter(description = "Código único de la regla de fraude", required = true)
            @PathVariable("codigoRegla") String codigoRegla) {
        log.debug("Petición REST para eliminar la regla de fraude con código: {}", codigoRegla);
        servicio.eliminar(codigoRegla);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listado/activas")
    @Operation(summary = "Obtener listado de reglas activas",
            description = "Retorna el listado de todas las reglas de fraude que están activas en el sistema")
    @ApiResponse(responseCode = "200", description = "Listado de reglas activas obtenido exitosamente")
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerReglasActivas() {
        log.debug("Petición REST para obtener reglas de fraude activas");
        List<ReglaFraude> reglas = servicio.obtenerReglasActivas();
        List<ReglaFraudeDTO> dtos = reglas.stream()
                .map(mapeador::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/listado/activas/tipo/{tipoRegla}")
    @Operation(summary = "Obtener listado de reglas activas por tipo",
            description = "Retorna el listado de reglas de fraude activas filtradas por un tipo específico")
    @ApiResponse(responseCode = "200", description = "Listado de reglas activas por tipo obtenido exitosamente")
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerReglasActivasPorTipo(
            @Parameter(description = "Tipo de regla de fraude (MON/TRX)", required = true)
            @PathVariable String tipoRegla) {
        log.debug("Petición REST para obtener reglas de fraude activas por tipo: {}", tipoRegla);
        List<ReglaFraude> reglas = servicio.obtenerReglasActivasPorTipo(tipoRegla);
        List<ReglaFraudeDTO> dtos = reglas.stream()
                .map(mapeador::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
} 