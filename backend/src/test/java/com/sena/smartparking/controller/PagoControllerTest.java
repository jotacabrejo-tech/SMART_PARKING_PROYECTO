package com.sena.smartparking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.smartparking.dto.PagoRequest;
import com.sena.smartparking.dto.PagoResponse;
import com.sena.smartparking.exception.GlobalExceptionHandler;
import com.sena.smartparking.exception.ResourceNotFoundException;
import com.sena.smartparking.service.PagoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de MockMvc y validaciones para PagoController.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@WebMvcTest(PagoController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagoService pagoService;

    @Test
    @DisplayName("GET /api/v1/pagos debe retornar 200 OK y lista de pagos")
    void testListarTodos() throws Exception {
        PagoResponse pago = new PagoResponse(1, 10, LocalDateTime.now(), new BigDecimal("12000.00"), "PAGADO", 15);
        when(pagoService.listarTodos()).thenReturn(List.of(pago));

        mockMvc.perform(get("/api/v1/pagos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idPago").value(1))
                .andExpect(jsonPath("$[0].valorPagado").value(12000.00));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} existente debe retornar 200 OK")
    void testConsultarPorId_Existente() throws Exception {
        PagoResponse pago = new PagoResponse(1, 10, LocalDateTime.now(), new BigDecimal("12000.00"), "PAGADO", 15);
        when(pagoService.consultarPorId(1)).thenReturn(pago);

        mockMvc.perform(get("/api/v1/pagos/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.idRegistro").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} inexistente debe retornar 404 Not Found")
    void testConsultarPorId_Inexistente() throws Exception {
        when(pagoService.consultarPorId(99))
                .thenThrow(new ResourceNotFoundException("Pago con ID 99 no encontrado."));

        mockMvc.perform(get("/api/v1/pagos/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/registro/{idRegistro} existente debe retornar 200 OK")
    void testConsultarPorRegistro_Existente() throws Exception {
        PagoResponse pago = new PagoResponse(1, 10, LocalDateTime.now(), new BigDecimal("12000.00"), "PAGADO", 15);
        when(pagoService.consultarPorRegistro(10)).thenReturn(List.of(pago));

        mockMvc.perform(get("/api/v1/pagos/registro/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idRegistro").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/registro/{idRegistro} con parqueo inexistente debe retornar 404")
    void testConsultarPorRegistro_Inexistente() throws Exception {
        when(pagoService.consultarPorRegistro(999))
                .thenThrow(new ResourceNotFoundException("Registro de parqueo con ID 999 no encontrado."));

        mockMvc.perform(get("/api/v1/pagos/registro/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/pagos válido debe retornar 201 Created y Location")
    void testRegistrarPago_Valido() throws Exception {
        PagoRequest request = new PagoRequest(10, LocalDateTime.now(), new BigDecimal("7500.00"), "PAGADO", 15);
        PagoResponse response = new PagoResponse(1, 10, request.getFechaPago(), new BigDecimal("7500.00"), "PAGADO", 15);

        when(pagoService.registrarPago(any(PagoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/pagos/1")))
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.idRegistro").value(10))
                .andExpect(jsonPath("$.valorPagado").value(7500.00));
    }

    @Test
    @DisplayName("POST /api/v1/pagos con valor negativo o cero debe retornar 400 Bad Request")
    void testRegistrarPago_ValorInvalido_Retorna400() throws Exception {
        PagoRequest request = new PagoRequest(10, LocalDateTime.now(), new BigDecimal("0.00"), "PAGADO", 15);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.valorPagado").exists());
    }

    @Test
    @DisplayName("POST /api/v1/pagos sin id_registro debe retornar 400 Bad Request")
    void testRegistrarPago_SinIdRegistro_Retorna400() throws Exception {
        PagoRequest request = new PagoRequest(null, LocalDateTime.now(), new BigDecimal("5000.00"), "PAGADO", 15);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.idRegistro").exists());
    }

    @Test
    @DisplayName("POST /api/v1/pagos con estado de pago inválido debe retornar 400 Bad Request")
    void testRegistrarPago_EstadoInvalido_Retorna400() throws Exception {
        PagoRequest request = new PagoRequest(10, LocalDateTime.now(), new BigDecimal("5000.00"), "ESTADO_DESCONOCIDO", 15);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.estadoPago").exists());
    }

    @Test
    @DisplayName("POST /api/v1/pagos cuando el registro de parqueo no existe debe retornar 404 Not Found")
    void testRegistrarPago_RegistroParqueoNoExiste_Retorna404() throws Exception {
        PagoRequest request = new PagoRequest(999, LocalDateTime.now(), new BigDecimal("5000.00"), "PAGADO", 15);

        when(pagoService.registrarPago(any(PagoRequest.class)))
                .thenThrow(new ResourceNotFoundException("No se puede registrar el pago: El registro de parqueo con ID 999 no existe."));

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }
}
