package com.sena.smartparking.exception;

/**
 * Excepción personalizada para encapsular y gestionar los errores 
 * de la capa de persistencia y conexión a base de datos.
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class DatabaseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje explicativo.
     * 
     * @param message Descripción clara del error ocurrido.
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje explicativo y causa raíz.
     * 
     * @param message Descripción clara del error ocurrido.
     * @param cause Causa original (ej. SQLException).
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
