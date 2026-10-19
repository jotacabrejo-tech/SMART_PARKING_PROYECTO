package com.sena.smartparking.dao;

import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.RegistroParqueo;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de operaciones CRUD para la entidad RegistroParqueo.
 * Define la interfaz de acceso a datos desacoplada de la implementación JDBC.
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public interface RegistroParqueoDAO {

    /**
     * Inserta un nuevo registro de ingreso de vehículo en la base de datos.
     * Asigna automáticamente el id_registro generado al objeto retornado.
     * 
     * @param registro Objeto con los datos del ingreso (placa, hora, valor, estado).
     * @return RegistroParqueo con su identificador primario asignado.
     * @throws DatabaseException En caso de error de validación o persistencia SQL.
     */
    RegistroParqueo insertar(RegistroParqueo registro) throws DatabaseException;

    /**
     * Consulta un registro de parqueo por su clave primaria (id_registro).
     * 
     * @param idRegistro Identificador único del registro.
     * @return Optional con el registro si existe, o Optional.empty() si no.
     * @throws DatabaseException En caso de error SQL.
     */
    Optional<RegistroParqueo> consultarPorId(int idRegistro) throws DatabaseException;

    /**
     * Consulta todos los registros asociados a una placa de vehículo.
     * 
     * @param placa Placa a buscar (exacta o prefijo).
     * @return Lista de registros coincidentes ordenados cronológicamente descendente.
     * @throws DatabaseException En caso de error SQL.
     */
    List<RegistroParqueo> consultarPorPlaca(String placa) throws DatabaseException;

    /**
     * Obtiene la lista completa de registros de parqueo.
     * 
     * @return Lista con todos los registros de la tabla.
     * @throws DatabaseException En caso de error SQL.
     */
    List<RegistroParqueo> listarTodos() throws DatabaseException;

    /**
     * Actualiza la información de un registro de parqueo existente.
     * Permite modificar valor_calculado y estado_pago.
     * 
     * @param registro Objeto con los datos modificados y su id_registro.
     * @return true si se actualizó al menos una fila, false de lo contrario.
     * @throws DatabaseException En caso de error SQL o si el registro no existe.
     */
    boolean actualizar(RegistroParqueo registro) throws DatabaseException;

    /**
     * Elimina un registro de parqueo por su identificador primario.
     * Respeta la integridad referencial: no permitirá la eliminación si existen
     * pagos asociados en la tabla 'pago'.
     * 
     * @param idRegistro Identificador de la fila a eliminar.
     * @return true si la fila fue eliminada, false si no se encontró.
     * @throws DatabaseException En caso de violación de clave foránea o error SQL.
     */
    boolean eliminar(int idRegistro) throws DatabaseException;

    /**
     * Cuenta el número total de registros existentes en la tabla.
     * 
     * @return Cantidad de filas.
     * @throws DatabaseException En caso de error SQL.
     */
    long contar() throws DatabaseException;
}
