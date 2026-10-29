package com.sena.smartparking.dao;

import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.Pago;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de operaciones de persistencia para la entidad Pago.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public interface PagoDAO {

    /**
     * Inserta un nuevo registro en la tabla 'pago'.
     * Asigna el id_pago generado al objeto retornado.
     */
    Pago insertar(Pago pago) throws DatabaseException;

    /**
     * Consulta un pago por su identificador primario (id_pago).
     */
    Optional<Pago> consultarPorId(int idPago) throws DatabaseException;

    /**
     * Consulta todos los pagos asociados a un registro de parqueo específico (por id_registro).
     */
    List<Pago> consultarPorRegistro(int idRegistro) throws DatabaseException;

    /**
     * Obtiene el listado completo de pagos registrados en el sistema.
     */
    List<Pago> listarTodos() throws DatabaseException;

    /**
     * Cuenta el número total de pagos registrados.
     */
    long contar() throws DatabaseException;
}
