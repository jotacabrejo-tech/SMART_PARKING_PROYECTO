package com.sena.smartparking.service;

import com.sena.smartparking.dao.RegistroParqueoDAO;
import com.sena.smartparking.dto.ConteoResponse;
import com.sena.smartparking.dto.RegistroParqueoRequest;
import com.sena.smartparking.dto.RegistroParqueoResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.RegistroParqueo;
import com.sena.smartparking.service.impl.RegistroParqueoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la capa de servicio RegistroParqueoService.
 * Emplea Mockito para aislar completamente la base de datos real.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@ExtendWith(MockitoExtension.class)
class RegistroParqueoServiceTest {

    @Mock
    private RegistroParqueoDAO registroParqueoDAO;

    @InjectMocks
    private RegistroParqueoServiceImpl registroParqueoService;

    private RegistroParqueo registroPrueba;

    @BeforeEach
    void setUp() {
        registroPrueba = new RegistroParqueo(
                1,
                "XYZ789",
                LocalDateTime.now().minusHours(1),
                new BigDecimal("5000.00"),
                "PENDIENTE"
        );
    }

    @Test
    @DisplayName("listarTodos sin filtro debe invocar dao.listarTodos() y retornar DTOs")
    void testListarTodos_SinFiltro() throws DatabaseException {
        when(registroParqueoDAO.listarTodos()).thenReturn(List.of(registroPrueba));

        List<RegistroParqueoResponse> lista = registroParqueoService.listarTodos(null);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("XYZ789", lista.get(0).getPlaca());
        verify(registroParqueoDAO).listarTodos();
    }

    @Test
    @DisplayName("listarTodos con placa debe normalizar y llamar a consultarPorPlaca()")
    void testListarTodos_ConFiltroPlaca() throws DatabaseException {
        when(registroParqueoDAO.consultarPorPlaca("XYZ789")).thenReturn(List.of(registroPrueba));

        List<RegistroParqueoResponse> lista = registroParqueoService.listarTodos(" xyz789 ");

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("XYZ789", lista.get(0).getPlaca());
        verify(registroParqueoDAO).consultarPorPlaca("XYZ789");
    }

    @Test
    @DisplayName("consultarPorId existente debe retornar el DTO correspondiente")
    void testConsultarPorId_Existente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(1)).thenReturn(Optional.of(registroPrueba));

        RegistroParqueoResponse response = registroParqueoService.consultarPorId(1);

        assertNotNull(response);
        assertEquals(1, response.getIdRegistro());
        assertEquals("XYZ789", response.getPlaca());
    }

    @Test
    @DisplayName("consultarPorId inexistente debe lanzar ResourceNotFoundException")
    void testConsultarPorId_Inexistente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> registroParqueoService.consultarPorId(999));
    }

    @Test
    @DisplayName("contarRegistros debe retornar ConteoResponse con el total del DAO")
    void testContarRegistros() throws DatabaseException {
        when(registroParqueoDAO.contar()).thenReturn(15L);

        ConteoResponse conteo = registroParqueoService.contarRegistros();

        assertNotNull(conteo);
        assertEquals(15L, conteo.getTotal());
        assertNotNull(conteo.getTimestamp());
    }

    @Test
    @DisplayName("registrarIngreso debe normalizar datos y persistir mediante DAO")
    void testRegistrarIngreso() throws DatabaseException {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "abc123",
                LocalDateTime.now(),
                new BigDecimal("3000.00"),
                "pendiente"
        );

        RegistroParqueo guardado = new RegistroParqueo(
                10,
                "ABC123",
                request.getHoraIngreso(),
                new BigDecimal("3000.00"),
                "PENDIENTE"
        );

        when(registroParqueoDAO.insertar(any(RegistroParqueo.class))).thenReturn(guardado);

        RegistroParqueoResponse response = registroParqueoService.registrarIngreso(request);

        assertNotNull(response);
        assertEquals(10, response.getIdRegistro());
        assertEquals("ABC123", response.getPlaca());
        assertEquals("PENDIENTE", response.getEstadoPago());
        verify(registroParqueoDAO).insertar(any(RegistroParqueo.class));
    }

    @Test
    @DisplayName("actualizar registro existente debe modificar y retornar DTO")
    void testActualizar_Existente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(1)).thenReturn(Optional.of(registroPrueba));
        when(registroParqueoDAO.actualizar(any(RegistroParqueo.class))).thenReturn(true);

        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "XYZ789",
                registroPrueba.getHoraIngreso(),
                new BigDecimal("8000.00"),
                "PAGADO"
        );

        RegistroParqueoResponse response = registroParqueoService.actualizar(1, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("8000.00"), response.getValorCalculado());
        assertEquals("PAGADO", response.getEstadoPago());
        verify(registroParqueoDAO).actualizar(any(RegistroParqueo.class));
    }

    @Test
    @DisplayName("actualizar registro inexistente debe lanzar ResourceNotFoundException")
    void testActualizar_Inexistente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(99)).thenReturn(Optional.empty());

        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "XYZ789",
                LocalDateTime.now(),
                new BigDecimal("8000.00"),
                "PAGADO"
        );

        assertThrows(ResourceNotFoundException.class, () -> registroParqueoService.actualizar(99, request));
    }

    @Test
    @DisplayName("eliminar registro existente debe invocar dao.eliminar()")
    void testEliminar_Existente() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(1)).thenReturn(Optional.of(registroPrueba));
        when(registroParqueoDAO.eliminar(1)).thenReturn(true);

        registroParqueoService.eliminar(1);

        verify(registroParqueoDAO).eliminar(1);
    }

    @Test
    @DisplayName("eliminar registro con pagos asociados debe lanzar IntegrityConflictException")
    void testEliminar_ConflictoIntegridad() throws DatabaseException {
        when(registroParqueoDAO.consultarPorId(1)).thenReturn(Optional.of(registroPrueba));
        DatabaseException dbEx = new DatabaseException(
                "Restricción de Integridad Referencial: No es posible eliminar el registro de parqueo ID 1",
                new SQLIntegrityConstraintViolationException("FK Violation")
        );
        doThrow(dbEx).when(registroParqueoDAO).eliminar(1);

        assertThrows(IntegrityConflictException.class, () -> registroParqueoService.eliminar(1));
    }
}
