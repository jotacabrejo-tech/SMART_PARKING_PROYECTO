<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="moduloActivo" value="inicio" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Principal — Smart Parking Web (Servlets &amp; JSP)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <%@ include file="fragments/header.jspf" %>

    <main class="main-container">
        <section class="page-banner">
            <div>
                <h2>Panel Principal de Gestión — Smart Parking</h2>
                <p>Módulo web codificado con Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0 y JDBC conectado a MySQL (<code>smart_parking</code>).</p>
            </div>
            <div>
                <span class="badge badge-success">Tomcat 10.1 / Java 21</span>
            </div>
        </section>

        <c:if test="${not empty mensajeError}">
            <div class="alert alert-error" role="alert">
                <strong>Aviso del sistema:</strong> <c:out value="${mensajeError}" />
            </div>
        </c:if>

        <!-- Indicadores en tiempo real desde MySQL -->
        <section class="kpi-grid" aria-label="Indicadores del sistema">
            <div class="kpi-card">
                <span class="kpi-label">Registros de Parqueo</span>
                <div class="kpi-value"><c:out value="${totalRegistros}" default="0" /></div>
                <span class="kpi-sub">Total almacenados en <code>registro_parqueo</code></span>
            </div>
            <div class="kpi-card accent-amber">
                <span class="kpi-label">Pendientes de Pago</span>
                <div class="kpi-value"><c:out value="${registrosPendientes}" default="0" /></div>
                <span class="kpi-sub">Vehículos con estado <code>PENDIENTE</code></span>
            </div>
            <div class="kpi-card accent-green">
                <span class="kpi-label">Clientes Registrados</span>
                <div class="kpi-value"><c:out value="${totalClientes}" default="0" /></div>
                <span class="kpi-sub">Total almacenados en <code>cliente</code></span>
            </div>
            <div class="kpi-card accent-green">
                <span class="kpi-label">Pagos Procesados</span>
                <div class="kpi-value"><c:out value="${totalPagos}" default="0" /></div>
                <span class="kpi-sub">Total almacenados en <code>pago</code></span>
            </div>
        </section>

        <!-- Tarjetas de acceso a los 3 modulos -->
        <section class="module-grid" aria-label="Módulos funcionales">
            <article class="card">
                <div class="card-title">
                    <span>1. Módulo de Registros de Parqueo</span>
                    <span class="method-tag method-post">GET / POST</span>
                </div>
                <p style="margin-bottom: 1rem; color: var(--texto-secundario);">
                    Permite registrar el ingreso de vehículos (placa, fecha/hora, <code>valor_calculado</code> y estado) mediante
                    <code>doPost()</code> y consultar o filtrar por placa y estado mediante <code>doGet()</code>.
                </p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/parqueos">Ir a Registros de Parqueo</a>
            </article>

            <article class="card">
                <div class="card-title">
                    <span>2. Módulo de Clientes</span>
                    <span class="method-tag method-post">GET / POST</span>
                </div>
                <p style="margin-bottom: 1rem; color: var(--texto-secundario);">
                    Gestiona el registro de clientes validando tipo y número de documento único y correo electrónico mediante
                    <code>doPost()</code>, y permite búsquedas por documento mediante <code>doGet()</code>.
                </p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/clientes">Ir a Clientes</a>
            </article>

            <article class="card">
                <div class="card-title">
                    <span>3. Módulo de Pagos</span>
                    <span class="method-tag method-post">GET / POST</span>
                </div>
                <p style="margin-bottom: 1rem; color: var(--texto-secundario);">
                    Registra pagos asociados a un <code>id_registro</code> existente respetando la clave foránea
                    <code>fk_pago_registro</code> con <code>doPost()</code> y filtra pagos por registro con <code>doGet()</code>.
                </p>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/pagos">Ir a Pagos</a>
            </article>
        </section>

        <!-- Ultimos registros en MySQL -->
        <section class="card">
            <div class="card-title">
                <span>Últimos Registros de Parqueo en Base de Datos</span>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/parqueos">Ver todos</a>
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
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty ultimosRegistros}">
                                <c:forEach var="r" items="${ultimosRegistros}">
                                    <tr>
                                        <td><strong>#<c:out value="${r.idRegistro}" /></strong></td>
                                        <td><c:out value="${r.placa}" /></td>
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
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: var(--texto-secundario);">
                                        No hay registros de parqueo disponibles actualmente.
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

