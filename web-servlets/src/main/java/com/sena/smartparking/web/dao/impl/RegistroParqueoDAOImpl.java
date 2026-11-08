package com.sena.smartparking.web.dao.impl;

import com.sena.smartparking.web.config.ConexionBD;
import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.model.RegistroParqueo;

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
 * Implementacion JDBC de {@link RegistroParqueoDAO} usando PreparedStatement y try-with-resources.
 * Mapea estrictamente la columna {@code valor_calculado} de la tabla {@code registro_parqueo}.
 */
public class RegistroParqueoDAOImpl implements RegistroParqueoDAO {

    private static final String SQL_INSERT =
            "INSERT INTO registro_parqueo (placa, hora_ingreso, valor_calculado, estado_pago) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago "
            + "FROM registro_parqueo WHERE id_registro = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago "
            + "FROM registro_parqueo ORDER BY id_registro DESC";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE registro_parqueo SET estado_pago = ? WHERE id_registro = ?";

    private final ConexionBD conexionBD;

    public RegistroParqueoDAOImpl() {
        this(new ConexionBD());
    }

    public RegistroParqueoDAOImpl(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public int insertar(RegistroParqueo registro) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, registro.getPlaca());
            ps.setTimestamp(2, Timestamp.valueOf(registro.getHoraIngreso()));
            ps.setBigDecimal(3, registro.getValorCalculado());
            ps.setString(4, registro.getEstadoPago());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    registro.setIdRegistro(idGenerado);
                    return idGenerado;
                }
            }
            return 0;
        }
    }

    @Override
    public Optional<RegistroParqueo> buscarPorId(int idRegistro) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idRegistro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public List<RegistroParqueo> listarTodos() throws SQLException {
        List<RegistroParqueo> lista = new ArrayList<>();
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
    public List<RegistroParqueo> buscarPorFiltros(String placa, String estadoPago) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago "
                + "FROM registro_parqueo WHERE 1=1");
        List<String> parametros = new ArrayList<>();

        if (placa != null && !placa.isBlank()) {
            sql.append(" AND UPPER(placa) LIKE ?");
            parametros.add("%" + placa.trim().toUpperCase() + "%");
        }
        if (estadoPago != null && !estadoPago.isBlank()) {
            sql.append(" AND UPPER(estado_pago) = ?");
            parametros.add(estadoPago.trim().toUpperCase());
        }
        sql.append(" ORDER BY id_registro DESC");

        List<RegistroParqueo> lista = new ArrayList<>();
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setString(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public boolean actualizarEstadoPago(int idRegistro, String estadoPago) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ESTADO)) {

            ps.setString(1, estadoPago.trim().toUpperCase());
            ps.setInt(2, idRegistro);

            return ps.executeUpdate() > 0;
        }
    }

    private RegistroParqueo mapearFila(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("hora_ingreso");
        return new RegistroParqueo(
                rs.getInt("id_registro"),
                rs.getString("placa"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getBigDecimal("valor_calculado"),
                rs.getString("estado_pago")
        );
    }
}

