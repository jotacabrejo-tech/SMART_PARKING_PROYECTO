<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="moduloActivo" value="pagos" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Pagos — Smart Parking Web</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <%@ include file="fragments/header.jspf" %>

    <main class="main-container">
        <section class="page-banner">
            <div>
                <h2>Módulo de Pagos (<code>/pagos</code>)</h2>
                <p>Registro y consulta de pagos vinculados a <code>registro_parqueo</code> mediante <code>PagoServlet</code> y tabla <code>pago</code>.</p>
            </div>
            <div>
                <span class="badge badge-info">Total en vista: <c:out value="${totalResultados}" default="0" /></span>
            </div>
        </section>

        <!-- Mensajes de confirmacion o validacion -->
        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success" role="status">
                <strong>Operación exitosa:</strong> <c:out value="${mensajeExito}" />
            </div>
        </c:if>
        <c:if test="${not empty mensajeError}">
            <div class="alert alert-error" role="alert">
                <strong>Error de validación:</strong> <c:out value="${mensajeError}" />
            </div>
        </c:if>

        <div class="two-col-layout">
            <!-- Formulario HTML POST: Registrar nuevo pago -->
            <section class="card">
                <div class="card-title">
                    <span>Registrar Pago de Parqueo</span>
                    <span class="method-tag method-post">POST /pagos</span>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/pagos">
                    <div class="form-group">
                        <label for="idRegistro">Registro de Parqueo Asociado (<code>id_registro</code>) *</label>
                        <select id="idRegistro" name="idRegistro" required>
                            <option value="">-- Seleccione un registro de parqueo existente --</option>
                            <c:forEach var="reg" items="${registrosDisponibles}">
                                <option value="${reg.idRegistro}"
                                    ${formIdRegistro == reg.idRegistro.toString() || idRegistroFiltro == reg.idRegistro.toString() ? 'selected' : ''}>
                                    ID #<c:out value="${reg.idRegistro}" /> — Placa: <c:out value="${reg.placa}" />
                                    — Valor: $<c:out value="${reg.valorCalculado}" /> (<c:out value="${reg.estadoPago}" />)
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="valorPagado">Valor Pagado ($) *</label>
                            <input type="number" id="valorPagado" name="valorPagado" required
                                   min="0.01" step="0.01" placeholder="Ej. 5000.00"
                                   value="<c:out value='${formValorPagado}' default='5000.00' />">
                        </div>
                        <div class="form-group">
                            <label for="tiempoLimiteSalida">Tiempo Límite Salida (min) *</label>
                            <input type="number" id="tiempoLimiteSalida" name="tiempoLimiteSalida" required
                                   min="1" max="1440"
                                   value="<c:out value='${formTiempoLimiteSalida}' default='15' />">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="fechaPago">Fecha y Hora de Pago (opcional)</label>
                            <input type="datetime-local" id="fechaPago" name="fechaPago"
                                   value="<c:out value='${formFechaPago}' />">
                        </div>
                        <div class="form-group">
                            <label for="estadoPago">Estado del Pago *</label>
                            <select id="estadoPago" name="estadoPago" required>
                                <option value="APROBADO" ${formEstadoPago == 'APROBADO' || empty formEstadoPago ? 'selected' : ''}>APROBADO</option>
                                <option value="PAGADO" ${formEstadoPago == 'PAGADO' ? 'selected' : ''}>PAGADO</option>
                                <option value="PENDIENTE" ${formEstadoPago == 'PENDIENTE' ? 'selected' : ''}>PENDIENTE</option>
                            </select>
                        </div>
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-success">Registrar Pago (POST)</button>
                    </div>
                </form>
            </section>

            <!-- Formulario HTML GET: Filtrar pagos por ID de registro -->
            <section class="card">
                <div class="card-title">
                    <span>Filtrar Pagos por Registro</span>
                    <span class="method-tag method-get">GET /pagos</span>
                </div>
                <form method="get" action="${pageContext.request.contextPath}/pagos">
                    <div class="form-group">
                        <label for="idRegistroFiltro">Identificador de Registro (<code>id_registro</code>)</label>
                        <input type="number" id="idRegistroFiltro" name="idRegistro" min="1"
                               placeholder="Ej. 1"
                               value="<c:out value='${idRegistroFiltro}' />">
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-primary">Filtrar Pagos (GET)</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/pagos">Ver Todos los Pagos</a>
                    </div>
                </form>
            </section>
        </div>

        <!-- Tabla dinamica JSP de Pagos -->
        <section class="card">
            <div class="card-title">
                <span>Listado de Pagos en MySQL</span>
                <c:if test="${not empty idRegistroFiltro}">
                    <span class="badge badge-info">Filtrado por Registro #<c:out value="${idRegistroFiltro}" /></span>
                </c:if>
            </div>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>ID Pago</th>
                            <th>ID Registro Asociado</th>
                            <th>Fecha de Pago</th>
                            <th>Valor Pagado ($)</th>
                            <th>Estado de Pago</th>
                            <th>Tiempo Límite Salida</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty pagos}">
                                <c:forEach var="p" items="${pagos}">
                                    <tr>
                                        <td><strong>#<c:out value="${p.idPago}" /></strong></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/parqueos">
                                                <strong>Registro #<c:out value="${p.idRegistro}" /></strong>
                                            </a>
                                        </td>
                                        <td><c:out value="${p.fechaPagoFormateada}" /></td>
                                        <td>$ <c:out value="${p.valorPagado}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.estadoPago == 'APROBADO' || p.estadoPago == 'PAGADO'}">
                                                    <span class="badge badge-success"><c:out value="${p.estadoPago}" /></span>
                                                </c:when>
                                                <c:when test="${p.estadoPago == 'PENDIENTE'}">
                                                    <span class="badge badge-warning"><c:out value="${p.estadoPago}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-danger"><c:out value="${p.estadoPago}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${p.tiempoLimiteSalida}" /> min</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--texto-secundario);">
                                        No se encontraron pagos registrados para los criterios indicados.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <%@ include file="fragments/footer.jspf" %>
</body>
</html>

