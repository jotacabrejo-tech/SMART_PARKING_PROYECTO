<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="moduloActivo" value="clientes" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Clientes — Smart Parking Web</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <%@ include file="fragments/header.jspf" %>

    <main class="main-container">
        <section class="page-banner">
            <div>
                <h2>Módulo de Clientes (<code>/clientes</code>)</h2>
                <p>Registro y consulta de clientes conectados a <code>ClienteServlet</code> y tabla <code>cliente</code>.</p>
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
            <!-- Formulario HTML POST: Registrar nuevo cliente -->
            <section class="card">
                <div class="card-title">
                    <span>Registrar Nuevo Cliente</span>
                    <span class="method-tag method-post">POST /clientes</span>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/clientes">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="tipoDoc">Tipo de Documento *</label>
                            <select id="tipoDoc" name="tipoDoc" required>
                                <option value="CC" ${formTipoDoc == 'CC' || empty formTipoDoc ? 'selected' : ''}>CC — Cédula de Ciudadanía</option>
                                <option value="CE" ${formTipoDoc == 'CE' ? 'selected' : ''}>CE — Cédula de Extranjería</option>
                                <option value="NIT" ${formTipoDoc == 'NIT' ? 'selected' : ''}>NIT — Número Tributario</option>
                                <option value="TI" ${formTipoDoc == 'TI' ? 'selected' : ''}>TI — Tarjeta de Identidad</option>
                                <option value="PASAPORTE" ${formTipoDoc == 'PASAPORTE' ? 'selected' : ''}>PASAPORTE</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="numDoc">Número de Documento *</label>
                            <input type="text" id="numDoc" name="numDoc" required maxlength="20"
                                   placeholder="Ej. 1020304050"
                                   value="<c:out value='${formNumDoc}' />">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="nombres">Nombres Completos o Razón Social *</label>
                            <input type="text" id="nombres" name="nombres" required maxlength="100"
                                   placeholder="Ej. Laura Gómez Pérez"
                                   value="<c:out value='${formNombres}' />">
                        </div>
                        <div class="form-group">
                            <label for="correo">Correo Electrónico *</label>
                            <input type="email" id="correo" name="correo" required maxlength="100"
                                   placeholder="Ej. laura.gomez@correo.com"
                                   value="<c:out value='${formCorreo}' />">
                        </div>
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-success">Registrar Cliente (POST)</button>
                    </div>
                </form>
            </section>

            <!-- Formulario HTML GET: Buscar cliente por tipo y numero de documento -->
            <section class="card">
                <div class="card-title">
                    <span>Buscar Cliente por Documento</span>
                    <span class="method-tag method-get">GET /clientes</span>
                </div>
                <form method="get" action="${pageContext.request.contextPath}/clientes">
                    <div class="form-group">
                        <label for="tipoDocFiltro">Tipo de Documento</label>
                        <select id="tipoDocFiltro" name="tipoDoc">
                            <option value="">-- Todos los tipos --</option>
                            <option value="CC" ${tipoDocFiltro == 'CC' ? 'selected' : ''}>CC</option>
                            <option value="CE" ${tipoDocFiltro == 'CE' ? 'selected' : ''}>CE</option>
                            <option value="NIT" ${tipoDocFiltro == 'NIT' ? 'selected' : ''}>NIT</option>
                            <option value="TI" ${tipoDocFiltro == 'TI' ? 'selected' : ''}>TI</option>
                            <option value="PASAPORTE" ${tipoDocFiltro == 'PASAPORTE' ? 'selected' : ''}>PASAPORTE</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="numDocFiltro">Número de Documento</label>
                        <input type="text" id="numDocFiltro" name="numDoc" maxlength="20"
                               placeholder="Ej. 102030"
                               value="<c:out value='${numDocFiltro}' />">
                    </div>

                    <div class="btn-group">
                        <button type="submit" class="btn btn-primary">Buscar Cliente (GET)</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/clientes">Mostrar Todos</a>
                    </div>
                </form>
            </section>
        </div>

        <!-- Tabla dinamica JSP de Clientes -->
        <section class="card">
            <div class="card-title">
                <span>Listado de Clientes en MySQL</span>
                <c:if test="${not empty tipoDocFiltro or not empty numDocFiltro}">
                    <span class="badge badge-info">
                        Filtro activo:
                        <c:if test="${not empty tipoDocFiltro}">Tipo=<c:out value="${tipoDocFiltro}" /> </c:if>
                        <c:if test="${not empty numDocFiltro}">Documento="<c:out value="${numDocFiltro}" />"</c:if>
                    </span>
                </c:if>
            </div>
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>ID Cliente</th>
                            <th>Tipo Doc.</th>
                            <th>Número de Documento</th>
                            <th>Nombres / Razón Social</th>
                            <th>Correo Electrónico</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty clientes}">
                                <c:forEach var="c" items="${clientes}">
                                    <tr>
                                        <td><strong>#<c:out value="${c.idCliente}" /></strong></td>
                                        <td><span class="badge badge-info"><c:out value="${c.tipoDoc}" /></span></td>
                                        <td><strong><c:out value="${c.numDoc}" /></strong></td>
                                        <td><c:out value="${c.nombres}" /></td>
                                        <td><c:out value="${c.correo}" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: var(--texto-secundario);">
                                        No se encontraron clientes registrados para los criterios indicados.
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

