package com.sena.smartparking.service;

import com.sena.smartparking.dto.ConteoResponse;
import com.sena.smartparking.dto.RegistroParqueoRequest;
import com.sena.smartparking.dto.RegistroParqueoResponse;

import java.util.List;

/**
 * Contrato de servicios de negocio para la gestión de registros de parqueo.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public interface RegistroParqueoService {

    /**
     * Obtiene todos los registros de parqueo, con opción de filtro por placa.
     * 
     * @param placaFiltro Opcional, placa a buscar.
     * @return Lista de DTOs de respuesta.
     */
    List<RegistroParqueoResponse> listarTodos(String placaFiltro);

    /**
     * Consulta un registro de parqueo por su identificador primario.
     * 
     * @param idRegistro Identificador único.
     * @return DTO del registro encontrado.
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si el ID no existe.
     */
    RegistroParqueoResponse consultarPorId(int idRegistro);

    /**
     * Retorna el número total de vehículos registrados en el histórico.
     * 
     * @return ConteoResponse con la cantidad total.
     */
    ConteoResponse contarRegistros();

    /**
     * Registra el ingreso de un nuevo vehículo al parqueadero.
     * 
     * @param request Datos validados de la solicitud.
     * @return DTO del registro creado con su ID autoincremental asignado.
     */
    RegistroParqueoResponse registrarIngreso(RegistroParqueoRequest request);

    /**
     * Actualiza la información de un registro de parqueo existente.
     * 
     * @param idRegistro Identificador del registro a modificar.
     * @param request Datos con los nuevos valores.
     * @return DTO del registro actualizado.
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si el ID no existe.
     */
    RegistroParqueoResponse actualizar(int idRegistro, RegistroParqueoRequest request);

    /**
     * Elimina un registro de parqueo por su ID.
     * 
     * @param idRegistro Identificador a eliminar.
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si no existe.
     * @throws com.sena.smartparking.exception.IntegrityConflictException Si tiene pagos asociados.
     */
    void eliminar(int idRegistro);
}
