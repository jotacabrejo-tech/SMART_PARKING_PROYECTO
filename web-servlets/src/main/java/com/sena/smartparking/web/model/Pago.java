package com.sena.smartparking.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo JavaBean que representa la tabla {@code pago} en la base de datos {@code smart_parking}.
 * Mapea estrictamente las columnas: id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida.
 */
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer idPago;
    private Integer idRegistro;
    private LocalDateTime fechaPago;
    private BigDecimal valorPagado;
    private String estadoPago;
    private Integer tiempoLimiteSalida;

    public Pago() {
    }

    public Pago(Integer idPago, Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this.idPago = idPago;
        this.idRegistro = idRegistro;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
        this.estadoPago = estadoPago;
        this.tiempoLimiteSalida = tiempoLimiteSalida;
    }

    public Pago(Integer idRegistro, LocalDateTime fechaPago, BigDecimal valorPagado, String estadoPago, Integer tiempoLimiteSalida) {
        this(null, idRegistro, fechaPago, valorPagado, estadoPago, tiempoLimiteSalida);
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

    public String getFechaPagoFormateada() {
        return fechaPago != null ? fechaPago.format(FORMATO_FECHA) : "";
    }
}

