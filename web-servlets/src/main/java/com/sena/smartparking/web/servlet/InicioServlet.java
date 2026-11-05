package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.ClienteDAO;
import com.sena.smartparking.web.dao.PagoDAO;
import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.dao.impl.ClienteDAOImpl;
import com.sena.smartparking.web.dao.impl.PagoDAOImpl;
import com.sena.smartparking.web.dao.impl.RegistroParqueoDAOImpl;
import com.sena.smartparking.web.model.Cliente;
import com.sena.smartparking.web.model.Pago;
import com.sena.smartparking.web.model.RegistroParqueo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Controlador Jakarta Servlet para el panel principal de Smart Parking Web (/inicio).
 * Consulta los indicadores reales en MySQL y despacha la vista /WEB-INF/views/inicio.jsp.
 */
@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio"})
public class InicioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VISTA_INICIO = "/WEB-INF/views/inicio.jsp";

    private final RegistroParqueoDAO registroParqueoDAO;
    private final ClienteDAO clienteDAO;
    private final PagoDAO pagoDAO;

    public InicioServlet() {
        this(new RegistroParqueoDAOImpl(), new ClienteDAOImpl(), new PagoDAOImpl());
    }

    public InicioServlet(RegistroParqueoDAO registroParqueoDAO, ClienteDAO clienteDAO, PagoDAO pagoDAO) {
        this.registroParqueoDAO = registroParqueoDAO;
        this.clienteDAO = clienteDAO;
        this.pagoDAO = pagoDAO;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            List<RegistroParqueo> registros = registroParqueoDAO.listarTodos();
            List<Cliente> clientes = clienteDAO.listarTodos();
            List<Pago> pagos = pagoDAO.listarTodos();

            long registrosPendientes = registros.stream()
                    .filter(r -> r.getEstadoPago() != null && "PENDIENTE".equalsIgnoreCase(r.getEstadoPago()))
                    .count();

            request.setAttribute("totalRegistros", registros.size());
            request.setAttribute("registrosPendientes", registrosPendientes);
            request.setAttribute("totalClientes", clientes.size());
            request.setAttribute("totalPagos", pagos.size());
            request.setAttribute("ultimosRegistros", registros.size() > 5 ? registros.subList(0, 5) : registros);
        } catch (SQLException ex) {
            request.setAttribute("totalRegistros", 0);
            request.setAttribute("registrosPendientes", 0);
            request.setAttribute("totalClientes", 0);
            request.setAttribute("totalPagos", 0);
            request.setAttribute("ultimosRegistros", Collections.emptyList());
            request.setAttribute("mensajeError",
                    "No fue posible consultar los indicadores en MySQL. Verifique el estado de la base de datos smart_parking.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_INICIO);
        dispatcher.forward(request, response);
    }
}

