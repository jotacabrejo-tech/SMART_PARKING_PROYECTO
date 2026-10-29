package com.sena.smartparking.service.impl;

import com.sena.smartparking.dao.PagoDAO;
import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.dto.PagoRequest;
import com.sena.smartparking.dto.PagoResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.Pago;
import com.sena.smartparking.service.PagoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la capa de servicios para Pago.
 * Valida la existencia del registro de parqueo padre antes de insertar
 * y orquesta las consultas con la capa DAO JDBC.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Service
public class PagoServiceImpl implements PagoService {

    private final PagoDAO pagoDAO;
    private final RegistroParqueoDAO registroParqueoDAO;

    public PagoServiceImpl(PagoDAO pagoDAO, RegistroParqueoDAO registroParqueoDAO) {
        this.pagoDAO = pagoDAO;
        this.registroParqueoDAO = registroParqueoDAO;
    }

    @Override
    public List<PagoResponse> listarTodos() {
        try {
            return pagoDAO.listarTodos().stream()
                    .map(PagoResponse::fromModel)
                    .collect(Collectors.toList());
        } catch (DatabaseException e) {
            throw new RuntimeException("Error en persistencia al listar los pagos: " + e.getMessage(), e);
        }
    }

    @Override
    public PagoResponse consultarPorId(int idPago) {
        if (idPago <= 0) {
            throw new ResourceNotFoundException("El identificador del pago debe ser un entero positivo.");
        }
        try {
            return pagoDAO.consultarPorId(idPago)
                    .map(PagoResponse::fromModel)
                    .orElseThrow(() -> new ResourceNotFoundException("Pago con ID " + idPago + " no encontrado."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al consultar pago con ID " + idPago + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<PagoResponse> consultarPorRegistro(int idRegistro) {
        if (idRegistro <= 0) {
            throw new ResourceNotFoundException("El identificador del registro debe ser un entero positivo.");
        }

        // 1. Verificar existencia del registro de parqueo
        try {
            registroParqueoDAO.consultarPorId(idRegistro)
                    .orElseThrow(() -> new ResourceNotFoundException("Registro de parqueo con ID " + idRegistro + " no encontrado."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al verificar registro de parqueo con ID " + idRegistro + ": " + e.getMessage(), e);
        }

        // 2. Obtener los pagos vinculados
        try {
            return pagoDAO.consultarPorRegistro(idRegistro).stream()
                    .map(PagoResponse::fromModel)
                    .collect(Collectors.toList());
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al consultar pagos para el registro ID " + idRegistro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public PagoResponse registrarPago(PagoRequest request) {
        int idRegistro = request.getIdRegistro();

        // 1. Validar que exista el registro de parqueo asociado
        try {
            registroParqueoDAO.consultarPorId(idRegistro)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede registrar el pago: El registro de parqueo con ID " 
                            + idRegistro + " no existe."));
        } catch (DatabaseException e) {
            throw new RuntimeException("Error al validar registro de parqueo con ID " + idRegistro + ": " + e.getMessage(), e);
        }

        // 2. Normalizar campos
        LocalDateTime fechaPago = (request.getFechaPago() != null)
                ? request.getFechaPago()
                : LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        String estadoPagoNormalizado = request.getEstadoPago().trim().toUpperCase();

        Pago nuevoPago = new Pago(
                idRegistro,
                fechaPago,
                request.getValorPagado(),
                estadoPagoNormalizado,
                request.getTiempoLimiteSalida()
        );

        // 3. Persistir pago en la base de datos
        try {
            Pago guardado = pagoDAO.insertar(nuevoPago);
            return PagoResponse.fromModel(guardado);
        } catch (DatabaseException e) {
            throw new RuntimeException("Fallo al registrar el pago: " + e.getMessage(), e);
        }
    }
}
