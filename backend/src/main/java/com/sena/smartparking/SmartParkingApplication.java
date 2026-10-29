package com.sena.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Clase principal de inicio para el backend de Smart Parking.
 * Arranca el servidor embebido Spring Boot en el puerto 8080 y habilita
 * el escaneo de componentes en el paquete com.sena.smartparking.
 * 
 * Se excluye DataSourceAutoConfiguration para mantener la gestión de conexión
 * mediante la clase desacoplada ConexionBD (JDBC nativo) definida en el proyecto.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmartParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);
    }
}
