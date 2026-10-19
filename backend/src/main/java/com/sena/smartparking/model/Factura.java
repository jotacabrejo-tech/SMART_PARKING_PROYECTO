package com.sena.smartparking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'factura' en la base de datos smart_parking.
 * Corresponde a la facturación electrónica vinculada a pagos y clientes.
 * 
 * Columnas reales:
 * - id_factura     : INT NOT NULL AUTO_INCREMENT (PK)
 * - id_pago        : INT NOT NULL (FK -> pago.id_pago)
 * - id_cliente     : INT NOT NULL (FK -> cliente.id_cliente)
 * - numero_factura : VARCHAR(30) NOT NULL
 * - cufe           : VARCHAR(120) NOT NULL (Código Único de Factura Electrónica)
 * - xml_dian       : VARCHAR(225) NOT NULL
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idFactura;
    private Integer idPago;
    private Integer idCliente;
    private String numeroFactura;
    private String cufe;
    private String xmlDian;

    public Factura() {
    }

    public Factura(Integer idPago, Integer idCliente, String numeroFactura, String cufe, String xmlDian) {
        this.idPago = idPago;
        this.idCliente = idCliente;
        this.numeroFactura = numeroFactura;
        this.cufe = cufe;
        this.xmlDian = xmlDian;
    }

    public Factura(Integer idFactura, Integer idPago, Integer idCliente, String numeroFactura, String cufe, String xmlDian) {
        this.idFactura = idFactura;
        this.idPago = idPago;
        this.idCliente = idCliente;
        this.numeroFactura = numeroFactura;
        this.cufe = cufe;
        this.xmlDian = xmlDian;
    }

    public Integer getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getXmlDian() {
        return xmlDian;
    }

    public void setXmlDian(String xmlDian) {
        this.xmlDian = xmlDian;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factura factura = (Factura) o;
        return Objects.equals(idFactura, factura.idFactura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFactura);
    }

    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", idPago=" + idPago +
                ", idCliente=" + idCliente +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", cufe='" + cufe + '\'' +
                ", xmlDian='" + xmlDian + '\'' +
                '}';
    }
}
