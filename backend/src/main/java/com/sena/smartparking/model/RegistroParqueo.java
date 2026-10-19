package com.sena.smartparking.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad modelo que representa la tabla 'registro_parqueo' en la base de datos smart_parking.
 * Corresponde al registro de entrada de vehículos y cálculo de cobro en el parqueadero.
 * 
 * Columnas reales:
 * - id_registro    : INT NOT NULL AUTO_INCREMENT (PK)
 * - placa          : VARCHAR(10) NOT NULL
 * - hora_ingreso   : DATETIME NOT NULL
 * - valor_calculado: DECIMAL(10,2) NOT NULL
 * - estado_pago    : VARCHAR(20) NOT NULL
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public class RegistroParqueo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idRegistro;
    private String placa;
    private LocalDateTime horaIngreso;
    private BigDecimal valorCalculado;
    private String estadoPago;

    /**
     * Constructor por defecto.
     */
    public RegistroParqueo() {
    }

    /**
     * Constructor para creación de nuevos registros (sin identificador primario asignado).
     * 
     * @param placa Placa del vehículo (máximo 10 caracteres).
     * @param horaIngreso Fecha y hora exacta de entrada al parqueadero.
     * @param valorCalculado Monto liquidado o estimado de cobro.
     * @param estadoPago Estado del pago (ej. 'PENDIENTE', 'PAGADO', 'ANULADO').
     */
    public RegistroParqueo(String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.valorCalculado = valorCalculado;
        this.estadoPago = estadoPago;
    }

    /**
     * Constructor completo con identificador primario (usado al recuperar de la base de datos).
     * 
     * @param idRegistro Identificador único autoincremental.
     * @param placa Placa del vehículo.
     * @param horaIngreso Fecha y hora exacta de entrada.
     * @param valorCalculado Monto liquidado de cobro.
     * @param estadoPago Estado del pago.
     */
    public RegistroParqueo(Integer idRegistro, String placa, LocalDateTime horaIngreso, BigDecimal valorCalculado, String estadoPago) {
        this.idRegistro = idRegistro;
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.valorCalculado = valorCalculado;
        this.estadoPago = estadoPago;
    }

    // --- Getters y Setters ---

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

    // --- Métodos de Object ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroParqueo that = (RegistroParqueo) o;
        return Objects.equals(idRegistro, that.idRegistro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRegistro);
    }

    @Override
    public String toString() {
        return "RegistroParqueo{" +
                "idRegistro=" + idRegistro +
                ", placa='" + placa + '\'' +
                ", horaIngreso=" + horaIngreso +
                ", valorCalculado=" + valorCalculado +
                ", estadoPago='" + estadoPago + '\'' +
                '}';
    }
}
