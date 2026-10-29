package com.sena.smartparking.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos (HTTP 404).
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
