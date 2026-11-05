package com.sena.smartparking.web.dao;

import com.sena.smartparking.web.config.ConexionBD;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link ConexionBD} sin depender de datos reales de produccion.
 */
class ConexionBDTest {

    @Test
    @DisplayName("Debe cargar la configuracion desde db.properties.example cuando no existe el recurso indicado")
    void debeCargarConfiguracionDesdePlantilla() {
        ConexionBD conexion = new ConexionBD("archivo_inexistente_test.properties");

        assertNotNull(conexion.getUrl());
        assertTrue(conexion.getUrl().contains("jdbc:mysql://"));
        assertTrue(conexion.getUrl().contains("smart_parking"));
        assertEquals("com.mysql.cj.jdbc.Driver", conexion.getDriver());
        assertNotNull(conexion.getUser());
    }

    @Test
    @DisplayName("Debe permitir inicializar ConexionBD con parametros explicitos y aplicar valores por defecto si estan vacios")
    void debeInicializarConParametrosExplicitos() {
        ConexionBD conexionPersonalizada = new ConexionBD("jdbc:mysql://localhost:3306/smart_parking", "usuario_test", "clave_test");
        assertEquals("jdbc:mysql://localhost:3306/smart_parking", conexionPersonalizada.getUrl());
        assertEquals("usuario_test", conexionPersonalizada.getUser());
        assertEquals("com.mysql.cj.jdbc.Driver", conexionPersonalizada.getDriver());

        ConexionBD conexionPorDefecto = new ConexionBD("", "", null);
        assertTrue(conexionPorDefecto.getUrl().contains("smart_parking"));
        assertEquals("root", conexionPorDefecto.getUser());
    }

    @Test
    @DisplayName("Debe gestionar errores de conexion sin exponer credenciales en el mensaje de excepcion")
    void debeGestionarErrorDeConexionSinExponerCredenciales() {
        String claveSecreta = "CLAVE_SUPER_SECRETA_NO_EXPONER_999";
        ConexionBD conexionInvalida = new ConexionBD("jdbc:mysql://localhost:65530/smart_parking?connectTimeout=250", "usuario_invalido", claveSecreta);

        SQLException ex = assertThrows(SQLException.class, conexionInvalida::getConnection);
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().contains(claveSecreta), "El mensaje de error nunca debe exponer la contrasena");
        assertTrue(ex.getMessage().contains("smart_parking"));
    }

    @Test
    @DisplayName("Debe cerrar recursos AutoCloseable de manera segura incluso con valores nulos")
    void debeCerrarRecursosDeFormaSegura() {
        final boolean[] cerrado = {false};
        AutoCloseable recursoSimulado = () -> cerrado[0] = true;

        assertDoesNotThrow(() -> ConexionBD.cerrar(null, recursoSimulado));
        assertTrue(cerrado[0]);
    }
}

