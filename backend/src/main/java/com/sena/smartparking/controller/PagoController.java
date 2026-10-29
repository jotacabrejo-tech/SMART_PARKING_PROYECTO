package com.sena.smartparking.controller;

import com.sena.smartparking.dto.ErrorResponse;
import com.sena.smartparking.dto.PagoRequest;
import com.sena.smartparking.dto.PagoResponse;
import com.sena.smartparking.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para el recurso Pago.
 * Expone los servicios para registrar recaudos de tarifas y consultar el historial de pagos.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos y Recaudos", description = "Servicios RESTful para el registro y consulta de pagos de parqueadero")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Listar todos los pagos",
            description = "Retorna el historial completo de pagos y recaudos registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta ejecutada exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        List<PagoResponse> pagos = pagoService.listarTodos();
        return ResponseEntity.ok(pagos);
    }

    @GetMapping(value = "/{id:[0-9]+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar pago por ID",
            description = "Retorna el detalle de un pago mediante su identificador primario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PagoResponse> consultarPorId(
            @Parameter(description = "Identificador único del pago", required = true, example = "1")
            @PathVariable int id
    ) {
        PagoResponse pago = pagoService.consultarPorId(id);
        return ResponseEntity.ok(pago);
    }

    @GetMapping(value = "/registro/{idRegistro:[0-9]+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar pagos por registro de parqueo",
            description = "Retorna todos los pagos vinculados a un registro de ingreso de vehículo específico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagos asociados encontrados",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El registro de parqueo especificado no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<PagoResponse>> consultarPorRegistro(
            @Parameter(description = "ID del registro de parqueo", required = true, example = "1")
            @PathVariable int idRegistro
    ) {
        List<PagoResponse> pagos = pagoService.consultarPorRegistro(idRegistro);
        return ResponseEntity.ok(pagos);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Registrar nuevo pago de parqueadero",
            description = "Registra un pago liquidado para un vehículo en parqueo. Valida que el registro de parqueo exista y retorna la cabecera Location."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pago registrado exitosamente",
                    headers = @Header(name = "Location", description = "URI del pago registrado"),
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o campos obligatorios faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El registro de parqueo asociado no existe en la base de datos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al procesar el pago",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PagoResponse> registrarPago(
            @Valid @RequestBody PagoRequest request
    ) {
        PagoResponse creado = pagoService.registrarPago(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdPago())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }
}
