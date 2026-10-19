package com.sena.smartparking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'comprobante' en la base de datos smart_parking.
 * Corresponde al recibo o ticket de soporte para el usuario.
 * 
 * Columnas reales:
 * - id_comprobante : INT NOT NULL AUTO_INCREMENT (PK)
 * - id_pago        : INT NOT NULL (FK -> pago.id_pago)
 * - cod_comprobante: VARCHAR(30) NOT NULL
 * - url_descarga   : VARCHAR(255) NOT NULL
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class Comprobante implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idComprobante;
    private Integer idPago;
    private String codComprobante;
    private String urlDescarga;

    public Comprobante() {
    }

    public Comprobante(Integer idPago, String codComprobante, String urlDescarga) {
        this.idPago = idPago;
        this.codComprobante = codComprobante;
        this.urlDescarga = urlDescarga;
    }

    public Comprobante(Integer idComprobante, Integer idPago, String codComprobante, String urlDescarga) {
        this.idComprobante = idComprobante;
        this.idPago = idPago;
        this.codComprobante = codComprobante;
        this.urlDescarga = urlDescarga;
    }

    public Integer getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Integer idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public String getCodComprobante() {
        return codComprobante;
    }

    public void setCodComprobante(String codComprobante) {
        this.codComprobante = codComprobante;
    }

    public String getUrlDescarga() {
        return urlDescarga;
    }

    public void setUrlDescarga(String urlDescarga) {
        this.urlDescarga = urlDescarga;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comprobante that = (Comprobante) o;
        return Objects.equals(idComprobante, that.idComprobante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idComprobante);
    }

    @Override
    public String toString() {
        return "Comprobante{" +
                "idComprobante=" + idComprobante +
                ", idPago=" + idPago +
                ", codComprobante='" + codComprobante + '\'' +
                ", urlDescarga='" + urlDescarga + '\'' +
                '}';
    }
}
