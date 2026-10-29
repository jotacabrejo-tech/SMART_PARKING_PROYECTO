package com.sena.smartparking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la solicitud de registro de pago de parqueadero.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos para el registro de un pago liquidado")
public class PagoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del registro de parqueo asociado (debe existir previamente)", example = "1")
    @NotNull(message = "El id_registro del parqueo es obligatorio")
    @Positive(message = "El id_registro debe ser un entero positivo")
    private Integer idRegistro;

    @Schema(description = "Fecha y hora del pago (opcional, si es nulo se asigna la hora actual)", example = "2026-10-28T08:30:00")
    @PastOrPresent(message = "La fecha de pago no puede ser una fecha futura")
    private LocalDateTime fechaPago;

    @Schema(description = "Monto pagado en pesos colombianos (mayor a cero)", example = "10000.00")
    @NotNull(message = "El valor pagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor pagado debe ser mayor a cero")
    private BigDecimal valorPagado;

    @Schema(description = "Estado del pago", example = "PAGADO", allowableValues = {"APROBADO", "PAGADO", "PENDIENTE", "RECHAZADO"})
    @NotBlank(message = "El estado de pago es obligatorio")
    @Size(max = 20, message = "El estado de pago no debe exceder 20 caracteres")
    @Pattern(regexp = "^(?i)(APROBADO|PAGADO|PENDIENTE|RECHAZADO)$", message = "El estado de pago debe ser APROBADO, PAGADO, PENDIENTE o RECHAZADO")
    private String estadoPago;

    @Schema(description = "Minutos concedidos para abandonar el parqueadero tras el pago", example = "15")
    @NotNull(message = "El tiempo límite de salida es obligatorio")
    @Min(value = 0, message = "El tiempo límite de salida debe ser mayor o igual a 0 minutos")
    @Max(value = 1440, message = "El tiempo límite no puede superar 1440 minutos (24 horas)")
    private Integer tiempoLimiteSalida;

    public PagoRequest() {
    }

    public PagoRequest(Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this.idRegistro = idRegistro;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
        this.estadoPago = estadoPago;
        this.tiempoLimiteSalida = tiempoLimiteSalida;
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
