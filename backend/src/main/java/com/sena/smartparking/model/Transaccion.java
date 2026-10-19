package com.sena.smartparking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'transaccion' en la base de datos smart_parking.
 * Corresponde a la pasarela o método transaccional de pago.
 * 
 * Columnas reales:
 * - id_transaccion  : INT NOT NULL AUTO_INCREMENT (PK)
 * - id_pago         : INT NOT NULL (FK -> pago.id_pago)
 * - cod_autorizacion: VARCHAR(30) NOT NULL
 * - metodo_pago     : VARCHAR(30) NOT NULL (ej. 'EFECTIVO', 'TARJETA', 'TRANSFERENCIA')
 * - estado          : VARCHAR(20) NOT NULL (ej. 'APROBADA', 'RECHAZADA')
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class Transaccion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idTransaccion;
    private Integer idPago;
    private String codAutorizacion;
    private String metodoPago;
    private String estado;

    public Transaccion() {
    }

    public Transaccion(Integer idPago, String codAutorizacion, String metodoPago, String estado) {
        this.idPago = idPago;
        this.codAutorizacion = codAutorizacion;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public Transaccion(Integer idTransaccion, Integer idPago, String codAutorizacion, String metodoPago, String estado) {
        this.idTransaccion = idTransaccion;
        this.idPago = idPago;
        this.codAutorizacion = codAutorizacion;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public Integer getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public String getCodAutorizacion() {
        return codAutorizacion;
    }

    public void setCodAutorizacion(String codAutorizacion) {
        this.codAutorizacion = codAutorizacion;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaccion that = (Transaccion) o;
        return Objects.equals(idTransaccion, that.idTransaccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTransaccion);
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "idTransaccion=" + idTransaccion +
                ", idPago=" + idPago +
                ", codAutorizacion='" + codAutorizacion + '\'' +
                ", metodoPago='" + metodoPago + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
