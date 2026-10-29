package com.sena.smartparking.exception;

/**
 * Excepción lanzada cuando ocurre un conflicto de integridad referencial o regla de negocio (HTTP 409).
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public class IntegrityConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IntegrityConflictException(String message) {
        super(message);
    }

    public IntegrityConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
