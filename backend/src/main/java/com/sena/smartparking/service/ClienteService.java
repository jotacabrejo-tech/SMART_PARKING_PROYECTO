package com.sena.smartparking.service;

import com.sena.smartparking.dto.ClienteRequest;
import com.sena.smartparking.dto.ClienteResponse;

import java.util.List;

/**
 * Contrato de servicios de negocio para la gestión de clientes.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public interface ClienteService {

    /**
     * Obtiene la lista completa de clientes registrados.
     */
    List<ClienteResponse> listarTodos();

    /**
     * Consulta un cliente por su ID primario.
     * 
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si no existe.
     */
    ClienteResponse consultarPorId(int idCliente);

    /**
     * Registra un nuevo cliente validando que no exista documento duplicado.
     * 
     * @throws com.sena.smartparking.exception.IntegrityConflictException Si ya existe el documento.
     */
    ClienteResponse registrarCliente(ClienteRequest request);

    /**
     * Actualiza la información de un cliente existente.
     * 
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si no existe.
     * @throws com.sena.smartparking.exception.IntegrityConflictException Si el documento colisiona con otro cliente.
     */
    ClienteResponse actualizar(int idCliente, ClienteRequest request);
}
