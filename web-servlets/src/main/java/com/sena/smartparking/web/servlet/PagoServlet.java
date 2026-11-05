package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.PagoDAO;
import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.dao.impl.PagoDAOImpl;
import com.sena.smartparking.web.dao.impl.RegistroParqueoDAOImpl;
import com.sena.smartparking.web.model.Pago;
import com.sena.smartparking.web.model.RegistroParqueo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador Jakarta Servlet para el modulo de Pagos (/pagos).
 * Implementa manejo de parametros por GET (filtrado por id_registro) y POST (registro validando FK fk_pago_registro).
 */
@WebServlet(name = "PagoServlet", urlPatterns = {"/pagos"})
public class PagoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String VISTA_PAGOS = "/WEB-INF/views/pagos.jsp";

    private static final Set<String> ESTADOS_PAGO_VALIDOS = Set.of("APROBADO", "PAGADO", "PENDIENTE", "RECHAZADO");

    private final PagoDAO pagoDAO;
    private final RegistroParqueoDAO registroParqueoDAO;

    public PagoServlet() {
        this(new PagoDAOImpl(), new RegistroParqueoDAOImpl());
    }

    public PagoServlet(PagoDAO pagoDAO, RegistroParqueoDAO registroParqueoDAO) {
        this.pagoDAO = pagoDAO;
        this.registroParqueoDAO = registroParqueoDAO;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String idRegistroRaw = limpiarParametro(request.getParameter("idRegistro"));
        request.setAttribute("idRegistroFiltro", idRegistroRaw);

        Integer idRegistroFiltro = null;
        if (!idRegistroRaw.isEmpty()) {
            try {
                idRegistroFiltro = Integer.valueOf(idRegistroRaw);
                if (idRegistroFiltro <= 0) {
                    throw new NumberFormatException("ID no positivo");
                }
            } catch (NumberFormatException ex) {
                request.setAttribute("mensajeError",
                        "El identificador de registro para filtrar debe ser un numero entero positivo.");
                idRegistroFiltro = null;
            }
        }

        cargarDatosEnRequest(request, idRegistroFiltro);

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_PAGOS);
        dispatcher.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String idRegistroRaw = limpiarParametro(request.getParameter("idRegistro"));
        String valorPagadoRaw = limpiarParametro(request.getParameter("valorPagado"));
        String fechaPagoRaw = limpiarParametro(request.getParameter("fechaPago"));
        String estadoPagoRaw = limpiarParametro(request.getParameter("estadoPago"));
        String tiempoLimiteRaw = limpiarParametro(request.getParameter("tiempoLimiteSalida"));

        String estadoPago = estadoPagoRaw.isEmpty() ? "APROBADO" : estadoPagoRaw.toUpperCase();

        // 1. Validar idRegistro (entero positivo)
        int idRegistro;
        try {
            if (idRegistroRaw.isEmpty()) {
                throw new NumberFormatException("Vacio");
            }
            idRegistro = Integer.parseInt(idRegistroRaw);
            if (idRegistro <= 0) {
                throw new NumberFormatException("No positivo");
            }
        } catch (NumberFormatException ex) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "Debe seleccionar o ingresar un ID de registro de parqueo valido (entero positivo).");
            cargarDatosEnRequest(request, null);
            request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
            return;
        }

        // 2. Validar valor pagado
        BigDecimal valorPagado;
        try {
            if (valorPagadoRaw.isEmpty()) {
                throw new NumberFormatException("Vacio");
            }
            valorPagado = new BigDecimal(valorPagadoRaw);
            if (valorPagado.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("No mayor a cero");
            }
        } catch (NumberFormatException ex) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "El valor pagado es obligatorio y debe ser un monto mayor que cero.");
            cargarDatosEnRequest(request, null);
            request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
            return;
        }

        // 3. Validar tiempo limite de salida
        int tiempoLimiteSalida;
        try {
            tiempoLimiteSalida = tiempoLimiteRaw.isEmpty() ? 15 : Integer.parseInt(tiempoLimiteRaw);
            if (tiempoLimiteSalida <= 0 || tiempoLimiteSalida > 1440) {
                throw new NumberFormatException("Rango invalido");
            }
        } catch (NumberFormatException ex) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "El tiempo limite de salida debe ser un numero entero entre 1 y 1440 minutos.");
            cargarDatosEnRequest(request, null);
            request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
            return;
        }

        // 4. Validar estado del pago
        if (!ESTADOS_PAGO_VALIDOS.contains(estadoPago)) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "El estado del pago no es valido. Seleccione APROBADO, PAGADO, PENDIENTE o RECHAZADO.");
            cargarDatosEnRequest(request, null);
            request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
            return;
        }

        // 5. Validar fecha de pago
        LocalDateTime fechaPago;
        try {
            fechaPago = parsearFechaHora(fechaPagoRaw);
        } catch (DateTimeParseException ex) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "El formato de la fecha y hora de pago no es valido.");
            cargarDatosEnRequest(request, null);
            request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
            return;
        }

        try {
            // 6. Comprobar existencia del registro de parqueo relacionado (FK fk_pago_registro)
            Optional<RegistroParqueo> registroRelacionado = registroParqueoDAO.buscarPorId(idRegistro);
            if (registroRelacionado.isEmpty()) {
                preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
                request.setAttribute("mensajeError",
                        "No existe el registro de parqueo con ID #" + idRegistro + ". Seleccione un registro existente.");
                cargarDatosEnRequest(request, null);
                request.getRequestDispatcher(VISTA_PAGOS).forward(request, response);
                return;
            }

            Pago nuevoPago = new Pago(idRegistro, fechaPago, valorPagado, estadoPago, tiempoLimiteSalida);
            int idPagoGenerado = pagoDAO.insertar(nuevoPago);

            request.setAttribute("mensajeExito",
                    "Pago registrado exitosamente con ID #" + idPagoGenerado
                    + " asociado al registro #" + idRegistro
                    + " (Placa: " + registroRelacionado.get().getPlaca() + ").");
        } catch (SQLException ex) {
            preservarDatosFormulario(request, idRegistroRaw, valorPagadoRaw, fechaPagoRaw, estadoPago, tiempoLimiteRaw);
            request.setAttribute("mensajeError",
                    "No se pudo registrar el pago en la base de datos. Verifique la conexion a MySQL.");
        }

        cargarDatosEnRequest(request, null);
        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_PAGOS);
        dispatcher.forward(request, response);
    }

    private void cargarDatosEnRequest(HttpServletRequest request, Integer idRegistroFiltro) {
        try {
            List<Pago> pagos = (idRegistroFiltro != null)
                    ? pagoDAO.listarPorIdRegistro(idRegistroFiltro)
                    : pagoDAO.listarTodos();
            List<RegistroParqueo> registrosDisponibles = registroParqueoDAO.listarTodos();

            request.setAttribute("pagos", pagos);
            request.setAttribute("totalResultados", pagos.size());
            request.setAttribute("registrosDisponibles", registrosDisponibles);
        } catch (SQLException ex) {
            request.setAttribute("pagos", Collections.emptyList());
            request.setAttribute("totalResultados", 0);
            request.setAttribute("registrosDisponibles", Collections.emptyList());
            if (request.getAttribute("mensajeError") == null) {
                request.setAttribute("mensajeError",
                        "Error al consultar los pagos y registros en MySQL.");
            }
        }
    }

    private void preservarDatosFormulario(
            HttpServletRequest request,
            String idRegistro,
            String valorPagado,
            String fechaPago,
            String estadoPago,
            String tiempoLimite) {
        request.setAttribute("formIdRegistro", idRegistro);
        request.setAttribute("formValorPagado", valorPagado);
        request.setAttribute("formFechaPago", fechaPago);
        request.setAttribute("formEstadoPago", estadoPago);
        request.setAttribute("formTiempoLimiteSalida", tiempoLimite);
    }

    private LocalDateTime parsearFechaHora(String valor) {
        if (valor == null || valor.isBlank()) {
            return LocalDateTime.now().withNano(0);
        }
        String limpio = valor.trim();
        if (limpio.length() == 16) {
            return LocalDateTime.parse(limpio, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        }
        return LocalDateTime.parse(limpio.replace(' ', 'T'));
    }

    private String limpiarParametro(String param) {
        return param != null ? param.trim() : "";
    }
}

