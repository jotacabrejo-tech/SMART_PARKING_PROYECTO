package com.sena.smartparking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.smartparking.dto.ClienteRequest;
import com.sena.smartparking.dto.ClienteResponse;
import com.sena.smartparking.exception.GlobalExceptionHandler;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de MockMvc y validaciones para ClienteController.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("GET /api/v1/clientes debe retornar 200 OK y lista de clientes")
    void testListarTodos() throws Exception {
        ClienteResponse cliente = new ClienteResponse(1, "CC", "1020304050", "Carlos Mendoza", "carlos@email.com");
        when(clienteService.listarTodos()).thenReturn(List.of(cliente));

        mockMvc.perform(get("/api/v1/clientes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numDoc").value("1020304050"))
                .andExpect(jsonPath("$[0].nombres").value("Carlos Mendoza"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} existente debe retornar 200 OK")
    void testConsultarPorId_Existente() throws Exception {
        ClienteResponse cliente = new ClienteResponse(1, "CC", "1020304050", "Carlos Mendoza", "carlos@email.com");
        when(clienteService.consultarPorId(1)).thenReturn(cliente);

        mockMvc.perform(get("/api/v1/clientes/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.correo").value("carlos@email.com"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} inexistente debe retornar 404 Not Found")
    void testConsultarPorId_Inexistente() throws Exception {
        when(clienteService.consultarPorId(99))
                .thenThrow(new ResourceNotFoundException("Cliente con ID 99 no encontrado."));

        mockMvc.perform(get("/api/v1/clientes/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    @DisplayName("POST /api/v1/clientes válido debe retornar 201 Created y Location")
    void testRegistrarCliente_Valido() throws Exception {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "carlos@email.com");
        ClienteResponse response = new ClienteResponse(1, "CC", "1020304050", "Carlos Mendoza", "carlos@email.com");

        when(clienteService.registrarCliente(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/clientes/1")))
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.numDoc").value("1020304050"));
    }

    @Test
    @DisplayName("POST /api/v1/clientes con correo inválido debe retornar 400 Bad Request")
    void testRegistrarCliente_CorreoInvalido_Retorna400() throws Exception {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "correo-no-valido");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.correo").exists());
    }

    @Test
    @DisplayName("POST /api/v1/clientes con tipo_doc no permitido debe retornar 400 Bad Request")
    void testRegistrarCliente_TipoDocInvalido_Retorna400() throws Exception {
        ClienteRequest request = new ClienteRequest("TIPO_INVALIDO", "1020304050", "Carlos Mendoza", "carlos@email.com");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.tipoDoc").exists());
    }

    @Test
    @DisplayName("POST /api/v1/clientes con documento duplicado debe retornar 409 Conflict")
    void testRegistrarCliente_DocumentoDuplicado_Retorna409() throws Exception {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "carlos@email.com");

        when(clienteService.registrarCliente(any(ClienteRequest.class)))
                .thenThrow(new IntegrityConflictException("Conflicto: Ya existe un cliente registrado con el documento CC 1020304050."));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("Ya existe un cliente")));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} válido debe retornar 200 OK")
    void testActualizar_Valido() throws Exception {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos A. Mendoza", "carlos.nuevo@email.com");
        ClienteResponse response = new ClienteResponse(1, "CC", "1020304050", "Carlos A. Mendoza", "carlos.nuevo@email.com");

        when(clienteService.actualizar(eq(1), any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.nombres").value("Carlos A. Mendoza"));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} inexistente debe retornar 404 Not Found")
    void testActualizar_Inexistente() throws Exception {
        ClienteRequest request = new ClienteRequest("CC", "1020304050", "Carlos Mendoza", "carlos@email.com");

        when(clienteService.actualizar(eq(99), any(ClienteRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cliente con ID 99 no existe."));

        mockMvc.perform(put("/api/v1/clientes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
