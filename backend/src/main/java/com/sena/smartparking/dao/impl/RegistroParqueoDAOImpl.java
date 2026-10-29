package com.sena.smartparking.dao.impl;

import com.sena.smartparking.config.ConexionBD;
import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.RegistroParqueo;

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
 * Implementación JDBC de la interfaz RegistroParqueoDAO.
 * Realiza operaciones CRUD sobre la tabla 'registro_parqueo' usando PreparedStatement
 * y control estricto de recursos mediante try-with-resources.
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
@Repository
public class RegistroParqueoDAOImpl implements RegistroParqueoDAO {

    private static final String SQL_INSERT = 
            "INSERT INTO registro_parqueo (placa, hora_ingreso, valor_calculado, estado_pago) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID = 
            "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago FROM registro_parqueo WHERE id_registro = ?";

    private static final String SQL_SELECT_BY_PLACA = 
            "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago FROM registro_parqueo WHERE placa = ? ORDER BY hora_ingreso DESC";

    private static final String SQL_SELECT_ALL = 
            "SELECT id_registro, placa, hora_ingreso, valor_calculado, estado_pago FROM registro_parqueo ORDER BY id_registro DESC";

    private static final String SQL_UPDATE = 
            "UPDATE registro_parqueo SET placa = ?, hora_ingreso = ?, valor_calculado = ?, estado_pago = ? WHERE id_registro = ?";

    private static final String SQL_DELETE = 
            "DELETE FROM registro_parqueo WHERE id_registro = ?";

    private static final String SQL_COUNT = 
            "SELECT COUNT(*) FROM registro_parqueo";

    @Override
    public RegistroParqueo insertar(RegistroParqueo registro) throws DatabaseException {
        validarRegistro(registro);

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, registro.getPlaca().trim().toUpperCase());
            ps.setTimestamp(2, Timestamp.valueOf(registro.getHoraIngreso()));
            ps.setBigDecimal(3, registro.getValorCalculado());
            ps.setString(4, registro.getEstadoPago().trim().toUpperCase());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new DatabaseException("No se pudo insertar el registro de parqueo: ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    registro.setIdRegistro(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Inserción completada, pero no se obtuvo el identificador generado.");
                }
            }

            return registro;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al insertar registro de parqueo para la placa " 
                    + registro.getPlaca() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<RegistroParqueo> consultarPorId(int idRegistro) throws DatabaseException {
        if (idRegistro <= 0) {
            return Optional.empty();
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, idRegistro);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar el registro de parqueo con ID " 
                    + idRegistro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistroParqueo> consultarPorPlaca(String placa) throws DatabaseException {
        if (placa == null || placa.isBlank()) {
            return List.of();
        }

        List<RegistroParqueo> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_PLACA)) {

            ps.setString(1, placa.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

            return lista;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al consultar registros de parqueo para la placa " 
                    + placa + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistroParqueo> listarTodos() throws DatabaseException {
        List<RegistroParqueo> lista = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

            return lista;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al listar todos los registros de parqueo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(RegistroParqueo registro) throws DatabaseException {
        if (registro == null || registro.getIdRegistro() == null || registro.getIdRegistro() <= 0) {
            throw new DatabaseException("No se puede actualizar un registro de parqueo sin un id_registro válido.");
        }
        validarRegistro(registro);

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, registro.getPlaca().trim().toUpperCase());
            ps.setTimestamp(2, Timestamp.valueOf(registro.getHoraIngreso()));
            ps.setBigDecimal(3, registro.getValorCalculado());
            ps.setString(4, registro.getEstadoPago().trim().toUpperCase());
            ps.setInt(5, registro.getIdRegistro());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al actualizar el registro de parqueo con ID " 
                    + registro.getIdRegistro() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int idRegistro) throws DatabaseException {
        if (idRegistro <= 0) {
            throw new DatabaseException("El identificador a eliminar debe ser un entero positivo.");
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, idRegistro);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Manejo específico del error de clave foránea (FK fk_pago_registro)
            throw new DatabaseException("Restricción de Integridad Referencial: No es posible eliminar el registro de parqueo ID " 
                    + idRegistro + " porque tiene pagos asociados en la tabla 'pago'. "
                    + "Por seguridad y auditoría, no se eliminan registros relacionados automáticamente.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Error SQL al eliminar el registro de parqueo con ID " 
                    + idRegistro + ": " + e.getMessage(), e);
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
            throw new DatabaseException("Error SQL al contar los registros de parqueo: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea una fila del ResultSet hacia una instancia de RegistroParqueo.
     */
    private RegistroParqueo mapearFila(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("hora_ingreso");
        LocalDateTime horaIngreso = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new RegistroParqueo(
                rs.getInt("id_registro"),
                rs.getString("placa"),
                horaIngreso,
                rs.getBigDecimal("valor_calculado"),
                rs.getString("estado_pago")
        );
    }

    /**
     * Realiza validaciones de negocio e integridad antes de ejecutar operaciones en BD.
     */
    private void validarRegistro(RegistroParqueo registro) throws DatabaseException {
        if (registro == null) {
            throw new DatabaseException("El objeto RegistroParqueo no puede ser nulo.");
        }
        if (registro.getPlaca() == null || registro.getPlaca().isBlank()) {
            throw new DatabaseException("La placa del vehículo es un campo obligatorio.");
        }
        if (registro.getPlaca().trim().length() > 10) {
            throw new DatabaseException("La placa excede el límite máximo de 10 caracteres (recibido: " 
                    + registro.getPlaca().trim().length() + ").");
        }
        if (registro.getHoraIngreso() == null) {
            throw new DatabaseException("La hora de ingreso es un campo obligatorio.");
        }
        if (registro.getValorCalculado() == null) {
            throw new DatabaseException("El valor calculado es obligatorio (use 0 si es tarifa inicial).");
        }
        if (registro.getEstadoPago() == null || registro.getEstadoPago().isBlank()) {
            throw new DatabaseException("El estado de pago es obligatorio (ej. 'PENDIENTE', 'PAGADO').");
        }
        if (registro.getEstadoPago().trim().length() > 20) {
            throw new DatabaseException("El estado de pago excede el límite de 20 caracteres.");
        }
    }
}
