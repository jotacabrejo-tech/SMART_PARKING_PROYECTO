package com.sena.smartparking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta del conteo total de registros en la base de datos.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Respuesta del número total de registros existentes")
public class ConteoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Cantidad total de filas registradas en la tabla", example = "42")
    private long total;

    @Schema(description = "Fecha y hora del conteo", example = "2026-10-27T23:55:00")
    private LocalDateTime timestamp;

    public ConteoResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ConteoResponse(long total) {
        this.total = total;
        this.timestamp = LocalDateTime.now();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
