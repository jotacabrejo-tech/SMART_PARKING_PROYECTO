package com.sena.smartparking.dto;

import com.sena.smartparking.model.RegistroParqueo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de datos de registros de parqueo.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos detallados de un registro de parqueo de vehículo")
public class RegistroParqueoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único autoincremental en la base de datos", example = "10")
    private Integer idRegistro;

    @Schema(description = "Placa del vehículo", example = "ABC123")
    private String placa;

    @Schema(description = "Fecha y hora de entrada al parqueadero", example = "2026-10-27T14:30:00")
    private LocalDateTime horaIngreso;

    @Schema(description = "Valor calculado o liquidado del parqueo", example = "5000.00")
    private BigDecimal valorCalculado;

    @Schema(description = "Estado actual del pago", example = "PENDIENTE")
    private String estadoPago;

    public RegistroParqueoResponse() {
    }

    public RegistroParqueoResponse(Integer idRegistro, String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this.idRegistro = idRegistro;
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.valorCalculado = valorCalculado;
        this.estadoPago = estadoPago;
    }

    /**
     * Mapea una entidad modelo RegistroParqueo hacia su DTO de respuesta REST.
     */
    public static RegistroParqueoResponse fromModel(RegistroParqueo model) {
        if (model == null) {
            return null;
        }
        return new RegistroParqueoResponse(
                model.getIdRegistro(),
                model.getPlaca(),
                model.getHoraIngreso(),
                model.getValorCalculado(),
                model.getEstadoPago()
        );
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
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
