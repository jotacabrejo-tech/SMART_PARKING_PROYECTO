package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.ClienteDAO;
import com.sena.smartparking.web.dao.impl.ClienteDAOImpl;
import com.sena.smartparking.web.model.Cliente;

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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Controlador Jakarta Servlet para el modulo de Clientes (/clientes).
 * Implementa manejo de parametros por GET (listado y busqueda por documento) y POST (registro con validacion de duplicados).
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/clientes"})
public class ClienteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String VISTA_CLIENTES = "/WEB-INF/views/clientes.jsp";

    private static final Set<String> TIPOS_DOC_VALIDOS = Set.of("CC", "CE", "NIT", "TI", "PASAPORTE");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClienteDAO clienteDAO;

    public ClienteServlet() {
        this(new ClienteDAOImpl());
    }

    public ClienteServlet(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String tipoDocFiltro = limpiarParametro(request.getParameter("tipoDoc"));
        String numDocFiltro = limpiarParametro(request.getParameter("numDoc"));

        request.setAttribute("tipoDocFiltro", tipoDocFiltro);
        request.setAttribute("numDocFiltro", numDocFiltro);

        cargarClientesEnRequest(request, tipoDocFiltro, numDocFiltro);

        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_CLIENTES);
        dispatcher.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String tipoDoc = limpiarParametro(request.getParameter("tipoDoc")).toUpperCase();
        String numDoc = limpiarParametro(request.getParameter("numDoc"));
        String nombres = limpiarParametro(request.getParameter("nombres"));
        String correo = limpiarParametro(request.getParameter("correo"));

        // Validar campos obligatorios
        if (tipoDoc.isEmpty() || numDoc.isEmpty() || nombres.isEmpty() || correo.isEmpty()) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "Todos los campos del formulario de cliente (Tipo Doc, Numero Doc, Nombres y Correo) son obligatorios.");
            cargarClientesEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
            return;
        }

        // Validar tipo de documento
        if (!TIPOS_DOC_VALIDOS.contains(tipoDoc)) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "El tipo de documento no es valido. Seleccione CC, CE, NIT, TI o PASAPORTE.");
            cargarClientesEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
            return;
        }

        // Validar longitudes de columnas SQL (num_doc VARCHAR(20), nombres VARCHAR(100), correo VARCHAR(100))
        if (numDoc.length() < 3 || numDoc.length() > 20) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "El numero de documento debe tener entre 3 y 20 caracteres.");
            cargarClientesEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
            return;
        }

        if (nombres.length() < 3 || nombres.length() > 100) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "El nombre del cliente debe tener entre 3 y 100 caracteres.");
            cargarClientesEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
            return;
        }

        // Validar correo electronico
        if (correo.length() > 100 || !PATRON_CORREO.matcher(correo).matches()) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "Debe ingresar una direccion de correo electronico valida (ej. usuario@dominio.com).");
            cargarClientesEnRequest(request, "", "");
            request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
            return;
        }

        try {
            // Comprobar duplicados por tipo_doc y num_doc
            Optional<Cliente> existente = clienteDAO.buscarPorDocumento(tipoDoc, numDoc);
            if (existente.isPresent()) {
                preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
                request.setAttribute("mensajeError",
                        "Ya existe un cliente registrado con el documento " + tipoDoc + " " + numDoc + ".");
                cargarClientesEnRequest(request, "", "");
                request.getRequestDispatcher(VISTA_CLIENTES).forward(request, response);
                return;
            }

            Cliente nuevoCliente = new Cliente(tipoDoc, numDoc, nombres, correo);
            int idGenerado = clienteDAO.insertar(nuevoCliente);
            request.setAttribute("mensajeExito",
                    "Cliente registrado exitosamente con ID #" + idGenerado + " (" + nombres + ").");
        } catch (SQLException ex) {
            preservarDatosFormulario(request, tipoDoc, numDoc, nombres, correo);
            request.setAttribute("mensajeError",
                    "No se pudo registrar el cliente en la base de datos. Verifique la conexion a MySQL.");
        }

        cargarClientesEnRequest(request, "", "");
        RequestDispatcher dispatcher = request.getRequestDispatcher(VISTA_CLIENTES);
        dispatcher.forward(request, response);
    }

    private void cargarClientesEnRequest(HttpServletRequest request, String tipoDocFiltro, String numDocFiltro) {
        try {
            List<Cliente> clientes;
            if ((tipoDocFiltro != null && !tipoDocFiltro.isEmpty()) || (numDocFiltro != null && !numDocFiltro.isEmpty())) {
                clientes = clienteDAO.buscarPorFiltros(tipoDocFiltro, numDocFiltro);
            } else {
                clientes = clienteDAO.listarTodos();
            }
            request.setAttribute("clientes", clientes);
            request.setAttribute("totalResultados", clientes.size());
        } catch (SQLException ex) {
            request.setAttribute("clientes", Collections.emptyList());
            request.setAttribute("totalResultados", 0);
            if (request.getAttribute("mensajeError") == null) {
                request.setAttribute("mensajeError",
                        "Error al consultar el listado de clientes en MySQL.");
            }
        }
    }

    private void preservarDatosFormulario(HttpServletRequest request, String tipoDoc, String numDoc, String nombres, String correo) {
        request.setAttribute("formTipoDoc", tipoDoc);
        request.setAttribute("formNumDoc", numDoc);
        request.setAttribute("formNombres", nombres);
        request.setAttribute("formCorreo", correo);
    }

    private String limpiarParametro(String param) {
        return param != null ? param.trim() : "";
    }
}

