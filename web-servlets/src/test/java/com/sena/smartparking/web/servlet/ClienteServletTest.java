package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.ClienteDAO;
import com.sena.smartparking.web.model.Cliente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias JUnit 5 + Mockito para {@link ClienteServlet}.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServletTest {

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private ClienteServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ClienteServlet(clienteDAO);
        when(request.getRequestDispatcher(ClienteServlet.VISTA_CLIENTES)).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet sin parametros debe listar todos los clientes y despachar a clientes.jsp")
    void doGetSinParametrosDebeListarTodos() throws Exception {
        List<Cliente> clientes = List.of(
                new Cliente(1, "CC", "10203040", "Juan Perez", "juan@sena.edu.co")
        );
        when(request.getParameter("tipoDoc")).thenReturn(null);
        when(request.getParameter("numDoc")).thenReturn(null);
        when(clienteDAO.listarTodos()).thenReturn(clientes);

        servlet.doGet(request, response);

        verify(clienteDAO).listarTodos();
        verify(request).setAttribute("clientes", clientes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet con tipoDoc y numDoc debe invocar buscarPorFiltros")
    void doGetConFiltrosDebeBuscarPorDocumento() throws Exception {
        List<Cliente> filtrados = List.of(
                new Cliente(2, "NIT", "900123456", "Empresa SENA SAS", "contacto@empresa.co")
        );
        when(request.getParameter("tipoDoc")).thenReturn("NIT");
        when(request.getParameter("numDoc")).thenReturn("900123456");
        when(clienteDAO.buscarPorFiltros("NIT", "900123456")).thenReturn(filtrados);

        servlet.doGet(request, response);

        verify(clienteDAO).buscarPorFiltros("NIT", "900123456");
        verify(request).setAttribute("clientes", filtrados);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con datos validos y sin duplicado debe registrar el cliente")
    void doPostValidoDebeRegistrarCliente() throws Exception {
        when(request.getParameter("tipoDoc")).thenReturn("CC");
        when(request.getParameter("numDoc")).thenReturn("1098765432");
        when(request.getParameter("nombres")).thenReturn("Ana Maria Lopez");
        when(request.getParameter("correo")).thenReturn("ana.lopez@sena.edu.co");
        when(clienteDAO.buscarPorDocumento("CC", "1098765432")).thenReturn(Optional.empty());
        when(clienteDAO.insertar(any(Cliente.class))).thenReturn(10);
        when(clienteDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteDAO).insertar(captor.capture());
        assertEquals("CC", captor.getValue().getTipoDoc());
        assertEquals("1098765432", captor.getValue().getNumDoc());
        assertEquals("Ana Maria Lopez", captor.getValue().getNombres());
        assertEquals("ana.lopez@sena.edu.co", captor.getValue().getCorreo());
        verify(request).setAttribute(eq("mensajeExito"), contains("#10"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con documento duplicado debe rechazar e informar error de duplicidad")
    void doPostDuplicadoDebeRechazar() throws Exception {
        when(request.getParameter("tipoDoc")).thenReturn("CC");
        when(request.getParameter("numDoc")).thenReturn("10203040");
        when(request.getParameter("nombres")).thenReturn("Usuario Duplicado");
        when(request.getParameter("correo")).thenReturn("duplicado@correo.com");
        when(clienteDAO.buscarPorDocumento("CC", "10203040"))
                .thenReturn(Optional.of(new Cliente(1, "CC", "10203040", "Existente", "ex@correo.com")));
        when(clienteDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(clienteDAO, never()).insertar(any());
        verify(request).setAttribute(eq("mensajeError"), contains("Ya existe un cliente"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con correo electronico invalido debe rechazar antes de consultar DAO")
    void doPostCorreoInvalidoDebeRechazar() throws Exception {
        when(request.getParameter("tipoDoc")).thenReturn("CC");
        when(request.getParameter("numDoc")).thenReturn("10203040");
        when(request.getParameter("nombres")).thenReturn("Carlos Ruiz");
        when(request.getParameter("correo")).thenReturn("correo-sin-arroba");
        when(clienteDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(clienteDAO, never()).insertar(any());
        verify(request).setAttribute(eq("mensajeError"), contains("correo electronico"));
        verify(dispatcher).forward(request, response);
    }
}

