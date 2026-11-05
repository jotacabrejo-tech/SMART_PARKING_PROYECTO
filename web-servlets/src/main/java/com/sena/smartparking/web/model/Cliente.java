package com.sena.smartparking.web.model;

import java.io.Serializable;

/**
 * Modelo JavaBean que representa la tabla {@code cliente} en la base de datos {@code smart_parking}.
 * Mapea estrictamente las columnas: id_cliente, tipo_doc, num_doc, nombres, correo.
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

    public Cliente(Integer idCliente, String tipoDoc, String numDoc, String nombres, String correo) {
        this.idCliente = idCliente;
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.correo = correo;
    }

    public Cliente(String tipoDoc, String numDoc, String nombres, String correo) {
        this(null, tipoDoc, numDoc, nombres, correo);
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
}

