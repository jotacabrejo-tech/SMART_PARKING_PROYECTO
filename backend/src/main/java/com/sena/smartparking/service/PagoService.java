package com.sena.smartparking.service;

import com.sena.smartparking.dto.PagoRequest;
import com.sena.smartparking.dto.PagoResponse;

import java.util.List;

/**
 * Contrato de servicios de negocio para la gestión de pagos.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
public interface PagoService {

    /**
     * Obtiene el listado histórico completo de pagos registrados.
     */
    List<PagoResponse> listarTodos();

    /**
     * Consulta el detalle de un pago por su ID primario.
     * 
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si el pago no existe.
     */
    PagoResponse consultarPorId(int idPago);

    /**
     * Consulta los pagos asociados a un registro de parqueo específico.
     * 
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si el registro de parqueo no existe.
     */
    List<PagoResponse> consultarPorRegistro(int idRegistro);

    /**
     * Registra un nuevo pago en el sistema.
     * Verifica previamente la existencia del registro de parqueo asociado.
     * 
     * @throws com.sena.smartparking.exception.ResourceNotFoundException Si el registro de parqueo no existe.
     */
    PagoResponse registrarPago(PagoRequest request);
}
