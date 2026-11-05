package com.sena.smartparking.web.dao.impl;

import com.sena.smartparking.web.config.ConexionBD;
import com.sena.smartparking.web.dao.ClienteDAO;
import com.sena.smartparking.web.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion JDBC de {@link ClienteDAO} usando PreparedStatement y try-with-resources.
 * Mapea estrictamente las columnas id_cliente, tipo_doc, num_doc, nombres, correo de la tabla {@code cliente}.
 */
public class ClienteDAOImpl implements ClienteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO cliente (tipo_doc, num_doc, nombres, correo) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente WHERE id_cliente = ?";

    private static final String SQL_SELECT_BY_DOC =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente "
            + "WHERE UPPER(tipo_doc) = UPPER(?) AND num_doc = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente ORDER BY id_cliente DESC";

    private final ConexionBD conexionBD;

    public ClienteDAOImpl() {
        this(new ConexionBD());
    }

    public ClienteDAOImpl(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public int insertar(Cliente cliente) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getTipoDoc());
            ps.setString(2, cliente.getNumDoc());
            ps.setString(3, cliente.getNombres());
            ps.setString(4, cliente.getCorreo());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    cliente.setIdCliente(idGenerado);
                    return idGenerado;
                }
            }
            return 0;
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int idCliente) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String tipoDoc, String numDoc) throws SQLException {
        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_DOC)) {

            ps.setString(1, tipoDoc != null ? tipoDoc.trim() : "");
            ps.setString(2, numDoc != null ? numDoc.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
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
    public List<Cliente> buscarPorFiltros(String tipoDoc, String numDoc) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente WHERE 1=1");
        List<String> parametros = new ArrayList<>();

        if (tipoDoc != null && !tipoDoc.isBlank()) {
            sql.append(" AND UPPER(tipo_doc) = ?");
            parametros.add(tipoDoc.trim().toUpperCase());
        }
        if (numDoc != null && !numDoc.isBlank()) {
            sql.append(" AND num_doc LIKE ?");
            parametros.add("%" + numDoc.trim() + "%");
        }
        sql.append(" ORDER BY id_cliente DESC");

        List<Cliente> lista = new ArrayList<>();
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

    private Cliente mapearFila(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("tipo_doc"),
                rs.getString("num_doc"),
                rs.getString("nombres"),
                rs.getString("correo")
        );
    }
}

