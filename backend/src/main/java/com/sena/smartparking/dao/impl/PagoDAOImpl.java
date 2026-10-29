package com.sena.smartparking.dao.impl;

import com.sena.smartparking.config.ConexionBD;
import com.sena.smartparking.dao.PagoDAO;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.Pago;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de PagoDAO utilizando PreparedStatement
 * y control estricto de recursos mediante try-with-resources.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Repository
public class PagoDAOImpl implements PagoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO pago (id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida FROM pago WHERE id_pago = ?";

    private static final String SQL_SELECT_BY_REGISTRO =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida FROM pago WHERE id_registro = ? ORDER BY fecha_pago DESC";

    private static final String SQL_SELECT_ALL =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida FROM pago ORDER BY id_pago DESC";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM pago";

    @Override
    public Pago insertar(Pago pago) throws DatabaseException {
        if (pago == null) {
            throw new DatabaseException("El objeto Pago no puede ser nulo.");
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdRegistro());
            ps.setTimestamp(2, Timestamp.valueOf(pago.getFechaPago()));
            ps.setBigDecimal(3, pago.getValorPagado());
            ps.setString(4, pago.getEstadoPago().trim().toUpperCase());
            ps.setInt(5, pago.getTiempoLimiteSalida());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new DatabaseException("No se pudo insertar el pago: ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pago.setIdPago(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Inserción completada, pero no se obtuvo el id_pago generado.");
                }
            }

            return pago;

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Violación de integridad referencial: El registro de parqueo ID " 
                    + pago.getIdRegistro() + " no existe en la base de datos.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al insertar pago para el registro ID " 
                    + pago.getIdRegistro() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pago> consultarPorId(int idPago) throws DatabaseException {
        if (idPago <= 0) {
            return Optional.empty();
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idPago);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar el pago con ID " + idPago + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pago> consultarPorRegistro(int idRegistro) throws DatabaseException {
        if (idRegistro <= 0) {
            return List.of();
        }

        List<Pago> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_REGISTRO)) {

            ps.setInt(1, idRegistro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

            return lista;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar pagos para el registro ID " + idRegistro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pago> listarTodos() throws DatabaseException {
        List<Pago> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

            return lista;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al listar los pagos: " + e.getMessage(), e);
        }
    }

    @Override
    public long contar() throws DatabaseException {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al contar los pagos: " + e.getMessage(), e);
        }
    }

    private Pago mapearFila(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("fecha_pago");
        LocalDateTime fechaPago = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_registro"),
                fechaPago,
                rs.getBigDecimal("valor_pagado"),
                rs.getString("estado_pago"),
                rs.getInt("tiempo_limite_salida")
        );
    }
}
