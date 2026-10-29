package com.sena.smartparking.service;

import com.sena.smartparking.dao.ClienteDAO;
import com.sena.smartparking.dto.ClienteRequest;
import com.sena.smartparking.dto.ClienteResponse;
import com.sena.smartparking.exception.DatabaseException;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.model.Cliente;
import com.sena.smartparking.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ClienteService con Mockito (aislando la base de datos).
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteDAO clienteDAO;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        clientePrueba = new Cliente(
                1,
                "CC",
                "1020304050",
                "Carlos Alberto Mendoza",
                "carlos.mendoza@email.com"
        );
    }

    @Test
    @DisplayName("listarTodos debe retornar lista de DTOs")
    void testListarTodos() throws DatabaseException {
        when(clienteDAO.listarTodos()).thenReturn(List.of(clientePrueba));

        List<ClienteResponse> lista = clienteService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("1020304050", lista.get(0).getNumDoc());
        verify(clienteDAO).listarTodos();
    }

    @Test
    @DisplayName("consultarPorId existente debe retornar el DTO")
    void testConsultarPorId_Existente() throws DatabaseException {
        when(clienteDAO.consultarPorId(1)).thenReturn(Optional.of(clientePrueba));

        ClienteResponse response = clienteService.consultarPorId(1);

        assertNotNull(response);
        assertEquals(1, response.getIdCliente());
        assertEquals("Carlos Alberto Mendoza", response.getNombres());
    }

    @Test
    @DisplayName("consultarPorId inexistente debe lanzar ResourceNotFoundException")
    void testConsultarPorId_Inexistente() throws DatabaseException {
        when(clienteDAO.consultarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.consultarPorId(99));
    }

    @Test
    @DisplayName("registrarCliente exitoso cuando no existe documento duplicado")
    void testRegistrarCliente_Exitoso() throws DatabaseException {
        ClienteRequest request = new ClienteRequest("cc", "987654321", "Ana Maria Rios", "ana.rios@email.com");
        Cliente guardado = new Cliente(2, "CC", "987654321", "Ana Maria Rios", "ana.rios@email.com");

        when(clienteDAO.consultarPorDocumento("CC", "987654321")).thenReturn(Optional.empty());
        when(clienteDAO.insertar(any(Cliente.class))).thenReturn(guardado);

        ClienteResponse response = clienteService.registrarCliente(request);

        assertNotNull(response);
        assertEquals(2, response.getIdCliente());
        assertEquals("CC", response.getTipoDoc());
        assertEquals("987654321", response.getNumDoc());
    }

    @Test
    @DisplayName("registrarCliente con documento duplicado debe lanzar IntegrityConflictException")
    void testRegistrarCliente_Duplicado() throws DatabaseException {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "carlos@email.com");

        when(clienteDAO.consultarPorDocumento("CC", "1020304050")).thenReturn(Optional.of(clientePrueba));

        assertThrows(IntegrityConflictException.class, () -> clienteService.registrarCliente(request));
    }

    @Test
    @DisplayName("actualizar cliente existente exitoso")
    void testActualizar_Exitoso() throws DatabaseException {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos A. Mendoza Gomez", "carlos.nuevo@email.com");

        when(clienteDAO.consultarPorId(1)).thenReturn(Optional.of(clientePrueba));
        when(clienteDAO.consultarPorDocumento("CC", "1020304050")).thenReturn(Optional.of(clientePrueba));
        when(clienteDAO.actualizar(any(Cliente.class))).thenReturn(true);

        ClienteResponse response = clienteService.actualizar(1, request);

        assertNotNull(response);
        assertEquals("Carlos A. Mendoza Gomez", response.getNombres());
        assertEquals("carlos.nuevo@email.com", response.getCorreo());
    }

    @Test
    @DisplayName("actualizar cliente inexistente debe lanzar ResourceNotFoundException")
    void testActualizar_Inexistente() throws DatabaseException {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "carlos@email.com");

        when(clienteDAO.consultarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.actualizar(99, request));
    }

    @Test
    @DisplayName("actualizar con documento de otro cliente debe lanzar IntegrityConflictException")
    void testActualizar_DocumentoColisionaConOtro() throws DatabaseException {
        ClienteRequest request = new ClienteRequest("CC", "55555555", "Carlos Mendoza", "carlos@email.com");
        Cliente otroCliente = new Cliente(2, "CC", "55555555", "Otro Cliente", "otro@email.com");

        when(clienteDAO.consultarPorId(1)).thenReturn(Optional.of(clientePrueba));
        when(clienteDAO.consultarPorDocumento("CC", "55555555")).thenReturn(Optional.of(otroCliente));

        assertThrows(IntegrityConflictException.class, () -> clienteService.actualizar(1, request));
    }
}
