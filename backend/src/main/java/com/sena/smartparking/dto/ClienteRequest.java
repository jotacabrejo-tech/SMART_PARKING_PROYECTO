package com.sena.smartparking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO para la creación y actualización de información de clientes.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Schema(description = "Datos para el registro o actualización de un cliente")
public class ClienteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Tipo de documento de identificación", example = "CC", allowableValues = {"CC", "CE", "TI", "NIT", "PAS", "PEP"})
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 20, message = "El tipo de documento no debe superar 20 caracteres")
    @Pattern(regexp = "^(?i)(CC|CE|TI|NIT|PAS|PEP)$", message = "El tipo de documento debe ser CC, CE, TI, NIT, PAS o PEP")
    private String tipoDoc;

    @Schema(description = "Número del documento de identificación", example = "1020304050")
    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 3, max = 20, message = "El número de documento debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El número de documento solo debe contener letras, números y guiones")
    private String numDoc;

    @Schema(description = "Nombres y apellidos completos o razón social", example = "Carlos Alberto Mendoza")
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    private String nombres;

    @Schema(description = "Correo electrónico de contacto y facturación", example = "carlos.mendoza@email.com")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
    @Email(message = "El formato del correo electrónico no es válido")
    private String correo;

    public ClienteRequest() {
    }

    public ClienteRequest(String tipoDoc, String numDoc, String nombres, String correo) {
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.correo = correo;
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
