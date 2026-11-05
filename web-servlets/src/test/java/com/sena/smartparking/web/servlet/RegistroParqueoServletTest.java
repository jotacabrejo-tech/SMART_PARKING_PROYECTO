package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.model.RegistroParqueo;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias JUnit 5 + Mockito para {@link RegistroParqueoServlet}.
 */
@ExtendWith(MockitoExtension.class)
class RegistroParqueoServletTest {

    @Mock
    private RegistroParqueoDAO registroParqueoDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private RegistroParqueoServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new RegistroParqueoServlet(registroParqueoDAO);
        when(request.getRequestDispatcher(RegistroParqueoServlet.VISTA_PARQUEOS)).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet sin filtros debe listar todos los registros y despachar a parqueos.jsp")
    void doGetSinFiltrosDebeListarTodos() throws Exception {
        List<RegistroParqueo> simulados = List.of(
                new RegistroParqueo(1, "ABC123", LocalDateTime.now(), new BigDecimal("5000.00"), "PENDIENTE")
        );
        when(request.getParameter("placa")).thenReturn(null);
        when(request.getParameter("estadoPago")).thenReturn(null);
        when(registroParqueoDAO.listarTodos()).thenReturn(simulados);

        servlet.doGet(request, response);

        verify(registroParqueoDAO).listarTodos();
        verify(request).setAttribute("registros", simulados);
        verify(request).setAttribute("totalResultados", 1);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet con parametros de placa y estado debe invocar buscarPorFiltros")
    void doGetConFiltrosDebeInvocarBuscarPorFiltros() throws Exception {
        List<RegistroParqueo> filtrados = List.of(
                new RegistroParqueo(2, "XYZ789", LocalDateTime.now(), new BigDecimal("8000.00"), "PAGADO")
        );
        when(request.getParameter("placa")).thenReturn("xyz");
        when(request.getParameter("estadoPago")).thenReturn("PAGADO");
        when(registroParqueoDAO.buscarPorFiltros("xyz", "PAGADO")).thenReturn(filtrados);

        servlet.doGet(request, response);

        verify(registroParqueoDAO).buscarPorFiltros("xyz", "PAGADO");
        verify(request).setAttribute("registros", filtrados);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con datos validos debe insertar en el DAO y enviar mensajeExito")
    void doPostValidoDebeInsertarRegistro() throws Exception {
        when(request.getParameter("placa")).thenReturn("spk202");
        when(request.getParameter("valorCalculado")).thenReturn("6500.00");
        when(request.getParameter("horaIngreso")).thenReturn("2026-11-05T10:30");
        when(request.getParameter("estadoPago")).thenReturn("PENDIENTE");
        when(registroParqueoDAO.insertar(any(RegistroParqueo.class))).thenReturn(15);
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        ArgumentCaptor<RegistroParqueo> captor = ArgumentCaptor.forClass(RegistroParqueo.class);
        verify(registroParqueoDAO).insertar(captor.capture());
        RegistroParqueo insertado = captor.getValue();

        assertEquals("SPK202", insertado.getPlaca());
        assertEquals(new BigDecimal("6500.00"), insertado.getValorCalculado());
        assertEquals("PENDIENTE", insertado.getEstadoPago());
        verify(request).setAttribute(eq("mensajeExito"), contains("#15"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con placa invalida no debe invocar insertar y debe enviar mensajeError")
    void doPostPlacaInvalidaDebeRechazar() throws Exception {
        when(request.getParameter("placa")).thenReturn("A!");
        when(request.getParameter("valorCalculado")).thenReturn("5000.00");
        when(request.getParameter("horaIngreso")).thenReturn("");
        when(request.getParameter("estadoPago")).thenReturn("PENDIENTE");
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(registroParqueoDAO, never()).insertar(any());
        verify(request).setAttribute(eq("mensajeError"), contains("placa"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con valor calculado negativo debe rechazar e informar error")
    void doPostValorNegativoDebeRechazar() throws Exception {
        when(request.getParameter("placa")).thenReturn("ABC123");
        when(request.getParameter("valorCalculado")).thenReturn("-100");
        when(request.getParameter("horaIngreso")).thenReturn("");
        when(request.getParameter("estadoPago")).thenReturn("PENDIENTE");
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(registroParqueoDAO, never()).insertar(any());
        verify(request).setAttribute(eq("mensajeError"), contains("valor calculado"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost debe manejar SQLException del DAO de forma controlada sin lanzar excepcion")
    void doPostDebeManejarExcepcionSQL() throws Exception {
        when(request.getParameter("placa")).thenReturn("ABC123");
        when(request.getParameter("valorCalculado")).thenReturn("4000.00");
        when(request.getParameter("horaIngreso")).thenReturn("");
        when(request.getParameter("estadoPago")).thenReturn("PENDIENTE");
        when(registroParqueoDAO.insertar(any())).thenThrow(new SQLException("Fallo simulado"));
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("mensajeError"), contains("No se pudo guardar"));
        verify(dispatcher).forward(request, response);
    }
}

