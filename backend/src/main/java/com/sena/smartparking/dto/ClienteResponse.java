package com.sena.smartparking.dto;

import com.sena.smartparking.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * DTO para la respuesta de información de clientes.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos detallados de un cliente registrado")
public class ClienteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Identificador único autoincremental del cliente", example = "1")
    private Integer idCliente;

    @Schema(description = "Tipo de documento de identificación", example = "CC")
    private String tipoDoc;

    @Schema(description = "Número del documento de identificación", example = "1020304050")
    private String numDoc;

    @Schema(description = "Nombres y apellidos completos o razón social", example = "Carlos Alberto Mendoza")
    private String nombres;

    @Schema(description = "Correo electrónico de contacto", example = "carlos.mendoza@email.com")
    private String correo;

    public ClienteResponse() {
    }

    public ClienteResponse(Integer idCliente, String tipoDoc, String numDoc, String nombres, String correo) {
        this.idCliente = idCliente;
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.correo = correo;
    }

    public static ClienteResponse fromModel(Cliente model) {
        if (model == null) {
            return null;
        }
        return new ClienteResponse(
                model.getIdCliente(),
                model.getTipoDoc(),
                model.getNumDoc(),
                model.getNombres(),
                model.getCorreo()
        );
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
