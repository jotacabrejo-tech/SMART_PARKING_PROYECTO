package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.dao.impl.RegistroParqueoDAOImpl;
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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Controlador Jakarta Servlet para el modulo de Registros de Parqueo (/parqueos).
 * Implementa manejo de parametros por GET (busqueda/filtrado) y POST (creacion de ingreso).
 */
@WebServlet(name = "RegistroParqueoServlet", urlPatterns = {"/parqueos"})
public class RegistroParqueoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String VISTA_PARQUEOS = "/WEB-INF/views/parqueos.jsp";

    private static final Pattern PATRON_PLACA = Pattern.compile("^[A-Z0-9-]{5,10}$");
    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "PAGADO", "FINALIZADO");

    private final RegistroParqueoDAO registroParqueoDAO;

    public RegistroParqueoServlet() {
        this(new RegistroParqueoDAOImpl());
    }

    public RegistroParqueoServlet(RegistroParqueoDAO registroParqueoDAO) {
        this.registroParqueoDAO = registroParqueoDAO;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String placaFiltro = limpiarParametro(request.getParameter("placa"));
        String estadoFiltro = limpiarParametro(request.getParameter("estadoPago"));

        request.setAttribute("placaFiltro", placaFiltro);
        request.setAttribute("estadoFiltro", estadoFiltro);

        cargarRegistrosEnRequest(request, placaFiltro, estadoFiltro);

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_PARQUEOS);
        dispatcher.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String placaRaw = limpiarParametro(request.getParameter("placa"));
        String valorRaw = limpiarParametro(request.getParameter("valorCalculado"));
        String horaIngresoRaw = limpiarParametro(request.getParameter("horaIngreso"));
        String estadoPagoRaw = limpiarParametro(request.getParameter("estadoPago"));

        String placa = placaRaw.toUpperCase();
        String estadoPago = estadoPagoRaw.isEmpty() ? "PENDIENTE" : estadoPagoRaw.toUpperCase();

        // Validar placa
        if (placa.isEmpty() || !PATRON_PLACA.matcher(placa).matches()) {
            preservarDatosFormulario(request, placa, valorRaw, horaIngresoRaw, estadoPago);
            request.setAttribute("mensajeError",
                    "La placa es obligatoria y debe contener entre 5 y 10 caracteres alfanumericos (ej. ABC123).");
            cargarRegistrosEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_PARQUEOS).forward(request, response);
            return;
        }

        // Validar valor calculado (columna valor_calculado)
        BigDecimal valorCalculado;
        try {
            if (valorRaw.isEmpty()) {
                throw new NumberFormatException("Valor vacio");
            }
            valorCalculado = new BigDecimal(valorRaw);
            if (valorCalculado.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException("Valor negativo");
            }
        } catch (NumberFormatException ex) {
            preservarDatosFormulario(request, placa, valorRaw, horaIngresoRaw, estadoPago);
            request.setAttribute("mensajeError",
                    "El valor calculado es obligatorio y debe ser un numero decimal mayor o igual a 0.");
            cargarRegistrosEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_PARQUEOS).forward(request, response);
            return;
        }

        // Validar estado de pago
        if (!ESTADOS_VALIDOS.contains(estadoPago)) {
            preservarDatosFormulario(request, placa, valorRaw, horaIngresoRaw, estadoPago);
            request.setAttribute("mensajeError",
                    "El estado de pago seleccionado no es valido. Use PENDIENTE, PAGADO o FINALIZADO.");
            cargarRegistrosEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_PARQUEOS).forward(request, response);
            return;
        }

        // Procesar fecha/hora de ingreso
        LocalDateTime horaIngreso;
        try {
            horaIngreso = parsearFechaHora(horaIngresoRaw);
        } catch (DateTimeParseException ex) {
            preservarDatosFormulario(request, placa, valorRaw, horaIngresoRaw, estadoPago);
            request.setAttribute("mensajeError",
                    "El formato de la fecha y hora de ingreso no es valido.");
            cargarRegistrosEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_PARQUEOS).forward(request, response);
            return;
        }

        RegistroParqueo nuevoRegistro = new RegistroParqueo(placa, horaIngreso, valorCalculado, estadoPago);

        try {
            int idGenerado = registroParqueoDAO.insertar(nuevoRegistro);
            request.setAttribute("mensajeExito",
                    "Registro de parqueo creado exitosamente con ID #" + idGenerado + " para la placa " + placa + ".");
        } catch (SQLException ex) {
            preservarDatosFormulario(request, placa, valorRaw, horaIngresoRaw, estadoPago);
            request.setAttribute("mensajeError",
                    "No se pudo guardar el registro de parqueo en la base de datos. Verifique la conexion a MySQL.");
        }

        cargarRegistrosEnRequest(request, "", "");
        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_PARQUEOS);
        dispatcher.forward(request, response);
    }

    private void cargarRegistrosEnRequest(HttpServletRequest request, String placaFiltro, String estadoFiltro) {
        try {
            List<RegistroParqueo> registros;
            if ((placaFiltro != null && !placaFiltro.isEmpty()) || (estadoFiltro != null && !estadoFiltro.isEmpty())) {
                registros = registroParqueoDAO.buscarPorFiltros(placaFiltro, estadoFiltro);
            } else {
                registros = registroParqueoDAO.listarTodos();
            }
            request.setAttribute("registros", registros);
            request.setAttribute("totalResultados", registros.size());
        } catch (SQLException ex) {
            request.setAttribute("registros", Collections.emptyList());
            request.setAttribute("totalResultados", 0);
            if (request.getAttribute("mensajeError") == null) {
                request.setAttribute("mensajeError",
                        "Error al consultar los registros de parqueo en MySQL.");
            }
        }
    }

    private void preservarDatosFormulario(HttpServletRequest request, String placa, String valor, String hora, String estado) {
        request.setAttribute("formPlaca", placa);
        request.setAttribute("formValorCalculado", valor);
        request.setAttribute("formHoraIngreso", hora);
        request.setAttribute("formEstadoPago", estado);
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

