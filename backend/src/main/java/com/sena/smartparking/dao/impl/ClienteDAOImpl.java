package com.sena.smartparking.dao.impl;

import com.sena.smartparking.config.ConexionBD;
import com.sena.smartparking.dao.ClienteDAO;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.Cliente;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de ClienteDAO utilizando PreparedStatement
 * y control estricto de recursos mediante try-with-resources.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@Repository
public class ClienteDAOImpl implements ClienteDAO {

    private static final String SQL_INSERT =
            "INSERT INTO cliente (tipo_doc, num_doc, nombres, correo) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente WHERE id_cliente = ?";

    private static final String SQL_SELECT_BY_DOCUMENTO =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente WHERE tipo_doc = ? AND num_doc = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_cliente, tipo_doc, num_doc, nombres, correo FROM cliente ORDER BY id_cliente DESC";

    private static final String SQL_UPDATE =
            "UPDATE cliente SET tipo_doc = ?, num_doc = ?, nombres = ?, correo = ? WHERE id_cliente = ?";

    private static final String SQL_DELETE =
            "DELETE FROM cliente WHERE id_cliente = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM cliente";

    @Override
    public Cliente insertar(Cliente cliente) throws DatabaseException {
        if (cliente == null) {
            throw new DatabaseException("El objeto Cliente no puede ser nulo.");
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getTipoDoc().trim().toUpperCase());
            ps.setString(2, cliente.getNumDoc().trim().toUpperCase());
            ps.setString(3, cliente.getNombres().trim());
            ps.setString(4, cliente.getCorreo().trim().toLowerCase());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new DatabaseException("No se pudo insertar el cliente: ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setIdCliente(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Inserción de cliente completada, pero no se obtuvo el identificador generado.");
                }
            }

            return cliente;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al insertar cliente con documento " 
                    + cliente.getTipoDoc() + "-" + cliente.getNumDoc() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> consultarPorId(int idCliente) throws DatabaseException {
        if (idCliente <= 0) {
            return Optional.empty();
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar el cliente con ID " + idCliente + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> consultarPorDocumento(String tipoDoc, String numDoc) throws DatabaseException {
        if (tipoDoc == null || tipoDoc.isBlank() || numDoc == null || numDoc.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_DOCUMENTO)) {

            ps.setString(1, tipoDoc.trim().toUpperCase());
            ps.setString(2, numDoc.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar cliente por documento " 
                    + tipoDoc + "-" + numDoc + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> listarTodos() throws DatabaseException {
        List<Cliente> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

            return lista;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al listar los clientes: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) throws DatabaseException {
        if (cliente == null || cliente.getIdCliente() == null || cliente.getIdCliente() <= 0) {
            throw new DatabaseException("No se puede actualizar un cliente sin un id_cliente válido.");
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, cliente.getTipoDoc().trim().toUpperCase());
            ps.setString(2, cliente.getNumDoc().trim().toUpperCase());
            ps.setString(3, cliente.getNombres().trim());
            ps.setString(4, cliente.getCorreo().trim().toLowerCase());
            ps.setInt(5, cliente.getIdCliente());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al actualizar el cliente con ID " 
                    + cliente.getIdCliente() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idCliente) throws DatabaseException {
        if (idCliente <= 0) {
            throw new DatabaseException("El identificador a eliminar debe ser un entero positivo.");
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, idCliente);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Restricción de Integridad Referencial: No es posible eliminar el cliente ID " 
                    + idCliente + " porque tiene facturas asociadas en la tabla 'factura'.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al eliminar el cliente con ID " + idCliente + ": " + e.getMessage(), e);
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
            throw new DatabaseException("Error SQL al contar los clientes: " + e.getMessage(), e);
        }
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
