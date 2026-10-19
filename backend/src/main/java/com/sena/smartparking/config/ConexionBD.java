package com.sena.smartparking.config;

import com.sena.smartparking.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor centralizado de conexiones JDBC a la base de datos smart_parking.
 * 
 * Implementa la carga desacoplada y segura de credenciales desde el archivo
 * local de configuración 'db.properties' ubicado en el classpath.
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01
 */
public final class ConexionBD {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static String url;
    private static String user;
    private static String password;
    private static boolean configurado = false;

    // Bloque estático para cargar driver y propiedades al iniciar la clase
    static {
        inicializarConfiguracion();
    }

    private ConexionBD() {
        // Constructor privado para evitar instanciación directa (clase utilitaria)
    }

    /**
     * Carga las propiedades de conexión desde db.properties en resources.
     */
    private static synchronized void inicializarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("[ConexionBD] AVISO: No se encontró el archivo '" + PROPERTIES_FILE + "' en el classpath.");
                System.err.println("[ConexionBD] Por favor, copie 'db.properties.example' como 'db.properties' y configure sus credenciales.");
                configurado = false;
                return;
            }

            props.load(input);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // Validar que los campos obligatorios estén presentes
            if (url == null || url.isBlank() || user == null || user.isBlank()) {
                System.err.println("[ConexionBD] ERROR: 'db.url' o 'db.user' no están definidos en " + PROPERTIES_FILE);
                configurado = false;
                return;
            }

            // Registrar el driver JDBC
            Class.forName(DRIVER_CLASS);
            configurado = true;

        } catch (IOException e) {
            System.err.println("[ConexionBD] Error de E/S al leer " + PROPERTIES_FILE + ": " + e.getMessage());
            configurado = false;
        } catch (ClassNotFoundException e) {
            System.err.println("[ConexionBD] Error al cargar el driver JDBC (" + DRIVER_CLASS + "): " + e.getMessage());
            configurado = false;
        }
    }

    /**
     * Obtiene una conexión activa hacia la base de datos MySQL.
     * 
     * @return Instancia de java.sql.Connection abierta.
     * @throws DatabaseException Si no se encuentra la configuración o falla la conexión.
     */
    public static Connection getConnection() throws DatabaseException {
        if (!configurado) {
            // Intentar reconfigurar en caso de creación dinámica
            inicializarConfiguracion();
            if (!configurado) {
                throw new DatabaseException("La configuración de la base de datos no está disponible. "
                        + "Verifique que 'backend/src/main/resources/" + PROPERTIES_FILE + "' exista con credenciales válidas.");
            }
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Error al establecer la conexión con la base de datos (" 
                    + url + ", usuario=" + user + "): " + e.getMessage(), e);
        }
    }

    /**
     * Alias en español para obtener la conexión.
     * 
     * @return java.sql.Connection
     * @throws DatabaseException en caso de fallo.
     */
    public static Connection obtenerConexion() throws DatabaseException {
        return getConnection();
    }

    /**
     * Ejecuta una prueba de conectividad y muestra los metadatos básicos del servidor.
     * 
     * @throws DatabaseException Si la conexión no puede establecerse.
     */
    public static void probarConexion() throws DatabaseException {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[ConexionBD] Conexión establecida exitosamente con MySQL.");
                System.out.println("[ConexionBD] Base de Datos : " + conn.getCatalog());
                System.out.println("[ConexionBD] Motor RDBMS   : " + conn.getMetaData().getDatabaseProductName() 
                        + " v" + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("[ConexionBD] Driver JDBC   : " + conn.getMetaData().getDriverName() 
                        + " v" + conn.getMetaData().getDriverVersion());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fallo al verificar el estado de la conexión: " + e.getMessage(), e);
        }
    }

    /**
     * Libera los recursos del driver JDBC de MySQL (AbandonedConnectionCleanupThread)
     * para asegurar un cierre limpio e inmediato en ejecuciones desde consola y Maven.
     */
    public static void apagarDriver() {
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception ignored) {
            // Silencioso: no afecta el funcionamiento si ya se detuvo
        }
    }

    /**
     * Indica si los parámetros de conexión están cargados correctamente.
     * 
     * @return true si está configurado, false de lo contrario.
     */
    public static boolean isConfigurado() {
        return configurado;
    }
}
