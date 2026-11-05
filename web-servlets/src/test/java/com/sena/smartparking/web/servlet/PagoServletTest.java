package com.sena.smartparking.web.servlet;

import com.sena.smartparking.web.dao.PagoDAO;
import com.sena.smartparking.web.dao.RegistroParqueoDAO;
import com.sena.smartparking.web.model.Pago;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias JUnit 5 + Mockito para {@link PagoServlet}.
 */
@ExtendWith(MockitoExtension.class)
class PagoServletTest {

    @Mock
    private PagoDAO pagoDAO;

    @Mock
    private RegistroParqueoDAO registroParqueoDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private PagoServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new PagoServlet(pagoDAO, registroParqueoDAO);
        when(request.getRequestDispatcher(PagoServlet.VISTA_PAGOS)).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet sin filtro debe listar todos los pagos y cargar registros disponibles")
    void doGetSinFiltroDebeListarPagosYCargarRegistros() throws Exception {
        List<Pago> pagos = List.of(
                new Pago(1, 5, LocalDateTime.now(), new BigDecimal("5000.00"), "APROBADO", 15)
        );
        List<RegistroParqueo> registros = List.of(
                new RegistroParqueo(5, "ABC123", LocalDateTime.now(), new BigDecimal("5000.00"), "PENDIENTE")
        );
        when(request.getParameter("idRegistro")).thenReturn(null);
        when(pagoDAO.listarTodos()).thenReturn(pagos);
        when(registroParqueoDAO.listarTodos()).thenReturn(registros);

        servlet.doGet(request, response);

        verify(pagoDAO).listarTodos();
        verify(registroParqueoDAO).listarTodos();
        verify(request).setAttribute("pagos", pagos);
        verify(request).setAttribute("registrosDisponibles", registros);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet con idRegistro valido debe filtrar pagos por id_registro")
    void doGetConIdRegistroDebeFiltrar() throws Exception {
        List<Pago> pagosFiltrados = List.of(
                new Pago(2, 7, LocalDateTime.now(), new BigDecimal("6000.00"), "APROBADO", 15)
        );
        when(request.getParameter("idRegistro")).thenReturn("7");
        when(pagoDAO.listarPorIdRegistro(7)).thenReturn(pagosFiltrados);
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doGet(request, response);

        verify(pagoDAO).listarPorIdRegistro(7);
        verify(request).setAttribute("pagos", pagosFiltrados);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con registro existente y datos validos debe registrar el pago")
    void doPostValidoConRegistroExistenteDebeInsertarPago() throws Exception {
        RegistroParqueo regExistente = new RegistroParqueo(5, "ABC123", LocalDateTime.now(), new BigDecimal("5000.00"), "PENDIENTE");

        when(request.getParameter("idRegistro")).thenReturn("5");
        when(request.getParameter("valorPagado")).thenReturn("5000.00");
        when(request.getParameter("fechaPago")).thenReturn("2026-11-05T11:00");
        when(request.getParameter("estadoPago")).thenReturn("APROBADO");
        when(request.getParameter("tiempoLimiteSalida")).thenReturn("15");
        when(registroParqueoDAO.buscarPorId(5)).thenReturn(Optional.of(regExistente));
        when(pagoDAO.insertar(any(Pago.class))).thenReturn(21);
        when(pagoDAO.listarTodos()).thenReturn(List.of());
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of(regExistente));

        servlet.doPost(request, response);

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoDAO).insertar(captor.capture());
        assertEquals(5, captor.getValue().getIdRegistro());
        assertEquals(new BigDecimal("5000.00"), captor.getValue().getValorPagado());
        assertEquals("APROBADO", captor.getValue().getEstadoPago());
        assertEquals(15, captor.getValue().getTiempoLimiteSalida());
        verify(request).setAttribute(eq("mensajeExito"), contains("#21"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost con idRegistro inexistente debe rechazar para proteger la clave foranea fk_pago_registro")
    void doPostConRegistroInexistenteDebeRechazar() throws Exception {
        when(request.getParameter("idRegistro")).thenReturn("9999");
        when(request.getParameter("valorPagado")).thenReturn("5000.00");
        when(request.getParameter("fechaPago")).thenReturn("");
        when(request.getParameter("estadoPago")).thenReturn("APROBADO");
        when(request.getParameter("tiempoLimiteSalida")).thenReturn("15");
        when(registroParqueoDAO.buscarPorId(9999)).thenReturn(Optional.empty());
        when(pagoDAO.listarTodos()).thenReturn(List.of());
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(pagoDAO, never()).insertar(any());
        verify(request).setAttribute(eq("mensajeError"), contains("No existe el registro de parqueo con ID #9999"));
        verify(dispatcher).forward(request, response);
    }
}

