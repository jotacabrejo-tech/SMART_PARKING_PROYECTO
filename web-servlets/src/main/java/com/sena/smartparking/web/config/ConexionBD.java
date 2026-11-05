package com.sena.smartparking.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor de conexiones JDBC hacia la base de datos MySQL smart_parking
 * para el modulo web de Servlets y JSP (GA7-220501096-AA2-EV02).
 */
public class ConexionBD {

    private static final String ARCHIVO_PROPIEDADES = "db.properties";
    private static final String ARCHIVO_PLANTILLA = "db.properties.example";
    private static final String URL_DEFECTO = "jdbc:mysql://localhost:3306/smart_parking?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true";
    private static final String USUARIO_DEFECTO = "root";
    private static final String DRIVER_DEFECTO = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String user;
    private final String password;
    private final String driver;

    public ConexionBD() {
        this(ARCHIVO_PROPIEDADES);
    }

    public ConexionBD(String resourceName) {
        Properties props = cargarPropiedades(resourceName);

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        String envDriver = System.getenv("DB_DRIVER");

        this.url = (envUrl != null && !envUrl.isBlank())
                ? envUrl.trim()
                : props.getProperty("db.url", URL_DEFECTO).trim();

        this.user = (envUser != null && !envUser.isBlank())
                ? envUser.trim()
                : props.getProperty("db.user", USUARIO_DEFECTO).trim();

        this.password = (envPass != null)
                ? envPass
                : props.getProperty("db.password", "");

        this.driver = (envDriver != null && !envDriver.isBlank())
                ? envDriver.trim()
                : props.getProperty("db.driver", DRIVER_DEFECTO).trim();

        registrarDriver();
    }

    public ConexionBD(String url, String user, String password) {
        this.url = (url != null && !url.isBlank()) ? url.trim() : URL_DEFECTO;
        this.user = (user != null && !user.isBlank()) ? user.trim() : USUARIO_DEFECTO;
        this.password = (password != null) ? password : "";
        this.driver = DRIVER_DEFECTO;
        registrarDriver();
    }

    private Properties cargarPropiedades(String resourceName) {
        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ConexionBD.class.getClassLoader();
        }

        try (InputStream in = cl.getResourceAsStream(resourceName)) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (IOException ignored) {
            // Se usa respaldo de plantilla o valores por defecto sin exponer detalles sensibles
        }

        try (InputStream fallback = cl.getResourceAsStream(ARCHIVO_PLANTILLA)) {
            if (fallback != null) {
                props.load(fallback);
            }
        } catch (IOException ignored) {
            // Se mantienen valores por defecto
        }
        return props;
    }

    private void registrarDriver() {
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontro el driver JDBC de MySQL en el classpath.", e);
        }
    }

    /**
     * Obtiene una nueva conexion activa hacia la base de datos smart_parking.
     *
     * @return instancia de {@link Connection}
     * @throws SQLException si ocurre un error al conectar con MySQL
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(this.url, this.user, this.password);
        } catch (SQLException ex) {
            throw new SQLException("Error al establecer conexion con la base de datos smart_parking. Verifique que el servicio MySQL este activo y db.properties este configurado.", ex.getSQLState(), ex.getErrorCode());
        }
    }

    /**
     * Cierra de forma segura recursos JDBC adicionales cuando no se usa try-with-resources.
     *
     * @param recursos recursos AutoCloseable (Connection, Statement, ResultSet)
     */
    public static void cerrar(AutoCloseable... recursos) {
        if (recursos == null) {
            return;
        }
        for (AutoCloseable recurso : recursos) {
            if (recurso != null) {
                try {
                    recurso.close();
                } catch (Exception ignored) {
                    // Cierre silencioso seguro
                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getDriver() {
        return driver;
    }
}

