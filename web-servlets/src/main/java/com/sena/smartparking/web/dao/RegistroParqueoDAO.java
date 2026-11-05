package com.sena.smartparking.web.dao;

import com.sena.smartparking.web.model.RegistroParqueo;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para operaciones sobre la tabla {@code registro_parqueo}.
 */
public interface RegistroParqueoDAO {

    int insertar(RegistroParqueo registro) throws SQLException;

    Optional<RegistroParqueo> buscarPorId(int idRegistro) throws SQLException;

    List<RegistroParqueo> listarTodos() throws SQLException;

    List<RegistroParqueo> buscarPorFiltros(String placa, String estadoPago) throws SQLException;
}

