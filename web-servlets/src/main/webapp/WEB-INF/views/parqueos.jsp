<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="moduloActivo" value="parqueos" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registros de Parqueo — Smart Parking Web</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <%@ include file="fragments/header.jspf" %>

    <main class="main-container">
        <section class="page-banner">
            <div>
                <h2>Módulo de Registros de Parqueo (<code>/parqueos</code>)</h2>
                <p>Gestión de entradas de vehículos conectada a <code>RegistroParqueoServlet</code> y tabla <code>registro_parqueo</code>.</p>
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
            <!-- Formulario HTML POST: Registrar nuevo ingreso -->
            <section class="card">
                <div class="card-title">
                    <span>Registrar Ingreso de Vehículo</span>
                    <span class="method-tag method-post">POST /parqueos</span>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/parqueos">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="placa">Placa del Vehículo *</label>
                            <input type="text" id="placa" name="placa" required maxlength="10"
                                   placeholder="Ej. ABC123"
                                   value="<c:out value='${formPlaca}' />">
                        </div>
                        <div class="form-group">
                            <label for="valorCalculado">Valor Calculado / Tarifa ($) *</label>
                            <input type="number" id="valorCalculado" name="valorCalculado" required
                                   min="0" step="0.01" placeholder="Ej. 5000.00"
                                   value="<c:out value='${formValorCalculado}' default='5000.00' />">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="horaIngreso">Fecha y Hora de Ingreso (opcional)</label>
                            <input type="datetime-local" id="horaIngreso" name="horaIngreso"
                                   value="<c:out value='${formHoraIngreso}' />">
                        </div>
                        <div class="form-group">
                            <label for="estadoPago">Estado de Pago *</label>
                            <select id="estadoPago" name="estadoPago" required>
                                <option value="PENDIENTE" ${formEstadoPago == 'PENDIENTE' || empty formEstadoPago ? 'selected' : ''}>PENDIENTE</option>
                                <option value="PAGADO" ${formEstadoPago == 'PAGADO' ? 'selected' : ''}>PAGADO</option>
                                <option value="FINALIZADO" ${formEstadoPago == 'FINALIZADO' ? 'selected' : ''}>FINALIZADO</option>
                            </select>
                        </div>
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-success">Guardar Registro (POST)</button>
                    </div>
                </form>
            </section>

            <!-- Formulario HTML GET: Buscar y filtrar por placa y estado -->
            <section class="card">
                <div class="card-title">
                    <span>Consultar y Filtrar Registros</span>
                    <span class="method-tag method-get">GET /parqueos</span>
                </div>
                <form method="get" action="${pageContext.request.contextPath}/parqueos">
                    <div class="form-group">
                        <label for="placaFiltro">Buscar por Placa</label>
                        <input type="text" id="placaFiltro" name="placa" maxlength="10"
                               placeholder="Ej. ABC"
                               value="<c:out value='${placaFiltro}' />">
                    </div>

                    <div class="form-group">
                        <label for="estadoFiltro">Filtrar por Estado de Pago</label>
                        <select id="estadoFiltro" name="estadoPago">
                            <option value="">-- Todos los estados --</option>
                            <option value="PENDIENTE" ${estadoFiltro == 'PENDIENTE' ? 'selected' : ''}>PENDIENTE</option>
                            <option value="PAGADO" ${estadoFiltro == 'PAGADO' ? 'selected' : ''}>PAGADO</option>
                            <option value="FINALIZADO" ${estadoFiltro == 'FINALIZADO' ? 'selected' : ''}>FINALIZADO</option>
                        </select>
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-primary">Consultar (GET)</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/parqueos">Limpiar Filtros</a>
                    </div>
                </form>
            </section>
        </div>

        <!-- Listado dinamico JSP con JSTL -->
        <section class="card">
            <div class="card-title">
                <span>Listado de Registros de Parqueo en MySQL</span>
                <c:if test="${not empty placaFiltro or not empty estadoFiltro}">
                    <span class="badge badge-info">
                        Filtro activo:
                        <c:if test="${not empty placaFiltro}">Placa="<c:out value="${placaFiltro}" />" </c:if>
                        <c:if test="${not empty estadoFiltro}">Estado=<c:out value="${estadoFiltro}" /></c:if>
                    </span>
                </c:if>
            </div>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>ID Registro</th>
                            <th>Placa</th>
                            <th>Hora de Ingreso</th>
                            <th>Valor Calculado ($)</th>
                            <th>Estado de Pago</th>
                            <th>Acción Rápida</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty registros}">
                                <c:forEach var="r" items="${registros}">
                                    <tr>
                                        <td><strong>#<c:out value="${r.idRegistro}" /></strong></td>
                                        <td><strong><c:out value="${r.placa}" /></strong></td>
                                        <td><c:out value="${r.horaIngresoFormateada}" /></td>
                                        <td>$ <c:out value="${r.valorCalculado}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r.estadoPago == 'PAGADO'}">
                                                    <span class="badge badge-success"><c:out value="${r.estadoPago}" /></span>
                                                </c:when>
                                                <c:when test="${r.estadoPago == 'PENDIENTE'}">
                                                    <span class="badge badge-warning"><c:out value="${r.estadoPago}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-info"><c:out value="${r.estadoPago}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a class="btn btn-secondary" style="padding: 0.3rem 0.65rem; font-size: 0.8rem;"
                                               href="${pageContext.request.contextPath}/pagos?idRegistro=${r.idRegistro}">
                                                Ver Pagos (#<c:out value="${r.idRegistro}" />)
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--texto-secundario);">
                                        No se encontraron registros de parqueo para los criterios indicados.
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

