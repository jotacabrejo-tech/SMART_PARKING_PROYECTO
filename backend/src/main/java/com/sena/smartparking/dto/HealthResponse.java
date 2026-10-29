package com.sena.smartparking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Objeto de transferencia de datos (DTO) para la respuesta del endpoint de salud.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Respuesta del estado de disponibilidad y salud del servicio web")
public class HealthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Estado general del servicio", example = "UP")
    private String status;

    @Schema(description = "Nombre de la aplicación", example = "Smart Parking API")
    private String application;

    @Schema(description = "Versión del software", example = "1.0.0")
    private String version;

    @Schema(description = "Fecha y hora del servidor al responder", example = "2026-10-27T23:45:00")
    private LocalDateTime timestamp;

    @Schema(description = "Estado de conectividad con la base de datos MySQL (sin exponer secretos)", example = "CONNECTED")
    private String database;

    public HealthResponse() {
    }

    public HealthResponse(String status, String application, String version, LocalDateTime timestamp, String database) {
        this.status = status;
        this.application = application;
        this.version = version;
        this.timestamp = timestamp;
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
