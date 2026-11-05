package com.sena.smartparking.web.dao;

import com.sena.smartparking.web.model.Pago;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para operaciones sobre la tabla {@code pago}.
 */
public interface PagoDAO {

    int insertar(Pago pago) throws SQLException;

    Optional<Pago> buscarPorId(int idPago) throws SQLException;

    List<Pago> listarTodos() throws SQLException;

    List<Pago> listarPorIdRegistro(int idRegistro) throws SQLException;
}

