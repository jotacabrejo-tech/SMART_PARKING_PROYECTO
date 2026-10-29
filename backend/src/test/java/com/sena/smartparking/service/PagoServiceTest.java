package com.sena.smartparking.service;

import com.sena.smartparking.dao.PagoDAO;
import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.dto.PagoRequest;
import com.sena.smartparking.dto.PagoResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.Pago;
import com.sena.smartparking.model.RegistroParqueo;
import com.sena.smartparking.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para PagoService con Mockito (aislando la base de datos).
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoDAO pagoDAO;

    @Mock
    private RegistroParqueoDAO registroParqueoDAO;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Pago pagoPrueba;
    private RegistroParqueo registroPrueba;

    @BeforeEach
    void setUp() {
        registroPrueba = new RegistroParqueo(
                10, "ABC123", LocalDateTime.now().minusHours(2), new BigDecimal("8000.00"), "PENDIENTE"
        );
        pagoPrueba = new Pago(
                1, 10, LocalDateTime.now(), new BigDecimal("8000.00"), "PAGADO", 15
        );
    }

    @Test
    @DisplayName("listarTodos debe retornar lista de pagos")
    void testListarTodos() throws DatabaseException {
        when(pagoDAO.listarTodos()).thenReturn(List.of(pagoPrueba));

        List<PagoResponse> lista = pagoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(new BigDecimal("8000.00"), lista.get(0).getValorPagado());
        verify(pagoDAO).listarTodos();
    }

    @Test
    @DisplayName("consultarPorId existente debe retornar DTO")
    void testConsultarPorId_Existente() throws DatabaseException {
        when(pagoDAO.consultarPorId(1)).thenReturn(Optional.of(pagoPrueba));

        PagoResponse response = pagoService.consultarPorId(1);

        assertNotNull(response);
        assertEquals(1, response.getIdPago());
        assertEquals(10, response.getIdRegistro());
    }

    @Test
    @DisplayName("consultarPorId inexistente debe lanzar ResourceNotFoundException")
    void testConsultarPorId_Inexistente() throws DatabaseException {
        when(pagoDAO.consultarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.consultarPorId(99));
    }

    @Test
    @DisplayName("consultarPorRegistro existente debe retornar lista de pagos")
    void testConsultarPorRegistro_Existente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(10)).thenReturn(Optional.of(registroPrueba));
        when(pagoDAO.consultarPorRegistro(10)).thenReturn(List.of(pagoPrueba));

        List<PagoResponse> lista = pagoService.consultarPorRegistro(10);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(10, lista.get(0).getIdRegistro());
    }

    @Test
    @DisplayName("consultarPorRegistro cuando el parqueo no existe debe lanzar ResourceNotFoundException")
    void testConsultarPorRegistro_ParqueoInexistente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.consultarPorRegistro(99));
    }

    @Test
    @DisplayName("registrarPago exitoso cuando el registro de parqueo existe")
    void testRegistrarPago_Exitoso() throws DatabaseException {
        PagoRequest request = new PagoRequest(10, LocalDateTime.now(), new BigDecimal("8000.00"), "pagado", 15);

        when(registroParqueoDAO.consultarPorId(10)).thenReturn(Optional.of(registroPrueba));
        when(pagoDAO.insertar(any(Pago.class))).thenReturn(pagoPrueba);

        PagoResponse response = pagoService.registrarPago(request);

        assertNotNull(response);
        assertEquals(1, response.getIdPago());
        assertEquals(10, response.getIdRegistro());
        assertEquals("PAGADO", response.getEstadoPago());
        verify(pagoDAO).insertar(any(Pago.class));
    }

    @Test
    @DisplayName("registrarPago cuando el registro de parqueo NO existe debe lanzar ResourceNotFoundException")
    void testRegistrarPago_RegistroInexistente() throws DatabaseException {
        PagoRequest request = new PagoRequest(999, LocalDateTime.now(), new BigDecimal("8000.00"), "PAGADO", 15);

        when(registroParqueoDAO.consultarPorId(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.registrarPago(request));
    }
}
