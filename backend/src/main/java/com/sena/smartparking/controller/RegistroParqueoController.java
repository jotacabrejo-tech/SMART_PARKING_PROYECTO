package com.sena.smartparking.controller;

import com.sena.smartparking.dto.ConteoResponse;
import com.sena.smartparking.dto.ErrorResponse;
import com.sena.smartparking.dto.RegistroParqueoRequest;
import com.sena.smartparking.dto.RegistroParqueoResponse;
import com.sena.smartparking.service.RegistroParqueoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para el recurso RegistroParqueo.
 * Expone las operaciones para registrar ingresos, consultar historial, actualizar tarifas y eliminar registros.
 * 
 * Evidencia SENA: GA7-220501096-AA5-EV03
 */
@RestController
@RequestMapping("/api/v1/registros-parqueo")
@Tag(name = "Registros de Parqueo", description = "Servicios RESTful para el control de ingresos, liquidaciones, consultas y salida de vehículos")
public class RegistroParqueoController {

    private final RegistroParqueoService registroParqueoService;

    public RegistroParqueoController(RegistroParqueoService registroParqueoService) {
        this.registroParqueoService = registroParqueoService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Listar registros de parqueo",
            description = "Retorna todos los registros de parqueo almacenados en la base de datos. Permite filtrar de forma opcional por la placa del vehículo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta ejecutada con éxito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RegistroParqueoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<RegistroParqueoResponse>> listarTodos(
            @Parameter(description = "Placa del vehículo para filtrar resultados (ej. ABC123)", required = false)
            @RequestParam(required = false) String placa
    ) {
        List<RegistroParqueoResponse> resultado = registroParqueoService.listarTodos(placa);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping(value = "/conteo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Conteo total de registros",
            description = "Retorna el total de vehículos registrados en el histórico del parqueadero."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteo calculado exitosamente",
                    content = @Content(schema = @Schema(implementation = ConteoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ConteoResponse> contar() {
        ConteoResponse conteo = registroParqueoService.contarRegistros();
        return ResponseEntity.ok(conteo);
    }

    @GetMapping(value = "/{id:[0-9]+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Consultar registro por ID",
            description = "Retorna el detalle completo de un registro de parqueo mediante su identificador primario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro encontrado",
                    content = @Content(schema = @Schema(implementation = RegistroParqueoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Registro no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RegistroParqueoResponse> consultarPorId(
            @Parameter(description = "ID del registro de parqueo", required = true, example = "1")
            @PathVariable int id
    ) {
        RegistroParqueoResponse registro = registroParqueoService.consultarPorId(id);
        return ResponseEntity.ok(registro);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Registrar nuevo ingreso de vehículo",
            description = "Crea un nuevo registro de entrada en la tabla registro_parqueo. Asigna automáticamente el ID generado y retorna la cabecera Location."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ingreso registrado exitosamente",
                    headers = @Header(name = "Location", description = "URI del nuevo recurso creado"),
                    content = @Content(schema = @Schema(implementation = RegistroParqueoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o campos requeridos faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al persistir el registro",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RegistroParqueoResponse> registrarIngreso(
            @Valid @RequestBody RegistroParqueoRequest request
    ) {
        RegistroParqueoResponse creado = registroParqueoService.registrarIngreso(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdRegistro())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping(value = "/{id:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Actualizar registro de parqueo",
            description = "Modifica los datos de un registro existente (tarifa liquidada, estado de pago, hora o placa)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = RegistroParqueoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Registro no encontrado para actualizar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al actualizar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RegistroParqueoResponse> actualizar(
            @Parameter(description = "ID del registro a actualizar", required = true, example = "1")
            @PathVariable int id,
            @Valid @RequestBody RegistroParqueoRequest request
    ) {
        RegistroParqueoResponse actualizado = registroParqueoService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id:[0-9]+}")
    @Operation(
            summary = "Eliminar registro de parqueo",
            description = "Elimina un registro de parqueo si no posee pagos vinculados en la tabla pago (protegido por integridad referencial)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Registro eliminado exitosamente (sin contenido)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Registro no encontrado para eliminar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: No se puede eliminar porque posee pagos asociados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al eliminar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del registro a eliminar", required = true, example = "1")
            @PathVariable int id
    ) {
        registroParqueoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
