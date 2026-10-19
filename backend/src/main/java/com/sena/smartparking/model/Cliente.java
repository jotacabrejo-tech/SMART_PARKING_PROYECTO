package com.sena.smartparking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'cliente' en la base de datos smart_parking.
 * 
 * Columnas reales:
 * - id_cliente : INT NOT NULL AUTO_INCREMENT (PK)
 * - tipo_doc   : VARCHAR(20) NOT NULL
 * - num_doc    : VARCHAR(20) NOT NULL
 * - nombres    : VARCHAR(100) NOT NULL
 * - correo     : VARCHAR(100) NOT NULL
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idCliente;
    private String tipoDoc;
    private String numDoc;
    private String nombres;
    private String correo;

    public Cliente() {
    }

    public Cliente(String tipoDoc, String numDoc, String nombres, String correo) {
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.correo = correo;
    }

    public Cliente(Integer idCliente, String tipoDoc, String numDoc, String nombres, String correo) {
        this.idCliente = idCliente;
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.correo = correo;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getNumDoc() {
        return numDoc;
    }

    public void setNumDoc(String numDoc) {
        this.numDoc = numDoc;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(idCliente, cliente.idCliente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCliente);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", tipoDoc='" + tipoDoc + '\'' +
                ", numDoc='" + numDoc + '\'' +
                ", nombres='" + nombres + '\'' +
                ", correo='" + correo + '\'' +
                '}';
    }
}
