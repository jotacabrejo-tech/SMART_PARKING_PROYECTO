package com.sena.smartparking.service.impl;

import com.sena.smartparking.dao.ClienteDAO;
import com.sena.smartparking.dto.ClienteRequest;
import com.sena.smartparking.dto.ClienteResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.Cliente;
import com.sena.smartparking.service.ClienteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicios para la administración de Clientes.
 * Centraliza validaciones de duplicados de documento y persistencia JDBC.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteServiceImpl(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @Override
    public List<ClienteResponse> listarTodos() {
        try {
            return clienteDAO.listarTodos().stream()
                    .map(ClienteResponse::fromModel)
                    .collect(Collectors.toList());
        } catch (DatabaseException e) {
            throw new RuntimeException("Error en persistencia al listar los clientes: " + e.getMessage(), e);
        }
    }

    @Override
    public ClienteResponse consultarPorId(int idCliente) {
        if (idCliente <= 0) {
            throw new ResourceNotFoundException("El identificador del cliente debe ser un entero positivo.");
        }
        try {
            return clienteDAO.consultarPorId(idCliente)
                    .map(ClienteResponse::fromModel)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID " + idCliente + " no encontrado."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al consultar cliente con ID " + idCliente + ": " + e.getMessage(), e);
        }
    }

    @Override
    public ClienteResponse registrarCliente(ClienteRequest request) {
        String tipoDocNormalizado = request.getTipoDoc().trim().toUpperCase();
        String numDocNormalizado = request.getNumDoc().trim().toUpperCase();
        String nombresNormalizados = request.getNombres().trim();
        String correoNormalizado = request.getCorreo().trim().toLowerCase();

        // 1. Evitar duplicados por tipo y número de documento
        try {
            Optional<Cliente> existente = clienteDAO.consultarPorDocumento(tipoDocNormalizado, numDocNormalizado);
            if (existente.isPresent()) {
                throw new IntegrityConflictException("Conflicto: Ya existe un cliente registrado con el documento " 
                        + tipoDocNormalizado + " " + numDocNormalizado + ".");
            }

            Cliente nuevoCliente = new Cliente(
                    tipoDocNormalizado,
                    numDocNormalizado,
                    nombresNormalizados,
                    correoNormalizado
            );

            Cliente creado = clienteDAO.insertar(nuevoCliente);
            return ClienteResponse.fromModel(creado);

        } catch (DatabaseException e) {
            throw new RuntimeException("Error al registrar el cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public ClienteResponse actualizar(int idCliente, ClienteRequest request) {
        // 1. Verificar existencia previa
        Cliente existente;
        try {
            existente = clienteDAO.consultarPorId(idCliente)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar: Cliente con ID " + idCliente + " no existe."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error de consulta previa al actualizar cliente: " + e.getMessage(), e);
        }

        String tipoDocNormalizado = request.getTipoDoc().trim().toUpperCase();
        String numDocNormalizado = request.getNumDoc().trim().toUpperCase();
        String nombresNormalizados = request.getNombres().trim();
        String correoNormalizado = request.getCorreo().trim().toLowerCase();

        // 2. Verificar que no choque con el documento de OTRO cliente
        try {
            Optional<Cliente> porDocumento = clienteDAO.consultarPorDocumento(tipoDocNormalizado, numDocNormalizado);
            if (porDocumento.isPresent() && !porDocumento.get().getIdCliente().equals(idCliente)) {
                throw new IntegrityConflictException("Conflicto: El documento " + tipoDocNormalizado + " " 
                        + numDocNormalizado + " ya pertenece a otro cliente registrado (ID " + porDocumento.get().getIdCliente() + ").");
            }

            existente.setTipoDoc(tipoDocNormalizado);
            existente.setNumDoc(numDocNormalizado);
            existente.setNombres(nombresNormalizados);
            existente.setCorreo(correoNormalizado);

            boolean actualizado = clienteDAO.actualizar(existente);
            if (!actualizado) {
                throw new RuntimeException("La actualización no afectó ninguna fila.");
            }

            return ClienteResponse.fromModel(existente);

        } catch (DatabaseException e) {
            throw new RuntimeException("Error al actualizar el cliente con ID " + idCliente + ": " + e.getMessage(), e);
        }
    }
}
