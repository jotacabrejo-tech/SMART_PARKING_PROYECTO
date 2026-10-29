package com.sena.smartparking.dao;

import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de operaciones de persistencia para la entidad Cliente.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public interface ClienteDAO {

    /**
     * Inserta un nuevo cliente en la tabla 'cliente'.
     * Asigna el id_cliente autoincremental generado.
     */
    Cliente insertar(Cliente cliente) throws DatabaseException;

    /**
     * Consulta un cliente por su identificador primario (id_cliente).
     */
    Optional<Cliente> consultarPorId(int idCliente) throws DatabaseException;

    /**
     * Consulta un cliente por su tipo y número de documento (para evitar duplicados).
     */
    Optional<Cliente> consultarPorDocumento(String tipoDoc, String numDoc) throws DatabaseException;

    /**
     * Obtiene el listado completo de clientes.
     */
    List<Cliente> listarTodos() throws DatabaseException;

    /**
     * Actualiza la información de un cliente existente.
     */
    boolean actualizar(Cliente cliente) throws DatabaseException;

    /**
     * Elimina un cliente por su ID (si no tiene facturas asociadas).
     */
    boolean eliminar(int idCliente) throws DatabaseException;

    /**
     * Cuenta el número total de clientes registrados.
     */
    long contar() throws DatabaseException;
}
