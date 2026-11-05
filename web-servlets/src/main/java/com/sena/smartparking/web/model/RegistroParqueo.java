package com.sena.smartparking.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo JavaBean que representa la tabla {@code registro_parqueo} en la base de datos {@code smart_parking}.
 * Mapea estrictamente las columnas: id_registro, placa, hora_ingreso, valor_calculado, estado_pago.
 */
public class RegistroParqueo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer idRegistro;
    private String placa;
    private LocalDateTime horaIngreso;
    private BigDecimal valorCalculado;
    private String estadoPago;

    public RegistroParqueo() {
    }

    public RegistroParqueo(Integer idRegistro, String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this.idRegistro = idRegistro;
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.valorCalculado = valorCalculado;
        this.estadoPago = estadoPago;
    }

    public RegistroParqueo(String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this(null, placa, horaIngreso, valorCalculado, estadoPago);
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

    public String getHoraIngresoFormateada() {
        return horaIngreso != null ? horaIngreso.format(FORMATO_FECHA) : "";
    }
}

