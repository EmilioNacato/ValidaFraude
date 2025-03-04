package com.banquito.paymentprocessor.validafraude.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.service.ReglaFraudeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/reglas-fraude")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
@Tag(name = "Reglas de Fraude", 
     description = "API para la gestión y administración de reglas de detección de fraude")
@Slf4j
public class ReglaFraudeController {

    private final ReglaFraudeService servicio;

    public ReglaFraudeController(ReglaFraudeService servicio) {
        this.servicio = servicio;
    }

    @GetMapping({"", "/"})
    @Operation(
        summary = "Obtener listado completo de reglas de fraude",
        description = "Retorna el listado completo de todas las reglas de fraude registradas en el sistema, " +
                     "incluyendo tanto reglas activas como inactivas"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Listado de reglas de fraude obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerTodasLasReglasFraude() {
        log.debug("Petición REST para obtener todas las reglas de fraude");
        List<ReglaFraudeDTO> reglas = servicio.obtenerTodas();
        log.info("Se encontraron {} reglas de fraude en el sistema", reglas.size());
        return ResponseEntity.ok(reglas);
    }

    @GetMapping("/{codigoRegla}")
    @Operation(
        summary = "Consultar regla de fraude por código",
        description = "Retorna la información detallada de una regla de fraude específica según su código único"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Regla de fraude encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Regla de fraude no encontrada en el sistema",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ReglaFraudeDTO> consultarReglaPorCodigo(
            @Parameter(
                description = "Código único de la regla de fraude", 
                required = true,
                example = "RGL001"
            )
            @PathVariable("codigoRegla") String codigoRegla) {
        log.debug("Petición REST para consultar la regla de fraude con código: {}", codigoRegla);
        ReglaFraudeDTO regla = servicio.buscarPorCodigo(codigoRegla);
        return ResponseEntity.ok(regla);
    }

    @PostMapping("/")
    @Operation(
        summary = "Registrar nueva regla de fraude",
        description = "Crea y registra una nueva regla de fraude en el sistema con los datos proporcionados. " +
                     "La regla puede ser de tipo monetario (MON) o transaccional (TRX)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Regla de fraude registrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de la regla inválidos o incompletos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ReglaFraudeDTO> registrarNuevaRegla(
            @Parameter(
                description = "Datos de la nueva regla de fraude",
                required = true,
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
            @Valid @RequestBody ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Petición REST para registrar nueva regla de fraude: {}", reglaFraudeDTO);
        servicio.registrarNueva(reglaFraudeDTO);
        return ResponseEntity.ok(reglaFraudeDTO);
    }

    @PutMapping("/{codigoRegla}")
    @Operation(
        summary = "Actualizar regla de fraude existente",
        description = "Actualiza la información de una regla de fraude existente en el sistema. " +
                     "Permite modificar parámetros, descripción y estado de la regla"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Regla de fraude actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de actualización inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Regla de fraude no encontrada en el sistema",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ReglaFraudeDTO> actualizarReglaExistente(
            @Parameter(
                description = "Código único de la regla de fraude",
                required = true,
                example = "RGL001"
            )
            @PathVariable("codigoRegla") String codigoRegla,
            @Parameter(
                description = "Datos actualizados de la regla de fraude",
                required = true,
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
            @Valid @RequestBody ReglaFraudeDTO reglaFraudeDTO) {
        log.debug("Petición REST para actualizar la regla de fraude con código: {}", codigoRegla);
        servicio.actualizarExistente(codigoRegla, reglaFraudeDTO);
        return ResponseEntity.ok(reglaFraudeDTO);
    }

    @DeleteMapping("/{codigoRegla}")
    @Operation(
        summary = "Eliminar regla de fraude",
        description = "Elimina una regla de fraude específica del sistema según su código único"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Regla de fraude eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Regla de fraude no encontrada en el sistema",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<Void> eliminarRegla(
            @Parameter(
                description = "Código único de la regla de fraude",
                required = true,
                example = "RGL001"
            )
            @PathVariable("codigoRegla") String codigoRegla) {
        log.debug("Petición REST para eliminar la regla de fraude con código: {}", codigoRegla);
        servicio.eliminar(codigoRegla);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigoRegla}/estado")
    @Operation(
        summary = "Cambiar estado de una regla de fraude",
        description = "Actualiza el estado (activo/inactivo) de una regla de fraude específica"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estado de la regla actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Regla de fraude no encontrada en el sistema",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ReglaFraudeDTO> cambiarEstadoRegla(
            @Parameter(
                description = "Código único de la regla de fraude",
                required = true,
                example = "RGL001"
            )
            @PathVariable("codigoRegla") String codigoRegla,
            @Parameter(
                description = "Nuevo estado de la regla",
                required = true
            )
            @RequestBody Map<String, Boolean> estadoMap) {
        
        Boolean nuevoEstado = estadoMap.get("estado");
        if (nuevoEstado == null) {
            log.warn("Solicitud para cambiar estado de regla recibida sin el campo 'estado' en el cuerpo");
            throw new IllegalArgumentException("El campo 'estado' es requerido en el cuerpo de la solicitud");
        }
        
        log.info("Petición REST para cambiar estado de regla con código: {} a estado: {}", 
                 codigoRegla, nuevoEstado);
        
        ReglaFraudeDTO regla = servicio.cambiarEstadoRegla(codigoRegla, nuevoEstado);
        log.info("Estado de regla {} cambiado exitosamente a {}", codigoRegla, nuevoEstado);
        
        return ResponseEntity.ok(regla);
    }

    @GetMapping("/activas")
    @Operation(
        summary = "Obtener listado de reglas activas",
        description = "Retorna el listado de todas las reglas de fraude que están actualmente activas en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Listado de reglas activas obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerReglasActivas() {
        log.debug("Petición REST para obtener todas las reglas de fraude activas");
        List<ReglaFraudeDTO> reglas = servicio.obtenerReglasActivas();
        return ResponseEntity.ok(reglas);
    }

    @GetMapping("/{tipoRegla}")
    @Operation(
        summary = "Obtener listado de reglas activas por tipo",
        description = "Retorna el listado de reglas de fraude activas filtradas por un tipo específico (MON/TRX)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Listado de reglas activas por tipo obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReglaFraudeDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Tipo de regla inválido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<ReglaFraudeDTO>> obtenerReglasActivasPorTipo(
            @Parameter(
                description = "Tipo de regla de fraude (MON/TRX)",
                required = true,
                example = "MON"
            )
            @PathVariable String tipoRegla) {
        log.debug("Petición REST para obtener reglas de fraude activas por tipo: {}", tipoRegla);
        List<ReglaFraudeDTO> reglas = servicio.obtenerReglasActivasPorTipo(tipoRegla);
        return ResponseEntity.ok(reglas);
    }
} 