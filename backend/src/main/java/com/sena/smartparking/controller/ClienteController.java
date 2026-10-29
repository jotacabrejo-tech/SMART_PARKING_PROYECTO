package com.sena.smartparking.controller;

import com.sena.smartparking.dto.ClienteRequest;
import com.sena.smartparking.dto.ClienteResponse;
import com.sena.smartparking.dto.ErrorResponse;
import com.sena.smartparking.service.ClienteService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para el recurso Cliente.
 * Gestiona el alta, consulta y modificación de clientes del sistema de parqueadero.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Servicios RESTful para la administración de clientes y propietarios de vehículos")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Listar todos los clientes",
            description = "Retorna la colección completa de clientes registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta ejecutada exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClienteResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        List<ClienteResponse> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping(value = "/{id:[0-9]+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar cliente por ID",
            description = "Retorna el detalle del cliente especificado por su identificador primario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ClienteResponse> consultarPorId(
            @Parameter(description = "Identificador único del cliente", required = true, example = "1")
            @PathVariable int id
    ) {
        ClienteResponse cliente = clienteService.consultarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Registrar nuevo cliente",
            description = "Crea un nuevo registro de cliente en la base de datos. Valida formato de documento, nombres y correo, evitando duplicados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    headers = @Header(name = "Location", description = "URI del nuevo cliente creado"),
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o campos obligatorios faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: Ya existe un cliente con el mismo tipo y número de documento",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al crear el cliente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ClienteResponse> registrarCliente(
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse creado = clienteService.registrarCliente(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdCliente())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping(value = "/{id:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Actualizar cliente existente",
            description = "Actualiza los datos personales (tipo_doc, num_doc, nombres, correo) del cliente especificado por su ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado para actualizar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: El documento especificado ya pertenece a otro cliente registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al actualizar el cliente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ClienteResponse> actualizar(
            @Parameter(description = "Identificador único del cliente a actualizar", required = true, example = "1")
            @PathVariable int id,
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse actualizado = clienteService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }
}
