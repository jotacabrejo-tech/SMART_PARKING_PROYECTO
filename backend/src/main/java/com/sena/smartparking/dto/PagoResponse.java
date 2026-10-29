package com.sena.smartparking.dto;

import com.sena.smartparking.model.Pago;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de pagos.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos detallados de un pago registrado")
public class PagoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único autoincremental del pago", example = "1")
    private Integer idPago;

    @Schema(description = "Identificador del registro de parqueo liquidado", example = "10")
    private Integer idRegistro;

    @Schema(description = "Fecha y hora en que se efectuó el pago", example = "2026-10-28T08:30:00")
    private LocalDateTime fechaPago;

    @Schema(description = "Monto pagado", example = "10000.00")
    private BigDecimal valorPagado;

    @Schema(description = "Estado del pago", example = "PAGADO")
    private String estadoPago;

    @Schema(description = "Minutos concedidos para abandonar las instalaciones", example = "15")
    private Integer tiempoLimiteSalida;

    public PagoResponse() {
    }

    public PagoResponse(Integer idPago, Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this.idPago = idPago;
        this.idRegistro = idRegistro;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
        this.estadoPago = estadoPago;
        this.tiempoLimiteSalida = tiempoLimiteSalida;
    }

    public static PagoResponse fromModel(Pago model) {
        if (model == null) {
            return null;
        }
        return new PagoResponse(
                model.getIdPago(),
                model.getIdRegistro(),
                model.getFechaPago(),
                model.getValorPagado(),
                model.getEstadoPago(),
                model.getTiempoLimiteSalida()
        );
    }

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public BigDecimal getValorPagado() {
        return valorPagado;
    }

    public void setValorPagado(BigDecimal valorPagado) {
        this.valorPagado = valorPagado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Integer getTiempoLimiteSalida() {
        return tiempoLimiteSalida;
    }

    public void setTiempoLimiteSalida(Integer tiempoLimiteSalida) {
        this.tiempoLimiteSalida = tiempoLimiteSalida;
    }
}
