package com.sena.smartparking.service.impl;

import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.dto.ConteoResponse;
import com.sena.smartparking.dto.RegistroParqueoRequest;
import com.sena.smartparking.dto.RegistroParqueoResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.RegistroParqueo;
import com.sena.smartparking.service.RegistroParqueoService;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicios para RegistroParqueo.
 * Centraliza la lógica de negocio, validaciones y orquestación con la capa DAO JDBC.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Service
public class RegistroParqueoServiceImpl implements RegistroParqueoService {

    private final RegistroParqueoDAO registroParqueoDAO;

    public RegistroParqueoServiceImpl(RegistroParqueoDAO registroParqueoDAO) {
        this.registroParqueoDAO = registroParqueoDAO;
    }

    @Override
    public List<RegistroParqueoResponse> listarTodos(String placaFiltro) {
        try {
            List<RegistroParqueo> entidades;
            if (placaFiltro != null && !placaFiltro.isBlank()) {
                entidades = registroParqueoDAO.consultarPorPlaca(placaFiltro.trim().toUpperCase());
            } else {
                entidades = registroParqueoDAO.listarTodos();
            }
            return entidades.stream()
                    .map(RegistroParqueoResponse::fromModel)
                    .collect(Collectors.toList());
        } catch (DatabaseException e) {
            throw new RuntimeException("Error en la persistencia al listar registros de parqueo: " + e.getMessage(), e);
        }
    }

    @Override
    public RegistroParqueoResponse consultarPorId(int idRegistro) {
        if (idRegistro <= 0) {
            throw new ResourceNotFoundException("El identificador del registro debe ser un número entero positivo.");
        }
        try {
            return registroParqueoDAO.consultarPorId(idRegistro)
                    .map(RegistroParqueoResponse::fromModel)
                    .orElseThrow(() -> new ResourceNotFoundException("Registro de parqueo con ID " + idRegistro + " no encontrado."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al consultar el registro de parqueo con ID " + idRegistro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public ConteoResponse contarRegistros() {
        try {
            long total = registroParqueoDAO.contar();
            return new ConteoResponse(total);
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al obtener el conteo de registros: " + e.getMessage(), e);
        }
    }

    @Override
    public RegistroParqueoResponse registrarIngreso(RegistroParqueoRequest request) {
        String placaNormalizada = request.getPlaca().trim().toUpperCase();
        String estadoNormalizado = request.getEstadoPago().trim().toUpperCase();
        LocalDateTime horaIngreso = (request.getHoraIngreso() != null)
                ? request.getHoraIngreso()
                : LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        RegistroParqueo nuevoRegistro = new RegistroParqueo(
                placaNormalizada,
                horaIngreso,
                request.getValorCalculado(),
                estadoNormalizado
        );

        try {
            RegistroParqueo creado = registroParqueoDAO.insertar(nuevoRegistro);
            return RegistroParqueoResponse.fromModel(creado);
        } catch (DatabaseException e) {
            throw new RuntimeException("Fallo al registrar el ingreso para la placa " + placaNormalizada + ": " + e.getMessage(), e);
        }
    }

    @Override
    public RegistroParqueoResponse actualizar(int idRegistro, RegistroParqueoRequest request) {
        // 1. Verificar existencia previa
        RegistroParqueo existente;
        try {
            existente = registroParqueoDAO.consultarPorId(idRegistro)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar: Registro de parqueo con ID " + idRegistro + " no existe."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error de consulta previa para actualización: " + e.getMessage(), e);
        }

        // 2. Normalizar campos
        String placaNormalizada = request.getPlaca().trim().toUpperCase();
        String estadoNormalizado = request.getEstadoPago().trim().toUpperCase();
        LocalDateTime horaIngreso = (request.getHoraIngreso() != null)
                ? request.getHoraIngreso()
                : existente.getHoraIngreso();

        existente.setPlaca(placaNormalizada);
        existente.setHoraIngreso(horaIngreso);
        existente.setValorCalculado(request.getValorCalculado());
        existente.setEstadoPago(estadoNormalizado);

        try {
            boolean actualizado = registroParqueoDAO.actualizar(existente);
            if (!actualizado) {
                throw new RuntimeException("La actualización no afectó ninguna fila en la base de datos.");
            }
            return RegistroParqueoResponse.fromModel(existente);
        } catch (DatabaseException e) {
            throw new RuntimeException("Fallo al actualizar el registro de parqueo con ID " + idRegistro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int idRegistro) {
        // 1. Verificar existencia previa
        try {
            registroParqueoDAO.consultarPorId(idRegistro)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar: Registro de parqueo con ID " + idRegistro + " no existe."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error de consulta previa para eliminación: " + e.getMessage(), e);
        }

        // 2. Intentar eliminación controlada
        try {
            boolean eliminado = registroParqueoDAO.eliminar(idRegistro);
            if (!eliminado) {
                throw new ResourceNotFoundException("Registro de parqueo con ID " + idRegistro + " no encontrado para eliminación.");
            }
        } catch (DatabaseException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException
                    || (e.getMessage() != null && e.getMessage().contains("Restricción de Integridad Referencial"))) {
                throw new IntegrityConflictException("Restricción de Integridad: No es posible eliminar el registro ID " 
                        + idRegistro + " porque tiene pagos asociados en el histórico.");
            }
            throw new RuntimeException("Error al eliminar el registro ID " + idRegistro + ": " + e.getMessage(), e);
        }
    }
}
