package com.sena.smartparking;

import com.sena.smartparking.config.ConexionBD;
import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.dao.impl.RegistroParqueoDAOImpl;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.model.RegistroParqueo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Clase principal ejecutable para verificar la conectividad JDBC y demostrar
 * las cuatro operaciones CRUD sobre la tabla 'registro_parqueo'.
 * 
 * Proyecto: Smart Parking - ADSO SENA
 * Evidencia: GA7-220501096-AA2-EV01 — Codificación de módulos del software
 */
public class Main {

    private static final String LINEA_SEPARADORA = "================================================================================";
    private static final String PLACA_PRUEBA = "TEST-99";

    public static void main(String[] args) {
        imprimirEncabezado();

        try {
            // 1. Probar conectividad con la base de datos MySQL
            System.out.println("\n[PASO 1] Probando conexión JDBC con 'smart_parking'...");
            ConexionBD.probarConexion();

            // 2. Inicializar DAO
            RegistroParqueoDAO dao = new RegistroParqueoDAOImpl();
            long totalInicial = dao.contar();
            System.out.println("\n[PASO 2] Estado inicial de la tabla 'registro_parqueo': " + totalInicial + " registros existentes.");

            // 3. Ejecución controlada del ciclo CRUD de prueba
            System.out.println("\n[PASO 3] Iniciando demostración controlada de operaciones CRUD...");
            ejecutarCicloCrudControlado(dao);

            // 4. Verificación final de integridad
            long totalFinal = dao.contar();
            System.out.println("\n[PASO 4] Verificación de integridad post-pruebas: " + totalFinal + " registros.");
            if (totalInicial == totalFinal) {
                System.out.println(">>> ÉXITO: El estado de la base de datos se conservó íntegro (sin datos huérfanos).");
            } else {
                System.out.println(">>> AVISO: El conteo cambió respecto al inicial.");
            }

            System.out.println("\n" + LINEA_SEPARADORA);
            System.out.println("  EVIDENCIA GA7-220501096-AA2-EV01 COMPLETADA SATISFACTORIAMENTE");
            System.out.println(LINEA_SEPARADORA);

        } catch (DatabaseException e) {
            System.err.println("\n[ERROR FATAL] Ocurrió una excepción en el módulo de base de datos:");
            System.err.println("Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa  : " + e.getCause().getMessage());
            }
        } finally {
            // Cierre ordenado de los recursos en segundo plano del driver JDBC
            ConexionBD.apagarDriver();
        }
    }

    /**
     * Ejecuta las operaciones Create, Read, Update y Delete de forma secuencial y segura,
     * utilizando un registro de prueba aislado que se retira al finalizar.
     */
    private static void ejecutarCicloCrudControlado(RegistroParqueoDAO dao) throws DatabaseException {
        // -------------------------------------------------------------
        // A. CREATE (Inserción)
        // -------------------------------------------------------------
        System.out.println("\n--- [C] CREATE (Inserción de nuevo registro de ingreso) ---");
        LocalDateTime horaEntrada = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        RegistroParqueo nuevoRegistro = new RegistroParqueo(
                PLACA_PRUEBA,
                horaEntrada,
                new BigDecimal("5000.00"),
                "PENDIENTE"
        );

        RegistroParqueo registroCreado = dao.insertar(nuevoRegistro);
        System.out.println("✔ Inserción exitosa. ID generado: " + registroCreado.getIdRegistro());
        System.out.println("  Datos almacenados: " + registroCreado);

        int idGenerado = registroCreado.getIdRegistro();

        // -------------------------------------------------------------
        // B. READ (Consulta por ID y por Placa)
        // -------------------------------------------------------------
        System.out.println("\n--- [R] READ (Consulta por ID y por Placa) ---");
        Optional<RegistroParqueo> consultaOpt = dao.consultarPorId(idGenerado);
        if (consultaOpt.isPresent()) {
            System.out.println("✔ Consulta por ID (" + idGenerado + ") exitosa: " + consultaOpt.get());
        } else {
            throw new DatabaseException("Error de lectura: No se encontró el registro recién insertado.");
        }

        List<RegistroParqueo> porPlaca = dao.consultarPorPlaca(PLACA_PRUEBA);
        System.out.println("✔ Consulta por Placa ('" + PLACA_PRUEBA + "') exitosa: " + porPlaca.size() + " coincidencias encontradas.");

        // -------------------------------------------------------------
        // C. UPDATE (Actualización)
        // -------------------------------------------------------------
        System.out.println("\n--- [U] UPDATE (Actualización de tarifa y estado de pago) ---");
        RegistroParqueo aActualizar = consultaOpt.get();
        aActualizar.setValorCalculado(new BigDecimal("12500.00"));
        aActualizar.setEstadoPago("PAGADO");

        boolean actualizado = dao.actualizar(aActualizar);
        if (actualizado) {
            System.out.println("✔ Actualización exitosa en la base de datos.");
            Optional<RegistroParqueo> verificacion = dao.consultarPorId(idGenerado);
            System.out.println("  Datos tras actualización: " + verificacion.orElse(null));
        } else {
            throw new DatabaseException("Fallo al actualizar el registro con ID: " + idGenerado);
        }

        // -------------------------------------------------------------
        // D. DELETE (Eliminación controlada de registro de prueba)
        // -------------------------------------------------------------
        System.out.println("\n--- [D] DELETE (Eliminación controlada del registro de prueba) ---");
        boolean eliminado = dao.eliminar(idGenerado);
        if (eliminado) {
            System.out.println("✔ Eliminación exitosa del registro de prueba con ID: " + idGenerado);
            Optional<RegistroParqueo> eliminadoOpt = dao.consultarPorId(idGenerado);
            if (eliminadoOpt.isEmpty()) {
                System.out.println("✔ Verificación confirmada: El registro ya no existe en la base de datos.");
            } else {
                throw new DatabaseException("Anomalía: El registro sigue existiendo tras la orden DELETE.");
            }
        } else {
            throw new DatabaseException("Fallo al eliminar el registro con ID: " + idGenerado);
        }
    }

    private static void imprimirEncabezado() {
        System.out.println(LINEA_SEPARADORA);
        System.out.println("         SISTEMA SMART PARKING - MÓDULO JAVA JDBC");
        System.out.println("  Evidencia SENA: GA7-220501096-AA2-EV01 - Codificación de módulos");
        System.out.println("  Centro de Formación: SENA - ADSO | Ficha: 3336134");
        System.out.println(LINEA_SEPARADORA);
    }
}
