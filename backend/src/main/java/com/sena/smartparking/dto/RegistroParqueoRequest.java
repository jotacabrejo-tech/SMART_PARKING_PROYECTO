package com.sena.smartparking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la creación y actualización de registros de parqueo.
 * Contiene las validaciones declarativas de negocio y tipos de datos.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos para el registro o actualización de un ingreso de vehículo")
public class RegistroParqueoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Placa del vehículo (3 a 10 caracteres alfanuméricos)", example = "ABC123")
    @NotBlank(message = "La placa es un campo obligatorio")
    @Size(min = 3, max = 10, message = "La placa debe tener entre 3 y 10 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "La placa solo debe contener letras, números y guiones")
    private String placa;

    @Schema(description = "Fecha y hora de ingreso (opcional, si es nulo se asigna la hora actual)", example = "2026-10-27T14:30:00")
    @PastOrPresent(message = "La hora de ingreso no puede ser una fecha futura")
    private LocalDateTime horaIngreso;

    @Schema(description = "Valor inicial o liquidado del cobro (mínimo 0.0)", example = "5000.00")
    @NotNull(message = "El valor calculado es un campo obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El valor calculado no puede ser negativo")
    private BigDecimal valorCalculado;

    @Schema(description = "Estado actual del pago", example = "PENDIENTE", allowableValues = {"PENDIENTE", "PAGADO", "ANULADO"})
    @NotBlank(message = "El estado de pago es un campo obligatorio")
    @Size(max = 20, message = "El estado de pago no debe exceder los 20 caracteres")
    @Pattern(regexp = "^(?i)(PENDIENTE|PAGADO|ANULADO)$", message = "El estado de pago debe ser PENDIENTE, PAGADO o ANULADO")
    private String estadoPago;

    public RegistroParqueoRequest() {
    }

    public RegistroParqueoRequest(String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.valorCalculado = valorCalculado;
        this.estadoPago = estadoPago;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public LocalDateTime getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(LocalDateTime horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public BigDecimal getValorCalculado() {
        return valorCalculado;
    }

    public void setValorCalculado(BigDecimal valorCalculado) {
        this.valorCalculado = valorCalculado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }
}
