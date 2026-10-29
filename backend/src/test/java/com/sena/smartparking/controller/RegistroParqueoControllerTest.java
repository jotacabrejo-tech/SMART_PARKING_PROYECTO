package com.sena.smartparking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.smartparking.dto.ConteoResponse;
import com.sena.smartparking.dto.RegistroParqueoRequest;
import com.sena.smartparking.dto.RegistroParqueoResponse;
import com.sena.smartparking.exception.GlobalExceptionHandler;
import com.sena.smartparking.exception.IntegrityConflictException;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.service.RegistroParqueoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de controladores y validaciones HTTP mediante MockMvc.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@WebMvcTest(RegistroParqueoController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc
class RegistroParqueoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistroParqueoService registroParqueoService;

    @Test
    @DisplayName("GET /api/v1/registros-parqueo debe retornar 200 OK y lista de registros")
    void testListarTodos() throws Exception {
        RegistroParqueoResponse item = new RegistroParqueoResponse(
                1, "ABC123", LocalDateTime.now(), new BigDecimal("5000.00"), "PENDIENTE"
        );
        when(registroParqueoService.listarTodos(null)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/registros-parqueo")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].placa").value("ABC123"))
                .andExpect(jsonPath("$[0].estadoPago").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /api/v1/registros-parqueo/conteo debe retornar 200 OK con el total")
    void testContar() throws Exception {
        when(registroParqueoService.contarRegistros()).thenReturn(new ConteoResponse(25L));

        mockMvc.perform(get("/api/v1/registros-parqueo/conteo")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(25))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/v1/registros-parqueo/{id} existente debe retornar 200 OK")
    void testConsultarPorId_Existente() throws Exception {
        RegistroParqueoResponse item = new RegistroParqueoResponse(
                5, "JKL456", LocalDateTime.now(), new BigDecimal("6000.00"), "PAGADO"
        );
        when(registroParqueoService.consultarPorId(5)).thenReturn(item);

        mockMvc.perform(get("/api/v1/registros-parqueo/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRegistro").value(5))
                .andExpect(jsonPath("$.placa").value("JKL456"));
    }

    @Test
    @DisplayName("GET /api/v1/registros-parqueo/{id} inexistente debe retornar 404 Not Found")
    void testConsultarPorId_Inexistente() throws Exception {
        when(registroParqueoService.consultarPorId(99))
                .thenThrow(new ResourceNotFoundException("Registro de parqueo con ID 99 no encontrado."));

        mockMvc.perform(get("/api/v1/registros-parqueo/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    @DisplayName("POST /api/v1/registros-parqueo con datos válidos debe retornar 201 Created y Location")
    void testRegistrarIngreso_Valido() throws Exception {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "MNO789", LocalDateTime.now(), new BigDecimal("4500.00"), "PENDIENTE"
        );

        RegistroParqueoResponse response = new RegistroParqueoResponse(
                12, "MNO789", request.getHoraIngreso(), new BigDecimal("4500.00"), "PENDIENTE"
        );

        when(registroParqueoService.registrarIngreso(any(RegistroParqueoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/registros-parqueo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/registros-parqueo/12")))
                .andExpect(jsonPath("$.idRegistro").value(12))
                .andExpect(jsonPath("$.placa").value("MNO789"));
    }

    @Test
    @DisplayName("POST /api/v1/registros-parqueo con placa vacía debe retornar 400 Bad Request")
    void testRegistrarIngreso_PlacaVacia_Retorna400() throws Exception {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "", LocalDateTime.now(), new BigDecimal("4500.00"), "PENDIENTE"
        );

        mockMvc.perform(post("/api/v1/registros-parqueo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.placa").exists());
    }

    @Test
    @DisplayName("POST /api/v1/registros-parqueo con valor negativo debe retornar 400 Bad Request")
    void testRegistrarIngreso_ValorNegativo_Retorna400() throws Exception {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "ABC123", LocalDateTime.now(), new BigDecimal("-500.00"), "PENDIENTE"
        );

        mockMvc.perform(post("/api/v1/registros-parqueo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.valorCalculado").exists());
    }

    @Test
    @DisplayName("POST /api/v1/registros-parqueo con estado no permitido debe retornar 400 Bad Request")
    void testRegistrarIngreso_EstadoInvalido_Retorna400() throws Exception {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "ABC123", LocalDateTime.now(), new BigDecimal("5000.00"), "ESTADO_DESCONOCIDO"
        );

        mockMvc.perform(post("/api/v1/registros-parqueo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.estadoPago").exists());
    }

    @Test
    @DisplayName("POST /api/v1/registros-parqueo con JSON mal formado debe retornar 400 Bad Request")
    void testRegistrarIngreso_JsonMalFormado_Retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/registros-parqueo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json: malformed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("mal formado")));
    }

    @Test
    @DisplayName("PUT /api/v1/registros-parqueo/{id} válido debe retornar 200 OK")
    void testActualizar_Valido() throws Exception {
        RegistroParqueoRequest request = new RegistroParqueoRequest(
                "ABC123", LocalDateTime.now(), new BigDecimal("10000.00"), "PAGADO"
        );

        RegistroParqueoResponse response = new RegistroParqueoResponse(
                1, "ABC123", request.getHoraIngreso(), new BigDecimal("10000.00"), "PAGADO"
        );

        when(registroParqueoService.actualizar(eq(1), any(RegistroParqueoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/registros-parqueo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRegistro").value(1))
                .andExpect(jsonPath("$.valorCalculado").value(10000.00))
                .andExpect(jsonPath("$.estadoPago").value("PAGADO"));
    }

    @Test
    @DisplayName("DELETE /api/v1/registros-parqueo/{id} exitoso debe retornar 204 No Content")
    void testEliminar_Exitoso() throws Exception {
        doNothing().when(registroParqueoService).eliminar(1);

        mockMvc.perform(delete("/api/v1/registros-parqueo/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/registros-parqueo/{id} con pagos asociados debe retornar 409 Conflict")
    void testEliminar_ConflictoIntegridad() throws Exception {
        doThrow(new IntegrityConflictException("Restricción de Integridad: No es posible eliminar el registro ID 1 porque tiene pagos asociados."))
                .when(registroParqueoService).eliminar(1);

        mockMvc.perform(delete("/api/v1/registros-parqueo/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("pagos asociados")));
    }
}
