package com.sena.smartparking.web.dao;

import com.sena.smartparking.web.model.Cliente;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para operaciones sobre la tabla {@code cliente}.
 */
public interface ClienteDAO {

    int insertar(Cliente cliente) throws SQLException;

    Optional<Cliente> buscarPorId(int idCliente) throws SQLException;

    Optional<Cliente> buscarPorDocumento(String tipoDoc, String numDoc) throws SQLException;

    List<Cliente> listarTodos() throws SQLException;

    List<Cliente> buscarPorFiltros(String tipoDoc, String numDoc) throws SQLException;
}

