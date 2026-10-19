package com.sena.smartparking.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'pago' en la base de datos smart_parking.
 * 
 * Columnas reales:
 * - id_pago              : INT NOT NULL AUTO_INCREMENT (PK)
 * - id_registro          : INT NOT NULL (FK -> registro_parqueo.id_registro)
 * - fecha_pago           : DATETIME NOT NULL
 * - valor_pagado         : DECIMAL(10,2) NOT NULL
 * - estado_pago          : VARCHAR(20) NOT NULL
 * - tiempo_limite_salida : INT NOT NULL (minutos concedidos para abandonar las instalaciones)
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idPago;
    private Integer idRegistro;
    private LocalDateTime fechaPago;
    private BigDecimal valorPagado;
    private String estadoPago;
    private Integer tiempoLimiteSalida;

    public Pago() {
    }

    public Pago(Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this.idRegistro = idRegistro;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
        this.estadoPago = estadoPago;
        this.tiempoLimiteSalida = tiempoLimiteSalida;
    }

    public Pago(Integer idPago, Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this.idPago = idPago;
        this.idRegistro = idRegistro;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
        this.estadoPago = estadoPago;
        this.tiempoLimiteSalida = tiempoLimiteSalida;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pago pago = (Pago) o;
        return Objects.equals(idPago, pago.idPago);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPago);
    }

    @Override
    public String toString() {
        return "Pago{" +
                "idPago=" + idPago +
                ", idRegistro=" + idRegistro +
                ", fechaPago=" + fechaPago +
                ", valorPagado=" + valorPagado +
                ", estadoPago='" + estadoPago + '\'' +
                ", tiempoLimiteSalida=" + tiempoLimiteSalida +
                '}';
    }
}
