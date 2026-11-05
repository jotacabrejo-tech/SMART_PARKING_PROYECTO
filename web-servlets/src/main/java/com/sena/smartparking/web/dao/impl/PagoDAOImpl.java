package com.sena.smartparking.web.dao.impl;

import com.sena.smartparking.web.config.ConexionBD;
import com.sena.smartparking.web.dao.PagoDAO;
import com.sena.smartparking.web.model.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link PagoDAO} usando PreparedStatement y try-with-resources.
 * Mapea estrictamente las columnas id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida.
 */
public class PagoDAOImpl implements PagoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO pago (id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida "
            + "FROM pago WHERE id_pago = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida "
            + "FROM pago ORDER BY id_pago DESC";

    private static final String SQL_SELECT_BY_REGISTRO =
            "SELECT id_pago, id_registro, fecha_pago, valor_pagado, estado_pago, tiempo_limite_salida "
            + "FROM pago WHERE id_registro = ? ORDER BY id_pago DESC";

    private final ConexionBD conexionBD;

    public PagoDAOImpl() {
        this(new ConexionBD());
    }

    public PagoDAOImpl(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public int insertar(Pago pago) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdRegistro());
            ps.setTimestamp(2, Timestamp.valueOf(pago.getFechaPago()));
            ps.setBigDecimal(3, pago.getValorPagado());
            ps.setString(4, pago.getEstadoPago());
            ps.setInt(5, pago.getTiempoLimiteSalida());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    pago.setIdPago(idGenerado);
                    return idGenerado;
                }
            }
            return 0;
        }
    }

    @Override
    public Optional<Pago> buscarPorId(int idPago) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Pago> listarTodos() throws SQLException {
        List<Pago> lista = new ArrayList<>();
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Pago> listarPorIdRegistro(int idRegistro) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_REGISTRO)) {

            ps.setInt(1, idRegistro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        }
        return lista;
    }

    private Pago mapearFila(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_pago");
        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_registro"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getBigDecimal("valor_pagado"),
                rs.getString("estado_pago"),
                rs.getInt("tiempo_limite_salida")
        );
    }
}

