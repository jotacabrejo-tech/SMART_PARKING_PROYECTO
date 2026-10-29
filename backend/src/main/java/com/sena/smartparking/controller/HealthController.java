package com.sena.smartparking.controller;

import com.sena.smartparking.config.ConexionBD;
import com.sena.smartparking.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * Controlador REST para la comprobación del estado y salud de la aplicación.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud y Monitoreo", description = "Servicios para verificación de estado y disponibilidad de la API")
public class HealthController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar estado de la API",
            description = "Retorna un objeto JSON con el estado operativo del servicio web, la marca temporal del servidor y el estado de la base de datos sin exponer secretos ni rutas internas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Servicio activo y respondiendo correctamente",
            content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    public ResponseEntity<HealthResponse> checkHealth() {
        String dbStatus = verificarConexionBD();

        HealthResponse response = new HealthResponse(
                "UP",
                "Smart Parking API",
                "1.0.0",
                LocalDateTime.now(),
                dbStatus
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Comprueba de manera segura la conexión con MySQL sin exponer credenciales.
     */
    private String verificarConexionBD() {
        if (!ConexionBD.isConfigurado()) {
            return "NOT_CONFIGURED";
        }
        try (Connection conn = ConexionBD.getConnection()) {
            return (conn != null && !conn.isClosed()) ? "CONNECTED" : "DISCONNECTED";
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
