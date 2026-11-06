/*==========================================================
 SMART PARKING
 Sistema de Gestión de Parqueaderos
 Proyecto ADSO - SENA

 Archivo:
 js/script.js

 Evidencia SENA:
 GA7-220501096-AA3-EV01 — Codificación de módulos del software stand-alone, web y móvil

 Descripción:
 Integración del Front-End responsivo con la API REST de Spring Boot
 mediante Fetch API (SmartParkingApi), conservando el diseño visual
 y distinguiendo explícitamente entre datos persistidos en MySQL
 y datos complementarios locales de demostración.
==========================================================*/

"use strict";

/*==========================================================
 1. CONFIGURACIÓN GLOBAL Y CONSTANTES DEL PROYECTO
==========================================================*/

const SmartParking = {
    version: "2.0.0",
    appName: "SMART PARKING",
    loginDelay: 800,
    /**
     * Capacidad total de estacionamiento configurada en el frontend.
     * NOTA TÉCNICA: Este valor es un parámetro de configuración local (100 cupos)
     * y no proviene de una tabla en MySQL.
     */
    capacidadConfigurada: 100,
    /**
     * Tarifas existentes en el proyecto (referenciadas en Main.java,
     * RegistroParqueoRequest.java, PagoRequest.java y RegistroParqueoControllerTest.java).
     */
    tarifasProyecto: {
        "Hora": 5000.00,
        "Convenio": 4500.00,
        "Día": 12500.00,
        "Mensualidad": 10000.00,
        "DEFAULT": 5000.00
    },
    /**
     * Clave de almacenamiento local para atributos complementarios de interfaz
     * (color, tipoVehiculo, telefono, cupo, servicio, estadoVehiculo, observaciones)
     * que no forman parte de las tablas MySQL (registro_parqueo, cliente, pago).
     */
    localMetadataKey: "sp_complementarios_locales_v1"
};

/*==========================================================
 2. CLIENTE HTTP CENTRALIZADO — SmartParkingApi (Fetch API)
==========================================================*/

const SmartParkingApi = (() => {
    const DEFAULT_BASE_URL = "http://localhost:8080/api/v1";
    const DEFAULT_TIMEOUT_MS = 8000;

    /**
     * Obtiene la URL base de la API REST (configurable mediante window.SMART_PARKING_API_URL
     * o localStorage 'sp_api_base_url', por defecto http://localhost:8080/api/v1).
     */
    function getBaseUrl() {
        if (window.SMART_PARKING_API_URL) {
            return String(window.SMART_PARKING_API_URL).replace(/\/+$/, "");
        }
        const savedUrl = localStorage.getItem("sp_api_base_url");
        if (savedUrl && savedUrl.trim() !== "") {
            return savedUrl.trim().replace(/\/+$/, "");
        }
        return DEFAULT_BASE_URL;
    }

    /**
     * Construye un error enriquecido con código de estado HTTP y detalles de validación
     * devueltos por GlobalExceptionHandler (ErrorResponse en el backend).
     */
    function buildApiError(status, payload, fallbackMessage) {
        let msg = fallbackMessage || `Error HTTP ${status}`;
        let fieldErrors = null;

        if (payload && typeof payload === "object") {
            if (payload.message) {
                msg = payload.message;
            } else if (payload.error) {
                msg = `${payload.error} (HTTP ${status})`;
            }
            if (payload.fieldErrors && typeof payload.fieldErrors === "object") {
                fieldErrors = payload.fieldErrors;
                const detalles = Object.entries(payload.fieldErrors)
                    .map(([campo, detalle]) => `${campo}: ${detalle}`)
                    .join(" | ");
                if (detalles) {
                    msg = `${msg} (${detalles})`;
                }
            }
        }

        const error = new Error(msg);
        error.status = status;
        error.payload = payload;
        error.fieldErrors = fieldErrors;
        error.isConnectionError = false;
        return error;
    }

    /**
     * Ejecuta una solicitud HTTP hacia la API REST de Spring Boot gestionando:
     * - Timeouts con AbortController
     * - Cabeceras JSON
     * - Respuestas sin contenido (HTTP 204)
     * - Conversión segura de JSON y captura de ErrorResponse del backend
     * - Errores de red / servidor apagado
     */
    async function request(method, path, body = undefined, customTimeout = DEFAULT_TIMEOUT_MS) {
        const controller = new AbortController();
        const timerId = setTimeout(() => controller.abort(), customTimeout);

        const cleanPath = path.startsWith("/") ? path : `/${path}`;
        const url = `${getBaseUrl()}${cleanPath}`;

        const headers = {
            "Accept": "application/json"
        };
        if (body !== undefined && body !== null) {
            headers["Content-Type"] = "application/json";
        }

        try {
            const response = await fetch(url, {
                method,
                headers,
                body: body !== undefined && body !== null ? JSON.stringify(body) : undefined,
                signal: controller.signal
            });

            clearTimeout(timerId);

            // Gestión de respuestas sin contenido (HTTP 204 No Content)
            if (response.status === 204) {
                return null;
            }

            const rawText = await response.text();
            let parsedData = null;

            if (rawText && rawText.trim().length > 0) {
                try {
                    parsedData = JSON.parse(rawText);
                } catch (_jsonErr) {
                    if (response.ok) {
                        throw new Error("El servidor respondió con un formato JSON inválido.");
                    }
                }
            }

            if (!response.ok) {
                throw buildApiError(
                    response.status,
                    parsedData,
                    `La solicitud ${method} ${cleanPath} falló con código HTTP ${response.status}.`
                );
            }

            return parsedData;
        } catch (err) {
            clearTimeout(timerId);

            if (err.name === "AbortError") {
                const timeoutError = new Error(
                    `Tiempo de espera agotado (${customTimeout / 1000}s) al conectar con ${getBaseUrl()}. Verifique que el backend Spring Boot esté activo.`
                );
                timeoutError.status = 0;
                timeoutError.isConnectionError = true;
                throw timeoutError;
            }

            if (err instanceof TypeError || err.isConnectionError) {
                const netError = new Error(
                    `No fue posible conectar con la API REST en ${getBaseUrl()}. Verifique que el servidor Spring Boot esté en ejecución en el puerto 8080 y que el origen esté autorizado por CORS.`
                );
                netError.status = 0;
                netError.isConnectionError = true;
                throw netError;
            }

            throw err;
        }
    }

    return {
        getBaseUrl,
        get: (path) => request("GET", path),
        post: (path, body) => request("POST", path, body),
        put: (path, body) => request("PUT", path, body),
        delete: (path) => request("DELETE", path),

        // Endpoints de Salud (/api/v1/health)
        checkHealth: () => request("GET", "/health"),

        // Endpoints de Registros de Parqueo (/api/v1/registros-parqueo)
        listarRegistros: (placa = "") => {
            const filtro = placa ? `?placa=${encodeURIComponent(placa.trim().toUpperCase())}` : "";
            return request("GET", `/registros-parqueo${filtro}`);
        },
        contarRegistros: () => request("GET", "/registros-parqueo/conteo"),
        consultarRegistroPorId: (id) => request("GET", `/registros-parqueo/${encodeURIComponent(id)}`),
        crearRegistro: (registroRequest) => request("POST", "/registros-parqueo", registroRequest),
        actualizarRegistro: (id, registroRequest) => request("PUT", `/registros-parqueo/${encodeURIComponent(id)}`, registroRequest),
        eliminarRegistro: (id) => request("DELETE", `/registros-parqueo/${encodeURIComponent(id)}`),

        // Endpoints de Clientes (/api/v1/clientes)
        listarClientes: (numDocParam = "") => {
            const query = numDocParam ? `?numDoc=${encodeURIComponent(numDocParam.trim().toUpperCase())}` : "";
            return request("GET", `/clientes${query}`);
        },
        consultarClientePorId: (id) => request("GET", `/clientes/${encodeURIComponent(id)}`),
        crearCliente: (clienteRequest) => request("POST", "/clientes", clienteRequest),
        actualizarCliente: (id, clienteRequest) => request("PUT", `/clientes/${encodeURIComponent(id)}`, clienteRequest),

        /**
         * Busca un cliente en la colección real devuelta por GET /api/v1/clientes
         * filtrando por número de documento y/o correo electrónico.
         */
        buscarClientePorDocumentoOCorreo: async (numDoc, correo = "") => {
            const docLimpio = (numDoc || "").trim().toUpperCase();
            const correoLimpio = (correo || "").trim().toLowerCase();
            const clientes = await request("GET", `/clientes${docLimpio ? `?numDoc=${encodeURIComponent(docLimpio)}` : ""}`);
            if (!Array.isArray(clientes)) return null;

            return clientes.find(c => {
                const coincideDoc = docLimpio && String(c.numDoc || "").trim().toUpperCase() === docLimpio;
                const coincideCorreo = correoLimpio && String(c.correo || "").trim().toLowerCase() === correoLimpio;
                return coincideDoc || coincideCorreo;
            }) || null;
        },

        // Endpoints de Pagos (/api/v1/pagos)
        listarPagos: () => request("GET", "/pagos"),
        consultarPagoPorId: (id) => request("GET", `/pagos/${encodeURIComponent(id)}`),
        consultarPagosPorRegistro: (idRegistro) => request("GET", `/pagos/registro/${encodeURIComponent(idRegistro)}`),
        registrarPago: (pagoRequest) => request("POST", "/pagos", pagoRequest)
    };
})();

window.SmartParkingApi = SmartParkingApi;

/*==========================================================
 3. GESTIÓN DE METADATOS COMPLEMENTARIOS LOCALES (NO MYSQL)
==========================================================*/

/**
 * Lee del almacenamiento local del navegador los atributos complementarios
 * de interfaz (color, tipoVehiculo, cupo, servicio, telefono, observaciones)
 * que no existen en las tablas MySQL.
 */
function obtenerMetadatosLocales() {
    try {
        const raw = localStorage.getItem(SmartParking.localMetadataKey);
        return raw ? JSON.parse(raw) : {};
    } catch (_e) {
        return {};
    }
}

function guardarMetadatoLocalPorPlaca(placa, datos) {
    if (!placa) return;
    try {
        const mapa = obtenerMetadatosLocales();
        const clave = placa.trim().toUpperCase();
        mapa[clave] = {
            ...(mapa[clave] || {}),
            ...datos,
            actualizadoEn: new Date().toISOString()
        };
        localStorage.setItem(SmartParking.localMetadataKey, JSON.stringify(mapa));
    } catch (_e) {
        // Ignorar si localStorage está restringido
    }
}

function consultarMetadatoLocalPorPlaca(placa) {
    if (!placa) return null;
    const mapa = obtenerMetadatosLocales();
    return mapa[placa.trim().toUpperCase()] || null;
}

/*==========================================================
 4. INICIALIZACIÓN GENERAL DE LA APLICACIÓN
==========================================================*/

document.addEventListener("DOMContentLoaded", () => {
    initializeApp();
});

function initializeApp() {
    initializeLogin();
    initializeDashboard();
    initializeIngreso();
    initializeConsulta();
}

/*==========================================================
 5. MÓDULO DE LOGIN (DEMOSTRACIÓN LOCAL — SIN ENDPOINT AUTH)
==========================================================*/

function initializeLogin() {
    const loginForm = document.getElementById("loginForm");
    if (!loginForm) return;

    initializePassword();
    initializeRememberMe();
    loginForm.addEventListener("submit", loginSubmit);
}

function initializePassword() {
    const button = document.getElementById("togglePassword");
    const password = document.getElementById("password");
    if (!button || !password) return;

    button.addEventListener("click", () => {
        const hidden = password.type === "password";
        password.type = hidden ? "text" : "password";
        button.innerHTML = hidden
            ? '<i class="bi bi-eye-slash-fill"></i>'
            : '<i class="bi bi-eye-fill"></i>';
    });
}

function initializeRememberMe() {
    const input = document.getElementById("usuario");
    const remember = document.getElementById("rememberMe");
    if (!input || !remember) return;

    const saved = localStorage.getItem("sp_user");
    if (saved) {
        input.value = saved;
        remember.checked = true;
    }
}

function loginSubmit(event) {
    event.preventDefault();

    const usuario = document.getElementById("usuario");
    const password = document.getElementById("password");
    const remember = document.getElementById("rememberMe");

    clearValidation();

    let valido = true;
    if (!usuario || usuario.value.trim() === "") {
        if (usuario) showError(usuario);
        valido = false;
    }
    if (!password || password.value.trim() === "") {
        if (password) showError(password);
        valido = false;
    }
    if (!valido) return;

    // Guardar únicamente el identificador de usuario si activa "Recordarme" (nunca contraseñas)
    if (remember && remember.checked) {
        localStorage.setItem("sp_user", usuario.value.trim());
    } else {
        localStorage.removeItem("sp_user");
    }

    const boton = document.querySelector("#loginForm button[type='submit']");
    if (boton) {
        boton.disabled = true;
        boton.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2"></span>
            Ingresando (modo demostración)...
        `;
    }

    setTimeout(() => {
        window.location.href = "dashboard.html";
    }, SmartParking.loginDelay);
}

/*==========================================================
 6. UTILIDADES DE INTERFAZ, FORMATO Y SANITIZACIÓN
==========================================================*/

let currentButton = null;
let originalButton = "";

function startLoading(button, text = "Procesando...") {
    if (!button) return;
    currentButton = button;
    originalButton = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${escapeHtml(text)}`;
}

function stopLoading(button = currentButton) {
    if (!button) return;
    button.disabled = false;
    if (button === currentButton && originalButton) {
        button.innerHTML = originalButton;
    }
}

function showError(input, message = "") {
    if (!input) return;
    input.classList.add("is-invalid");
    if (message && typeof input.setCustomValidity === "function") {
        input.setCustomValidity(message);
    }
    input.focus();
}

function clearValidation() {
    document.querySelectorAll(".is-invalid").forEach(item => {
        item.classList.remove("is-invalid");
        if (typeof item.setCustomValidity === "function") {
            item.setCustomValidity("");
        }
    });
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function formatMoney(value) {
    const num = Number(value);
    if (Number.isNaN(num)) return "$0";
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(num);
}

function parseLocalDateTime(isoString) {
    if (!isoString) return null;
    if (Array.isArray(isoString)) {
        // Soporte por si Jackson serializa como array [yyyy, MM, dd, HH, mm, ss]
        const [y, m, d, hh = 0, mm = 0, ss = 0] = isoString;
        return new Date(y, m - 1, d, hh, mm, ss);
    }
    const clean = String(isoString).trim().replace(" ", "T");
    const parsed = new Date(clean);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDate(dateInput) {
    const d = dateInput instanceof Date ? dateInput : parseLocalDateTime(dateInput);
    if (!d) return "--";
    return d.toLocaleDateString("es-CO", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit"
    });
}

function formatTime(dateInput) {
    const d = dateInput instanceof Date ? dateInput : parseLocalDateTime(dateInput);
    if (!d) return "--";
    return d.toLocaleTimeString("es-CO", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });
}

function isSameLocalDay(dateA, dateB) {
    if (!dateA || !dateB) return false;
    return (
        dateA.getFullYear() === dateB.getFullYear() &&
        dateA.getMonth() === dateB.getMonth() &&
        dateA.getDate() === dateB.getDate()
    );
}

function formatLocalIsoSeconds(dateObj) {
    const pad = (n) => String(n).padStart(2, "0");
    return (
        `${dateObj.getFullYear()}-${pad(dateObj.getMonth() + 1)}-${pad(dateObj.getDate())}` +
        `T${pad(dateObj.getHours())}:${pad(dateObj.getMinutes())}:${pad(dateObj.getSeconds())}`
    );
}

function calcularTiempoTranscurridoTexto(fechaInicio, fechaFin = new Date()) {
    const inicio = fechaInicio instanceof Date ? fechaInicio : parseLocalDateTime(fechaInicio);
    const fin = fechaFin instanceof Date ? fechaFin : parseLocalDateTime(fechaFin);
    if (!inicio || !fin) return "--";

    const diffMs = Math.max(0, fin.getTime() - inicio.getTime());
    const totalMinutos = Math.floor(diffMs / 60000);
    const dias = Math.floor(totalMinutos / 1440);
    const horas = Math.floor((totalMinutos % 1440) / 60);
    const minutos = totalMinutos % 60;

    const partes = [];
    if (dias > 0) partes.push(`${dias} ${dias === 1 ? "Día" : "Días"}`);
    partes.push(`${horas} ${horas === 1 ? "Hora" : "Horas"}`);
    partes.push(`${minutos} ${minutos === 1 ? "Minuto" : "Minutos"}`);
    return partes.join(" ");
}

function setValue(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    if (element.tagName === "INPUT" || element.tagName === "TEXTAREA" || element.tagName === "SELECT") {
        element.value = value;
    } else {
        element.textContent = value;
    }
}

/**
 * Muestra un banner de estado o alerta dentro de .page-content utilizando las clases
 * existentes en css/style.css (.alert-custom, .alert-success, .alert-danger, .alert-warning, .alert-info).
 */
function renderPageNotification(type, title, message, containerId = "spNotificationBanner") {
    const pageContent = document.querySelector(".page-content");
    if (!pageContent) return;

    let banner = document.getElementById(containerId);
    if (!banner) {
        banner = document.createElement("div");
        banner.id = containerId;
        banner.setAttribute("role", "alert");
        banner.setAttribute("aria-live", "polite");
        pageContent.insertBefore(banner, pageContent.firstChild);
    }

    const iconMap = {
        success: "bi-check-circle-fill",
        danger: "bi-exclamation-triangle-fill",
        warning: "bi-exclamation-circle-fill",
        info: "bi-info-circle-fill"
    };
    const alertClass = `alert-custom alert-${type || "info"} fade-in`;
    const icon = iconMap[type] || iconMap.info;

    banner.className = alertClass;
    banner.innerHTML = `
        <i class="bi ${icon}" style="font-size: 1.4rem;"></i>
        <div class="flex-grow-1">
            <strong>${escapeHtml(title)}</strong>
            <div>${escapeHtml(message)}</div>
        </div>
    `;
}

function clearPageNotification(containerId = "spNotificationBanner") {
    const banner = document.getElementById(containerId);
    if (banner) {
        banner.remove();
    }
}

/*==========================================================
 7. MÓDULO DASHBOARD (dashboard.html)
==========================================================*/

let clockInterval = null;

function initializeDashboard() {
    const dashboard = document.getElementById("dashboardPage");
    if (!dashboard) return;

    initializeClock();
    loadDashboard();
}

function initializeClock() {
    updateClock();
    if (clockInterval) clearInterval(clockInterval);
    clockInterval = setInterval(updateClock, 1000);
}

function updateClock() {
    const time = document.getElementById("currentTime");
    const date = document.getElementById("currentDate");
    if (!time || !date) return;

    const now = new Date();
    time.textContent = now.toLocaleTimeString("es-CO");
    date.textContent = now.toLocaleDateString("es-CO", {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric"
    });
}

/**
 * Carga todos los datos reales del Dashboard desde la API REST de Spring Boot:
 * - GET /api/v1/health
 * - GET /api/v1/registros-parqueo/conteo
 * - GET /api/v1/registros-parqueo
 * - GET /api/v1/pagos
 * - GET /api/v1/clientes
 */
async function loadDashboard() {
    clearPageNotification();

    try {
        const [health, conteoResp, registros, pagos, clientes] = await Promise.all([
            SmartParkingApi.checkHealth(),
            SmartParkingApi.contarRegistros(),
            SmartParkingApi.listarRegistros(),
            SmartParkingApi.listarPagos(),
            SmartParkingApi.listarClientes()
        ]);

        actualizarWidgetsSistemaDashboard(health, true);
        actualizarIndicadoresRealesDashboard(conteoResp, registros, pagos);
        actualizarActividadRecienteDashboard(registros, pagos);
        actualizarTablaMovimientosDashboard(registros);
        actualizarResumenComportamientoDashboard(conteoResp, registros, pagos, clientes);
    } catch (error) {
        manejarDesconexionDashboard(error);
    }
}

/**
 * Actualiza los 3 widgets inferiores del Dashboard ("Estado del Sistema", "Base de Datos", "Versión")
 * usando la respuesta real de GET /api/v1/health.
 */
function actualizarWidgetsSistemaDashboard(health, online = true) {
    const widgets = document.querySelectorAll("#dashboardPage .widget");
    widgets.forEach(widget => {
        const titleEl = widget.querySelector(".widget-title h5");
        if (!titleEl) return;
        const titulo = titleEl.textContent.trim().toLowerCase();

        if (titulo.includes("estado del sistema")) {
            const p = widget.querySelector("p");
            if (p) {
                if (online && health && health.status === "UP") {
                    p.className = "status-online";
                    p.textContent = `● ${health.application || "Smart Parking API"} (${health.status})`;
                } else {
                    p.className = "status-offline";
                    p.textContent = "● Backend Desconectado (Puerto 8080)";
                }
            }
        } else if (titulo.includes("base de datos")) {
            const p = widget.querySelector("p");
            if (p) {
                if (online && health && health.database === "CONNECTED") {
                    p.className = "status-online";
                    p.textContent = "● MySQL Conectada (CONNECTED)";
                } else if (online && health) {
                    p.className = "status-offline";
                    p.textContent = `● MySQL: ${health.database || "DISCONNECTED"}`;
                } else {
                    p.className = "status-offline";
                    p.textContent = "● Sin verificación (API fuera de línea)";
                }
            }
        } else if (titulo.includes("versión") || titulo.includes("version")) {
            const val = widget.querySelector(".widget-value");
            if (val) {
                val.textContent = online && health && health.version ? `API v${health.version}` : "Sin conexión";
            }
        }
    });
}

/**
 * Calcula los 4 indicadores KPI del Dashboard a partir de registros reales de MySQL.
 *
 * FÓRMULAS DOCUMENTADAS:
 * 1. Vehículos (#kpiVehiculos):
 *    - Se obtiene la lista real de `GET /api/v1/registros-parqueo` y el total de `GET /api/v1/registros-parqueo/conteo`.
 *    - Se cuentan los registros cuya `horaIngreso` corresponde al día actual (`vehiculosHoy`)
 *      y los vehículos actualmente en parqueadero con `estadoPago === "PENDIENTE"` (`vehiculosActivos`).
 *    - El valor principal muestra `vehiculosHoy` (si es > 0) o `vehiculosActivos`, detallando ambos y el total histórico en el subtítulo.
 * 2. Cupos Disponibles (#kpiCupos):
 *    - Capacidad configurada en frontend = 100 cupos (no proviene de MySQL).
 *    - Fórmula: `cuposDisponibles = Math.max(0, 100 - vehiculosActivos)`.
 * 3. Ingresos (#kpiIngresos):
 *    - Se calcula EXCLUSIVAMENTE a partir de pagos efectivamente registrados en la tabla `pago` (`GET /api/v1/pagos`)
 *      con estado `PAGADO` o `APROBADO`. NO se suman `valorCalculado` de registros pendientes.
 *    - Si hay pagos realizados hoy, muestra el recaudo de hoy; en el subtítulo informa el recaudo total histórico confirmado en MySQL.
 * 4. Ocupación (#kpiOcupacion):
 *    - Fórmula: `Math.round((vehiculosActivos / 100) * 100)%` sobre la capacidad configurada de 100 cupos.
 */
function actualizarIndicadoresRealesDashboard(conteoResp, registros, pagos) {
    const listaRegistros = Array.isArray(registros) ? registros : [];
    const listaPagos = Array.isArray(pagos) ? pagos : [];
    const totalHistoricoBD = conteoResp && typeof conteoResp.total === "number"
        ? conteoResp.total
        : listaRegistros.length;

    const hoy = new Date();

    const vehiculosHoy = listaRegistros.filter(r => {
        const fecha = parseLocalDateTime(r.horaIngreso);
        return isSameLocalDay(fecha, hoy);
    }).length;

    const vehiculosActivos = listaRegistros.filter(
        r => String(r.estadoPago || "").toUpperCase() === "PENDIENTE"
    ).length;

    const capacidad = SmartParking.capacidadConfigurada;
    const cuposDisponibles = Math.max(0, capacidad - vehiculosActivos);
    const porcentajeOcupacion = Math.min(100, Math.round((vehiculosActivos / capacidad) * 100));

    // Solo pagos efectivamente recibidos en la tabla `pago` (PAGADO o APROBADO)
    const pagosConfirmados = listaPagos.filter(p => {
        const est = String(p.estadoPago || "").toUpperCase();
        return est === "PAGADO" || est === "APROBADO";
    });

    const ingresosHoyConfirmados = pagosConfirmados
        .filter(p => isSameLocalDay(parseLocalDateTime(p.fechaPago), hoy))
        .reduce((acc, p) => acc + (Number(p.valorPagado) || 0), 0);

    const ingresosTotalesConfirmados = pagosConfirmados
        .reduce((acc, p) => acc + (Number(p.valorPagado) || 0), 0);

    setValue("kpiVehiculos", vehiculosHoy);
    setValue("kpiCupos", cuposDisponibles);
    setValue("kpiIngresos", formatMoney(ingresosHoyConfirmados));
    setValue("kpiOcupacion", `${porcentajeOcupacion}%`);

    // Actualizar subtítulos explicativos (<small>) de cada tarjeta KPI para máxima transparencia
    actualizarSubtituloKpi("kpiVehiculos", `Hoy: ${vehiculosHoy} | En parqueadero (PENDIENTE): ${vehiculosActivos} | Total BD: ${totalHistoricoBD}`);
    actualizarSubtituloKpi("kpiCupos", `Capacidad configurada: ${capacidad} cupos (Ocupados: ${vehiculosActivos})`);
    actualizarSubtituloKpi("kpiIngresos", `Pagos hoy (tabla pago) | Total recaudado BD: ${formatMoney(ingresosTotalesConfirmados)}`);
    actualizarSubtituloKpi("kpiOcupacion", `${vehiculosActivos} vehículos activos sobre ${capacidad} cupos configurados`);
}

function actualizarSubtituloKpi(kpiId, texto) {
    const kpiEl = document.getElementById(kpiId);
    if (!kpiEl || !kpiEl.parentElement) return;
    const small = kpiEl.parentElement.querySelector("small");
    if (small) {
        small.textContent = texto;
    }
}

/**
 * Actualiza el panel `#recentActivity` con los últimos registros reales de MySQL.
 */
function actualizarActividadRecienteDashboard(registros, pagos) {
    const container = document.getElementById("recentActivity");
    if (!container) return;

    container.innerHTML = "";
    const lista = Array.isArray(registros) ? registros.slice(0, 5) : [];

    if (lista.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4 text-muted">
                No existen registros de parqueo en MySQL.
            </div>
        `;
        return;
    }

    const pagosPorRegistro = new Set(
        (Array.isArray(pagos) ? pagos : []).map(p => Number(p.idRegistro))
    );

    lista.forEach(item => {
        const estado = String(item.estadoPago || "").toUpperCase();
        const esPagado = estado === "PAGADO" || pagosPorRegistro.has(Number(item.idRegistro));
        const accionTexto = esPagado ? "Salida / Pagado" : "Ingreso (Activo)";
        const badgeClass = esPagado ? "badge-warning" : "badge-success";

        const row = document.createElement("div");
        row.className = "d-flex justify-content-between align-items-center border-bottom py-2";
        row.innerHTML = `
            <div>
                <strong>${escapeHtml(item.placa)}</strong>
                <small class="d-block text-muted">ID #${escapeHtml(item.idRegistro)} • ${escapeHtml(formatDate(item.horaIngreso))} ${escapeHtml(formatTime(item.horaIngreso))}</small>
            </div>
            <span class="badge-custom ${badgeClass}">${escapeHtml(accionTexto)}</span>
        `;
        container.appendChild(row);
    });
}

/**
 * Actualiza la tabla de "Últimos Movimientos" en `dashboard.html` con registros reales de MySQL.
 * Distingue explícitamente que la columna "Tipo" no existe en MySQL y muestra el valor calculado real.
 */
function actualizarTablaMovimientosDashboard(registros) {
    const tbody = document.querySelector("#dashboardPage .table-custom tbody");
    if (!tbody) return;

    tbody.innerHTML = "";
    const lista = Array.isArray(registros) ? registros.slice(0, 8) : [];

    if (lista.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center py-4">
                    No hay movimientos registrados en la tabla <code>registro_parqueo</code> de MySQL.
                </td>
            </tr>
        `;
        return;
    }

    lista.forEach(reg => {
        const metaLocal = consultarMetadatoLocalPorPlaca(reg.placa);
        const tipoTexto = metaLocal && metaLocal.tipoVehiculo
            ? `${metaLocal.tipoVehiculo} (Dato local) | Tarifa BD: ${formatMoney(reg.valorCalculado)}`
            : `N/D en BD | Tarifa BD: ${formatMoney(reg.valorCalculado)}`;

        const estado = String(reg.estadoPago || "").toUpperCase();
        const badgeClass = estado === "PENDIENTE"
            ? "badge-success"
            : estado === "PAGADO"
                ? "badge-warning"
                : "badge-danger";
        const estadoEtiqueta = estado === "PENDIENTE"
            ? "Dentro (PENDIENTE)"
            : estado === "PAGADO"
                ? "Salió (PAGADO)"
                : estado;

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>
                <strong>${escapeHtml(reg.placa)}</strong>
                <small class="d-block text-muted">ID #${escapeHtml(reg.idRegistro)}</small>
            </td>
            <td>${escapeHtml(tipoTexto)}</td>
            <td>${escapeHtml(formatDate(reg.horaIngreso))} ${escapeHtml(formatTime(reg.horaIngreso))}</td>
            <td>
                <span class="badge-custom ${badgeClass}">
                    ${escapeHtml(estadoEtiqueta)}
                </span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/**
 * Muestra en `#chartContainer` un resumen estadístico real de las 3 tablas de MySQL
 * (`registro_parqueo`, `cliente`, `pago`) sin alterar el diseño ni las clases del panel.
 */
function actualizarResumenComportamientoDashboard(conteoResp, registros, pagos, clientes) {
    const chartContainer = document.getElementById("chartContainer");
    if (!chartContainer) return;

    const listaRegistros = Array.isArray(registros) ? registros : [];
    const listaPagos = Array.isArray(pagos) ? pagos : [];
    const listaClientes = Array.isArray(clientes) ? clientes : [];
    const totalRegistros = conteoResp && typeof conteoResp.total === "number"
        ? conteoResp.total
        : listaRegistros.length;

    const pendientes = listaRegistros.filter(r => String(r.estadoPago || "").toUpperCase() === "PENDIENTE").length;
    const pagados = listaRegistros.filter(r => String(r.estadoPago || "").toUpperCase() === "PAGADO").length;
    const pctPagados = totalRegistros > 0 ? Math.round((pagados / totalRegistros) * 100) : 0;

    chartContainer.innerHTML = `
        <div class="w-100 px-3">
            <div class="text-center mb-3">
                <i class="bi bi-bar-chart-line-fill display-5 text-primary"></i>
                <h5 class="mt-2 mb-1">Resumen en Tiempo Real desde MySQL (<code>smart_parking</code>)</h5>
                <small class="text-muted">Datos consultados vía API REST (<code>/api/v1</code>)</small>
            </div>
            <div class="row text-center g-3 mb-3">
                <div class="col-6 col-md-3">
                    <div class="p-2 border rounded bg-light">
                        <small class="d-block text-muted">Total Parqueos</small>
                        <strong class="fs-5 text-primary">${escapeHtml(totalRegistros)}</strong>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="p-2 border rounded bg-light">
                        <small class="d-block text-muted">Pendientes (Dentro)</small>
                        <strong class="fs-5 text-success">${escapeHtml(pendientes)}</strong>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="p-2 border rounded bg-light">
                        <small class="d-block text-muted">Clientes en BD</small>
                        <strong class="fs-5 text-primary">${escapeHtml(listaClientes.length)}</strong>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="p-2 border rounded bg-light">
                        <small class="d-block text-muted">Pagos Registrados</small>
                        <strong class="fs-5 text-warning">${escapeHtml(listaPagos.length)}</strong>
                    </div>
                </div>
            </div>
            <div>
                <div class="d-flex justify-content-between small mb-1">
                    <span>Proporción de registros liquidados (PAGADO: ${escapeHtml(pagados)} de ${escapeHtml(totalRegistros)})</span>
                    <strong>${escapeHtml(pctPagados)}%</strong>
                </div>
                <div class="progress" style="height: 12px;">
                    <div class="progress-bar bg-success" role="progressbar" style="width: ${pctPagados}%"
                         aria-valuenow="${pctPagados}" aria-valuemin="0" aria-valuemax="100"></div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Si el backend no está disponible, muestra claramente el estado de desconexión
 * y evita presentar datos simulados como si provinieran de MySQL.
 */
function manejarDesconexionDashboard(error) {
    renderPageNotification(
        "danger",
        "Backend Spring Boot desconectado",
        error.message || "No se pudo establecer comunicación con http://localhost:8080/api/v1. No se muestran datos simulados."
    );

    actualizarWidgetsSistemaDashboard(null, false);

    setValue("kpiVehiculos", "--");
    setValue("kpiCupos", "--");
    setValue("kpiIngresos", "--");
    setValue("kpiOcupacion", "--");

    actualizarSubtituloKpi("kpiVehiculos", "Sin conexión con GET /api/v1/registros-parqueo");
    actualizarSubtituloKpi("kpiCupos", `Capacidad configurada: ${SmartParking.capacidadConfigurada} cupos (sin conexión a BD)`);
    actualizarSubtituloKpi("kpiIngresos", "Sin conexión con GET /api/v1/pagos");
    actualizarSubtituloKpi("kpiOcupacion", "Sin conexión con la API REST");

    const recentContainer = document.getElementById("recentActivity");
    if (recentContainer) {
        recentContainer.innerHTML = `
            <div class="alert-custom alert-danger mb-0">
                <i class="bi bi-wifi-off"></i>
                <span>Sin conexión con la API REST. Inicie el backend en el puerto 8080 para visualizar la actividad real de MySQL.</span>
            </div>
        `;
    }

    const tbody = document.querySelector("#dashboardPage .table-custom tbody");
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-danger py-4">
                    <i class="bi bi-exclamation-octagon-fill me-2"></i>
                    Servidor desconectado (${escapeHtml(SmartParkingApi.getBaseUrl())}). No se presentan movimientos simulados.
                </td>
            </tr>
        `;
    }
}

/*==========================================================
 8. MÓDULO REGISTRO DE INGRESO (ingreso.html — #formIngreso)
==========================================================*/

let isSubmittingIngreso = false;

function initializeIngreso() {
    const form = document.getElementById("formIngreso");
    if (!form) return;

    const placaInput = document.getElementById("placa");
    const fechaInput = document.getElementById("fechaIngreso");
    const horaInput = document.getElementById("horaIngreso");
    const cupoInput = document.getElementById("cupo");
    const servicioInput = document.getElementById("servicio");

    // Pre-cargar fecha y hora actuales (en hora local) para facilitar el registro sin fechas futuras
    const ahora = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    if (fechaInput && !fechaInput.value) {
        fechaInput.value = `${ahora.getFullYear()}-${pad(ahora.getMonth() + 1)}-${pad(ahora.getDate())}`;
    }
    if (horaInput && !horaInput.value) {
        horaInput.value = `${pad(ahora.getHours())}:${pad(ahora.getMinutes())}`;
    }

    // Normalizar placa en tiempo real y limpiar estados de validación al escribir
    if (placaInput) {
        placaInput.addEventListener("input", () => {
            const pos = placaInput.selectionStart;
            placaInput.value = placaInput.value.toUpperCase();
            if (typeof pos === "number") {
                placaInput.setSelectionRange(pos, pos);
            }
            placaInput.classList.remove("is-invalid");
            placaInput.setCustomValidity("");
            actualizarResumenIngresoUI();
        });
    }

    [fechaInput, horaInput, cupoInput, servicioInput].forEach(el => {
        if (!el) return;
        el.addEventListener("change", () => {
            el.classList.remove("is-invalid");
            el.setCustomValidity("");
            actualizarResumenIngresoUI();
        });
        el.addEventListener("input", () => {
            el.classList.remove("is-invalid");
            el.setCustomValidity("");
            actualizarResumenIngresoUI();
        });
    });

    form.querySelectorAll("input, select, textarea").forEach(ctrl => {
        ctrl.addEventListener("input", () => {
            ctrl.classList.remove("is-invalid");
            if (typeof ctrl.setCustomValidity === "function") {
                ctrl.setCustomValidity("");
            }
        });
    });

    form.addEventListener("reset", () => {
        clearValidation();
        clearPageNotification();
        setTimeout(() => {
            const now = new Date();
            if (fechaInput) {
                fechaInput.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
            }
            if (horaInput) {
                horaInput.value = `${pad(now.getHours())}:${pad(now.getMinutes())}`;
            }
            actualizarResumenIngresoUI("Listo para Registrar", "badge-success");
        }, 10);
    });

    actualizarResumenIngresoUI();
    form.addEventListener("submit", manejarSubmitIngreso);
}

function obtenerTarifaPorServicio(servicio) {
    if (!servicio) return SmartParking.tarifasProyecto.DEFAULT;
    return SmartParking.tarifasProyecto[servicio] ?? SmartParking.tarifasProyecto.DEFAULT;
}

function actualizarResumenIngresoUI(estadoTexto = null, badgeClase = null) {
    const fechaInput = document.getElementById("fechaIngreso");
    const horaInput = document.getElementById("horaIngreso");
    const cupoInput = document.getElementById("cupo");
    const servicioInput = document.getElementById("servicio");

    const fechaVal = fechaInput && fechaInput.value ? fechaInput.value : "--";
    const horaVal = horaInput && horaInput.value ? horaInput.value : "--";
    const cupoVal = cupoInput && cupoInput.value ? `${cupoInput.value} (Local)` : "--";
    const servicioVal = servicioInput && servicioInput.value ? servicioInput.value : "";
    const tarifa = obtenerTarifaPorServicio(servicioVal);

    setValue("resFecha", fechaVal);
    setValue("resHora", horaVal);
    setValue("resCupo", servicioVal ? `${cupoVal} | Tarifa: ${formatMoney(tarifa)}` : cupoVal);

    if (estadoTexto) {
        const resFechaEl = document.getElementById("resFecha");
        const panelResumen = resFechaEl ? resFechaEl.closest(".panel-body") : null;
        const badgeEl = panelResumen ? panelResumen.querySelector(".badge-custom") : null;
        if (badgeEl) {
            badgeEl.className = `badge-custom ${badgeClase || "badge-success"}`;
            badgeEl.textContent = estadoTexto;
        }
    }
}

/**
 * Valida y procesa el formulario `#formIngreso`:
 * 1. Valida placa (`^[A-Z0-9-]{3,10}$`), fecha/hora no futuras (`@PastOrPresent`) y servicio/tarifa.
 * 2. Si se ingresaron datos del propietario (`#propietario`, `#documento`, `#correo`), exige que los
 *    3 campos estén completos y válidos; verifica primero en `GET /api/v1/clientes` si ya existe para
 *    no duplicar, o lo crea con `POST /api/v1/clientes`.
 * 3. Registra el ingreso del vehículo con `POST /api/v1/registros-parqueo`.
 * 4. Informa con precisión el resultado de ambas operaciones y muestra `#modalRegistro` tras confirmar HTTP 201.
 */
async function manejarSubmitIngreso(event) {
    event.preventDefault();

    if (isSubmittingIngreso) {
        return;
    }

    clearValidation();
    clearPageNotification();

    const placaInput = document.getElementById("placa");
    const tipoInput = document.getElementById("tipoVehiculo");
    const colorInput = document.getElementById("color");
    const propietarioInput = document.getElementById("propietario");
    const documentoInput = document.getElementById("documento");
    const telefonoInput = document.getElementById("telefono");
    const correoInput = document.getElementById("correo");
    const fechaInput = document.getElementById("fechaIngreso");
    const horaInput = document.getElementById("horaIngreso");
    const cupoInput = document.getElementById("cupo");
    const servicioInput = document.getElementById("servicio");
    const estadoVehiculoInput = document.getElementById("estadoVehiculo");
    const observacionesInput = document.getElementById("observaciones");
    const submitBtn = document.querySelector("#formIngreso button[type='submit']");

    // 1. Normalizar y validar placa según contrato Java (RegistroParqueoRequest: 3 a 10 caracteres, ^[A-Za-z0-9-]+$)
    const placa = placaInput ? placaInput.value.trim().toUpperCase() : "";
    if (placaInput) placaInput.value = placa;

    const regexPlaca = /^[A-Z0-9-]{3,10}$/;
    if (!placa) {
        showError(placaInput, "La placa es un campo obligatorio.");
        renderPageNotification("danger", "Campos obligatorios incompletos", "Debe ingresar la placa del vehículo.");
        return;
    }
    if (!regexPlaca.test(placa)) {
        showError(placaInput, "La placa debe tener entre 3 y 10 caracteres alfanuméricos o guiones (ej. ABC123).");
        renderPageNotification(
            "danger",
            "Formato de placa inválido",
            "La placa debe contener entre 3 y 10 caracteres (solo letras, números y guiones, ej. ABC123)."
        );
        return;
    }

    // 2. Validar fecha y hora de ingreso (y que no sea futura respecto al momento actual)
    const fechaStr = fechaInput ? fechaInput.value.trim() : "";
    const horaStr = horaInput ? horaInput.value.trim() : "";

    if (!fechaStr) {
        showError(fechaInput, "Seleccione la fecha de ingreso.");
        renderPageNotification("danger", "Fecha requerida", "Debe indicar la fecha de ingreso del vehículo.");
        return;
    }
    if (!horaStr) {
        showError(horaInput, "Seleccione la hora de ingreso.");
        renderPageNotification("danger", "Hora requerida", "Debe indicar la hora de ingreso del vehículo.");
        return;
    }

    const horaPartes = horaStr.split(":");
    const hh = Number(horaPartes[0]);
    const mm = Number(horaPartes[1] || 0);
    const [yyyy, mes, dia] = fechaStr.split("-").map(Number);
    const fechaHoraIngresoDate = new Date(yyyy, mes - 1, dia, hh, mm, 0, 0);
    const ahora = new Date();

    if (Number.isNaN(fechaHoraIngresoDate.getTime())) {
        showError(fechaInput, "Fecha u hora de ingreso inválida.");
        renderPageNotification("danger", "Fecha u hora inválida", "Verifique la fecha y hora de ingreso.");
        return;
    }

    // Si supera el minuto actual, rechazar por restricción @PastOrPresent del backend
    if (fechaHoraIngresoDate.getTime() > ahora.getTime() + 59000) {
        showError(horaInput, "La fecha y hora de ingreso no pueden ser futuras.");
        renderPageNotification(
            "danger",
            "Fecha/hora futura no permitida",
            "El backend exige que la hora de ingreso sea pasada o presente (@PastOrPresent). No se permiten fechas futuras."
        );
        return;
    }

    // Ajustar al instante actual si coincide con el minuto en curso para evitar desfases de segundos con el servidor
    const fechaEfectiva = fechaHoraIngresoDate.getTime() > ahora.getTime()
        ? new Date(ahora.getTime() - 1000)
        : fechaHoraIngresoDate;
    const horaIngresoIso = formatLocalIsoSeconds(fechaEfectiva);

    // 3. Validar servicio y tarifa numérica del proyecto
    const servicioSeleccionado = servicioInput && servicioInput.value ? servicioInput.value.trim() : "Hora";
    const valorCalculado = Number(obtenerTarifaPorServicio(servicioSeleccionado));
    if (Number.isNaN(valorCalculado) || valorCalculado < 0) {
        showError(servicioInput, "La tarifa calculada no es válida.");
        renderPageNotification("danger", "Importe inválido", "El valor calculado debe ser un número mayor o igual a 0.");
        return;
    }

    // 4. Validar datos del propietario (si el usuario diligenció alguno de los campos de propietario)
    const nombresProp = propietarioInput ? propietarioInput.value.trim() : "";
    const numDocProp = documentoInput ? documentoInput.value.trim().toUpperCase() : "";
    const correoProp = correoInput ? correoInput.value.trim().toLowerCase() : "";

    const tieneAlgunDatoPropietario = Boolean(nombresProp || numDocProp || correoProp);
    if (tieneAlgunDatoPropietario) {
        if (!nombresProp || nombresProp.length < 2) {
            showError(propietarioInput, "Ingrese el nombre completo del propietario (mínimo 2 caracteres).");
            renderPageNotification(
                "danger",
                "Datos del propietario incompletos",
                "Para registrar al propietario en MySQL (tabla cliente), debe completar Nombre, Documento y Correo Electrónico. No se registran clientes incompletos."
            );
            return;
        }
        const regexDoc = /^[A-Z0-9-]{3,20}$/;
        if (!numDocProp || !regexDoc.test(numDocProp)) {
            showError(documentoInput, "El documento debe tener entre 3 y 20 caracteres alfanuméricos.");
            renderPageNotification(
                "danger",
                "Documento del propietario inválido",
                "El número de documento es obligatorio para el cliente y debe tener entre 3 y 20 caracteres alfanuméricos."
            );
            return;
        }
        const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!correoProp || !regexEmail.test(correoProp)) {
            showError(correoInput, "Ingrese un correo electrónico válido.");
            renderPageNotification(
                "danger",
                "Correo electrónico inválido",
                "Para registrar al propietario en MySQL es obligatorio un correo electrónico con formato válido."
            );
            return;
        }
    }

    // Bloquear envíos duplicados
    isSubmittingIngreso = true;
    startLoading(submitBtn, "Registrando en MySQL...");

    let resultadoCliente = {
        intentado: tieneAlgunDatoPropietario,
        exito: false,
        yaExistia: false,
        cliente: null,
        mensaje: "No se solicitaron datos de propietario para la tabla cliente."
    };

    try {
        // PASO A: Si se proporcionaron datos completos del propietario, verificar existencia o crear en /api/v1/clientes
        if (tieneAlgunDatoPropietario) {
            const clienteExistente = await SmartParkingApi.buscarClientePorDocumentoOCorreo(numDocProp, correoProp);
            if (clienteExistente) {
                resultadoCliente = {
                    intentado: true,
                    exito: true,
                    yaExistia: true,
                    cliente: clienteExistente,
                    mensaje: `Cliente existente verificado en MySQL (ID #${clienteExistente.idCliente} — ${clienteExistente.tipoDoc} ${clienteExistente.numDoc}). No se duplicó el registro.`
                };
            } else {
                const nuevoClienteReq = {
                    tipoDoc: "CC",
                    numDoc: numDocProp,
                    nombres: nombresProp,
                    correo: correoProp
                };
                const clienteCreado = await SmartParkingApi.crearCliente(nuevoClienteReq);
                resultadoCliente = {
                    intentado: true,
                    exito: true,
                    yaExistia: false,
                    cliente: clienteCreado,
                    mensaje: `Nuevo cliente creado en MySQL (ID #${clienteCreado.idCliente} — ${clienteCreado.tipoDoc} ${clienteCreado.numDoc}).`
                };
            }
        }

        // PASO B: Registrar el ingreso del vehículo en POST /api/v1/registros-parqueo
        const registroRequest = {
            placa,
            horaIngreso: horaIngresoIso,
            valorCalculado,
            estadoPago: "PENDIENTE"
        };

        const registroCreado = await SmartParkingApi.crearRegistro(registroRequest);

        // Guardar atributos complementarios de interfaz en localStorage identificándolos como datos locales
        guardarMetadatoLocalPorPlaca(registroCreado.placa, {
            idRegistroAsociado: registroCreado.idRegistro,
            tipoVehiculo: tipoInput && tipoInput.value ? tipoInput.value : "",
            color: colorInput && colorInput.value ? colorInput.value.trim() : "",
            telefono: telefonoInput && telefonoInput.value ? telefonoInput.value.trim() : "",
            cupo: cupoInput && cupoInput.value ? cupoInput.value : "",
            servicio: servicioSeleccionado,
            estadoVehiculo: estadoVehiculoInput && estadoVehiculoInput.value ? estadoVehiculoInput.value : "",
            observaciones: observacionesInput && observacionesInput.value ? observacionesInput.value.trim() : "",
            numDocPropietario: resultadoCliente.cliente ? resultadoCliente.cliente.numDoc : numDocProp,
            idClienteAsociado: resultadoCliente.cliente ? resultadoCliente.cliente.idCliente : null
        });

        // Actualizar el panel "Resumen del Registro" con los datos confirmados por MySQL
        setValue("resFecha", formatDate(registroCreado.horaIngreso));
        setValue("resHora", formatTime(registroCreado.horaIngreso));
        setValue(
            "resCupo",
            `ID BD #${registroCreado.idRegistro} | ${formatMoney(registroCreado.valorCalculado)} (${registroCreado.estadoPago})`
        );
        actualizarResumenIngresoUI(`Guardado ID #${registroCreado.idRegistro}`, "badge-success");

        const detalleCompleto = resultadoCliente.intentado
            ? `Ingreso registrado en MySQL con ID #${registroCreado.idRegistro} (Placa: ${registroCreado.placa}, Tarifa inicial: ${formatMoney(registroCreado.valorCalculado)}, Estado: ${registroCreado.estadoPago}). ${resultadoCliente.mensaje}`
            : `Ingreso registrado en MySQL con ID #${registroCreado.idRegistro} (Placa: ${registroCreado.placa}, Hora: ${formatDate(registroCreado.horaIngreso)} ${formatTime(registroCreado.horaIngreso)}, Tarifa inicial: ${formatMoney(registroCreado.valorCalculado)}, Estado: ${registroCreado.estadoPago}).`;

        renderPageNotification("success", "Registro confirmado en MySQL (HTTP 201)", detalleCompleto);
        mostrarModalConfirmacionIngreso(registroCreado, resultadoCliente);
    } catch (error) {
        const contextoError = resultadoCliente.intentado && !resultadoCliente.exito
            ? `Error al procesar el registro del propietario en /api/v1/clientes (el ingreso del vehículo NO fue enviado): ${error.message}`
            : resultadoCliente.intentado && resultadoCliente.exito
                ? `Atención: ${resultadoCliente.mensaje} Sin embargo, falló el registro del ingreso en /api/v1/registros-parqueo: ${error.message}`
                : `No se pudo registrar el ingreso en /api/v1/registros-parqueo: ${error.message}`;

        actualizarResumenIngresoUI("Error al Registrar", "badge-danger");
        renderPageNotification("danger", "Error en la operación con el Backend", contextoError);
    } finally {
        isSubmittingIngreso = false;
        stopLoading(submitBtn);
    }
}

function mostrarModalConfirmacionIngreso(registroCreado, resultadoCliente) {
    const modalEl = document.getElementById("modalRegistro");
    if (!modalEl) return;

    const tituloEl = modalEl.querySelector(".modal-title");
    const subtituloEl = modalEl.querySelector(".modal-body h4");
    const parrafoEl = modalEl.querySelector(".modal-body p");

    if (tituloEl) {
        tituloEl.textContent = `Registro Exitoso — ID MySQL #${registroCreado.idRegistro}`;
    }
    if (subtituloEl) {
        subtituloEl.textContent = `Vehículo ${registroCreado.placa} registrado correctamente`;
    }
    if (parrafoEl) {
        const infoCliente = resultadoCliente && resultadoCliente.intentado
            ? `<br><small class="text-muted">${escapeHtml(resultadoCliente.mensaje)}</small>`
            : "";
        parrafoEl.innerHTML = `
            Confirmado en tabla <code>registro_parqueo</code> con <strong>ID #${escapeHtml(registroCreado.idRegistro)}</strong>.<br>
            Fecha/Hora: <strong>${escapeHtml(formatDate(registroCreado.horaIngreso))} ${escapeHtml(formatTime(registroCreado.horaIngreso))}</strong> |
            Valor calculado: <strong>${escapeHtml(formatMoney(registroCreado.valorCalculado))}</strong> |
            Estado: <strong>${escapeHtml(registroCreado.estadoPago)}</strong>
            ${infoCliente}
            <br><a href="consulta.html?placa=${encodeURIComponent(registroCreado.placa)}" class="btn-custom btn-success-custom mt-3" style="padding: 8px 16px; font-size: 0.9rem;">
                <i class="bi bi-search"></i> Verificar en Consulta
            </a>
        `;
    }

    if (window.bootstrap && typeof window.bootstrap.Modal === "function") {
        const modalInstance = window.bootstrap.Modal.getOrCreateInstance(modalEl);
        modalInstance.show();
    }
}

/*==========================================================
 9. MÓDULO CONSULTA Y SALIDA/PAGO (consulta.html)
==========================================================*/

let estadoConsultaActual = {
    registroActual: null,
    historialPlaca: [],
    pagosRegistro: [],
    clienteActual: null,
    procesandoBusqueda: false,
    procesandoSalida: false
};

function initializeConsulta() {
    const buscarPlacaInput = document.getElementById("buscarPlaca");
    const buscarDocumentoInput = document.getElementById("buscarDocumento");
    const placaResultadoInput = document.getElementById("placaResultado");

    if (!buscarPlacaInput && !placaResultadoInput) return;

    // Limpiar los valores estáticos de plantilla ("ABC123", "Juan Pérez", etc.) al iniciar
    // para cumplir la regla de nunca mostrar datos simulados como si fueran de MySQL.
    limpiarCamposResultadoConsulta("Ingrese una placa o documento y presione Buscar");

    // Identificar botones de consulta.html sin alterar el HTML
    const panelBusqueda = buscarPlacaInput ? buscarPlacaInput.closest(".panel-body") : null;
    const btnBuscar = panelBusqueda ? panelBusqueda.querySelector("button.btn-primary-custom") : null;

    const botonesAccion = document.querySelectorAll(".page-content .panel:last-of-type button");
    let btnEditar = null;
    let btnImprimir = null;
    let btnRegistrarSalida = null;

    botonesAccion.forEach(btn => {
        const txt = btn.textContent.trim().toLowerCase();
        if (txt.includes("registrar salida")) {
            btnRegistrarSalida = btn;
        } else if (txt.includes("imprimir")) {
            btnImprimir = btn;
        } else if (txt.includes("editar")) {
            btnEditar = btn;
        }
    });

    if (buscarPlacaInput) {
        buscarPlacaInput.addEventListener("input", () => {
            buscarPlacaInput.value = buscarPlacaInput.value.toUpperCase();
            buscarPlacaInput.classList.remove("is-invalid");
        });
        buscarPlacaInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.isComposing) {
                e.preventDefault();
                ejecutarBusquedaConsulta(btnBuscar, btnRegistrarSalida);
            }
        });
    }

    if (buscarDocumentoInput) {
        buscarDocumentoInput.addEventListener("input", () => {
            buscarDocumentoInput.classList.remove("is-invalid");
        });
        buscarDocumentoInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.isComposing) {
                e.preventDefault();
                ejecutarBusquedaConsulta(btnBuscar, btnRegistrarSalida);
            }
        });
    }

    if (btnBuscar) {
        btnBuscar.addEventListener("click", () => {
            ejecutarBusquedaConsulta(btnBuscar, btnRegistrarSalida);
        });
    }

    if (btnRegistrarSalida) {
        btnRegistrarSalida.disabled = true;
        btnRegistrarSalida.addEventListener("click", () => {
            ejecutarSalidaYPago(btnRegistrarSalida);
        });
    }

    if (btnImprimir) {
        btnImprimir.addEventListener("click", () => {
            window.print();
        });
    }

    if (btnEditar) {
        btnEditar.addEventListener("click", () => {
            if (!estadoConsultaActual.registroActual) {
                renderPageNotification("info", "Sin registro seleccionado", "Busque primero un vehículo por placa para consultar su estado en MySQL.");
                return;
            }
            renderPageNotification(
                "info",
                "Información de edición",
                `El registro #${estadoConsultaActual.registroActual.idRegistro} (${estadoConsultaActual.registroActual.placa}) tiene estado ${estadoConsultaActual.registroActual.estadoPago}. Para liquidar y cambiar su estado a PAGADO utilice el botón 'Registrar Salida'.`
            );
        });
    }

    // Si la URL incluye ?placa=... o ?numDoc=..., ejecutar la consulta automáticamente
    const params = new URLSearchParams(window.location.search);
    const placaUrl = params.get("placa");
    const docUrl = params.get("numDoc");
    if (placaUrl && buscarPlacaInput) {
        buscarPlacaInput.value = placaUrl.trim().toUpperCase();
    }
    if (docUrl && buscarDocumentoInput) {
        buscarDocumentoInput.value = docUrl.trim();
    }
    if (placaUrl || docUrl) {
        ejecutarBusquedaConsulta(btnBuscar, btnRegistrarSalida);
    }
}

function limpiarCamposResultadoConsulta(mensajeTabla = "Realice una búsqueda por placa o documento para consultar MySQL.") {
    estadoConsultaActual.registroActual = null;
    estadoConsultaActual.historialPlaca = [];
    estadoConsultaActual.pagosRegistro = [];
    estadoConsultaActual.clienteActual = null;

    setValue("placaResultado", "--");
    setValue("tipoResultado", "--");
    setValue("colorResultado", "--");
    setValue("estadoResultado", "--");
    setValue("nombreResultado", "--");
    setValue("documentoResultado", "--");
    setValue("telefonoResultado", "--");
    setValue("cupoResultado", "--");
    setValue("fechaResultado", "--");
    setValue("horaResultado", "--");
    setValue("servicioResultado", "--");
    setValue("tiempoResultado", "--");
    setValue("valorResultado", "--");
    setValue("pagoResultado", "--");

    const tbody = document.querySelector(".page-content .table-custom tbody");
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-muted py-4">
                    ${escapeHtml(mensajeTabla)}
                </td>
            </tr>
        `;
    }
}

/**
 * Ejecuta la búsqueda en `consulta.html`:
 * - Por placa: `GET /api/v1/registros-parqueo?placa=...` y `GET /api/v1/pagos/registro/{idRegistro}`
 * - Por documento: `GET /api/v1/clientes?numDoc=...`
 */
async function ejecutarBusquedaConsulta(btnBuscar, btnRegistrarSalida) {
    if (estadoConsultaActual.procesandoBusqueda) return;

    clearValidation();
    clearPageNotification();

    const buscarPlacaInput = document.getElementById("buscarPlaca");
    const buscarDocumentoInput = document.getElementById("buscarDocumento");

    const placaRaw = buscarPlacaInput ? buscarPlacaInput.value.trim().toUpperCase() : "";
    const docRaw = buscarDocumentoInput ? buscarDocumentoInput.value.trim().toUpperCase() : "";

    if (buscarPlacaInput) buscarPlacaInput.value = placaRaw;

    if (!placaRaw && !docRaw) {
        if (buscarPlacaInput) showError(buscarPlacaInput);
        renderPageNotification(
            "warning",
            "Criterio de búsqueda requerido",
            "Ingrese una placa (ej. ABC123) y/o un número de documento para consultar en MySQL."
        );
        return;
    }

    if (placaRaw && !/^[A-Z0-9-]{3,10}$/.test(placaRaw)) {
        showError(buscarPlacaInput);
        renderPageNotification(
            "danger",
            "Placa inválida",
            "La placa a consultar debe tener entre 3 y 10 caracteres alfanuméricos o guiones."
        );
        return;
    }

    estadoConsultaActual.procesandoBusqueda = true;
    startLoading(btnBuscar, "Buscando...");

    try {
        let registrosEncontrados = [];
        let clienteEncontrado = null;
        let pagosDelRegistro = [];

        // 1. Si se ingresó placa, consultar GET /api/v1/registros-parqueo?placa=...
        if (placaRaw) {
            const respRegistros = await SmartParkingApi.listarRegistros(placaRaw);
            registrosEncontrados = Array.isArray(respRegistros) ? respRegistros : [];
        }

        // 2. Si se ingresó documento, consultar GET /api/v1/clientes?numDoc=...
        if (docRaw) {
            clienteEncontrado = await SmartParkingApi.buscarClientePorDocumentoOCorreo(docRaw);
        }

        // 3. Si se encontró registro por placa y no se buscó documento explícito, verificar si hay metadato local que asocie el documento de un cliente real en MySQL
        const registroPrincipal = seleccionarRegistroPrincipal(registrosEncontrados);
        if (registroPrincipal) {
            try {
                const respPagos = await SmartParkingApi.consultarPagosPorRegistro(registroPrincipal.idRegistro);
                pagosDelRegistro = Array.isArray(respPagos) ? respPagos : [];
            } catch (errPagos) {
                if (errPagos.status !== 404) {
                    throw errPagos;
                }
                pagosDelRegistro = [];
            }

            if (!docRaw) {
                const meta = consultarMetadatoLocalPorPlaca(registroPrincipal.placa);
                if (meta && meta.numDocPropietario) {
                    try {
                        clienteEncontrado = await SmartParkingApi.buscarClientePorDocumentoOCorreo(meta.numDocPropietario);
                    } catch (_e) {
                        clienteEncontrado = null;
                    }
                }
            }
        }

        // Evaluar resultados inexistentes
        if (placaRaw && !registroPrincipal && (!docRaw || !clienteEncontrado)) {
            limpiarCamposResultadoConsulta(`No se encontraron registros en MySQL para la placa ${placaRaw}.`);
            if (btnRegistrarSalida) btnRegistrarSalida.disabled = true;
            renderPageNotification(
                "warning",
                "Sin resultados en MySQL",
                `No existe ningún registro de parqueo con la placa "${placaRaw}"${docRaw ? ` ni cliente con documento "${docRaw}"` : ""}.`
            );
            return;
        }

        if (!placaRaw && docRaw && !clienteEncontrado) {
            limpiarCamposResultadoConsulta(`No se encontró ningún cliente con el documento ${docRaw} en MySQL.`);
            if (btnRegistrarSalida) btnRegistrarSalida.disabled = true;
            renderPageNotification(
                "warning",
                "Cliente no encontrado",
                `No existe ningún cliente registrado en la tabla cliente de MySQL con el documento "${docRaw}".`
            );
            return;
        }

        estadoConsultaActual.registroActual = registroPrincipal;
        estadoConsultaActual.historialPlaca = registrosEncontrados;
        estadoConsultaActual.pagosRegistro = pagosDelRegistro;
        estadoConsultaActual.clienteActual = clienteEncontrado;

        renderizarResultadosConsulta(registroPrincipal, registrosEncontrados, pagosDelRegistro, clienteEncontrado, btnRegistrarSalida);

        // Notificación clara del resultado
        if (registroPrincipal && clienteEncontrado) {
            renderPageNotification(
                "success",
                "Consulta exitosa en MySQL",
                `Vehículo ${registroPrincipal.placa} (Registro ID #${registroPrincipal.idRegistro}, Estado: ${registroPrincipal.estadoPago}) y Cliente ${clienteEncontrado.nombres} (${clienteEncontrado.tipoDoc} ${clienteEncontrado.numDoc}) cargados desde MySQL.`
            );
        } else if (registroPrincipal) {
            const avisoDoc = docRaw && !clienteEncontrado
                ? ` Nota: No se encontró un cliente con el documento "${docRaw}".`
                : "";
            renderPageNotification(
                "success",
                "Registro de parqueo encontrado en MySQL",
                `Se cargó el registro ID #${registroPrincipal.idRegistro} para la placa ${registroPrincipal.placa} (Estado: ${registroPrincipal.estadoPago}, Valor calculado: ${formatMoney(registroPrincipal.valorCalculado)}).${avisoDoc}`
            );
        } else if (clienteEncontrado) {
            renderPageNotification(
                "info",
                "Cliente encontrado en MySQL",
                `Cliente verificado en tabla cliente: ${clienteEncontrado.nombres} (ID #${clienteEncontrado.idCliente}, ${clienteEncontrado.tipoDoc} ${clienteEncontrado.numDoc}, Correo: ${clienteEncontrado.correo}). Ingrese una placa para consultar un vehículo específico.`
            );
        }
    } catch (error) {
        limpiarCamposResultadoConsulta("Error de comunicación con la API REST.");
        if (btnRegistrarSalida) btnRegistrarSalida.disabled = true;
        renderPageNotification(
            "danger",
            "Error al consultar la API REST",
            error.message || "No fue posible realizar la consulta en el backend."
        );
    } finally {
        estadoConsultaActual.procesandoBusqueda = false;
        stopLoading(btnBuscar);
    }
}

/**
 * Prioriza el registro más reciente con estado PENDIENTE; si todos están pagados,
 * selecciona el más reciente del historial devuelto por MySQL.
 */
function seleccionarRegistroPrincipal(registros) {
    if (!Array.isArray(registros) || registros.length === 0) return null;
    const pendiente = registros.find(r => String(r.estadoPago || "").toUpperCase() === "PENDIENTE");
    return pendiente || registros[0];
}

function renderizarResultadosConsulta(registro, historial, pagos, cliente, btnRegistrarSalida) {
    // 1. Poblar datos del propietario (exclusivamente si existe un ClienteResponse real o indicar sin datos en BD)
    if (cliente) {
        setValue("nombreResultado", `${cliente.nombres} (MySQL ID #${cliente.idCliente} | ${cliente.correo})`);
        setValue("documentoResultado", `${cliente.tipoDoc} ${cliente.numDoc} (MySQL)`);
    } else {
        setValue("nombreResultado", "No registrado / Sin vínculo en BD");
        setValue("documentoResultado", "No registrado en consulta");
    }

    // 2. Poblar datos del vehículo y del parqueo
    if (!registro) {
        setValue("placaResultado", "Sin placa consultada");
        setValue("tipoResultado", "N/D en BD");
        setValue("colorResultado", "N/D en BD");
        setValue("estadoResultado", "Ingrese placa para consultar");
        setValue("telefonoResultado", "N/D en BD");
        setValue("cupoResultado", "N/D en BD");
        setValue("fechaResultado", "--");
        setValue("horaResultado", "--");
        setValue("servicioResultado", "N/D en BD");
        setValue("tiempoResultado", "--");
        setValue("valorResultado", "--");
        setValue("pagoResultado", "--");
        if (btnRegistrarSalida) btnRegistrarSalida.disabled = true;
        return;
    }

    const meta = consultarMetadatoLocalPorPlaca(registro.placa);
    const estadoUpper = String(registro.estadoPago || "").toUpperCase();
    const tienePagoConfirmado = Array.isArray(pagos) && pagos.some(p => {
        const est = String(p.estadoPago || "").toUpperCase();
        return est === "PAGADO" || est === "APROBADO";
    });

    const ultimoPago = Array.isArray(pagos) && pagos.length > 0 ? pagos[0] : null;
    const fechaFinCalculo = ultimoPago && ultimoPago.fechaPago
        ? parseLocalDateTime(ultimoPago.fechaPago)
        : new Date();

    setValue("placaResultado", `${registro.placa} (ID #${registro.idRegistro})`);
    setValue("tipoResultado", meta && meta.tipoVehiculo ? `${meta.tipoVehiculo} (Dato local)` : "No aplica en BD");
    setValue("colorResultado", meta && meta.color ? `${meta.color} (Dato local)` : "No aplica en BD");
    setValue(
        "estadoResultado",
        estadoUpper === "PENDIENTE"
            ? "Dentro del Parqueadero (PENDIENTE)"
            : `Salida Registrada (${estadoUpper})`
    );
    setValue("telefonoResultado", meta && meta.telefono ? `${meta.telefono} (Dato local)` : "No aplica en BD");
    setValue("cupoResultado", meta && meta.cupo ? `${meta.cupo} (Dato local)` : "No aplica en BD");
    setValue("fechaResultado", formatDate(registro.horaIngreso));
    setValue("horaResultado", formatTime(registro.horaIngreso));
    setValue("servicioResultado", meta && meta.servicio ? `${meta.servicio} (Dato local)` : "Tarifa General (BD)");
    setValue("tiempoResultado", calcularTiempoTranscurridoTexto(registro.horaIngreso, fechaFinCalculo));
    setValue("valorResultado", formatMoney(registro.valorCalculado));

    if (estadoUpper === "PAGADO" && ultimoPago) {
        setValue("pagoResultado", `PAGADO (Pago #${ultimoPago.idPago}: ${formatMoney(ultimoPago.valorPagado)})`);
    } else if (estadoUpper !== "PAGADO" && tienePagoConfirmado && ultimoPago) {
        setValue("pagoResultado", `PAGO #${ultimoPago.idPago} CREADO — SINCRONIZAR ESTADO`);
    } else {
        setValue("pagoResultado", estadoUpper);
    }

    // Habilitar o deshabilitar botón "Registrar Salida"
    if (btnRegistrarSalida) {
        if (estadoUpper === "PAGADO") {
            btnRegistrarSalida.disabled = true;
            btnRegistrarSalida.innerHTML = `<i class="bi bi-check2-all"></i> Salida ya Registrada (PAGADO)`;
        } else if (tienePagoConfirmado) {
            btnRegistrarSalida.disabled = false;
            btnRegistrarSalida.innerHTML = `<i class="bi bi-arrow-repeat"></i> Sincronizar Estado a PAGADO`;
        } else {
            btnRegistrarSalida.disabled = false;
            btnRegistrarSalida.innerHTML = `<i class="bi bi-box-arrow-right"></i> Registrar Salida y Pago (${formatMoney(registro.valorCalculado)})`;
        }
    }

    renderizarTablaHistorialConsulta(registro, historial, pagos, meta);
}

function renderizarTablaHistorialConsulta(registroPrincipal, historial, pagos, metaLocal) {
    const tbody = document.querySelector(".page-content .table-custom tbody");
    if (!tbody) return;

    tbody.innerHTML = "";
    const cupoTexto = metaLocal && metaLocal.cupo ? `${metaLocal.cupo} (Local)` : "N/D en BD";

    // Mostrar pagos asociados al registro principal (desde GET /api/v1/pagos/registro/{id})
    if (Array.isArray(pagos) && pagos.length > 0) {
        pagos.forEach(p => {
            const trPago = document.createElement("tr");
            trPago.innerHTML = `
                <td>${escapeHtml(formatDate(p.fechaPago))}</td>
                <td>${escapeHtml(formatTime(p.fechaPago))}</td>
                <td>Pago / Salida (ID Pago #${escapeHtml(p.idPago)} — ${escapeHtml(formatMoney(p.valorPagado))})</td>
                <td>Límite: ${escapeHtml(p.tiempoLimiteSalida)} min</td>
                <td>
                    <span class="badge-custom badge-success">
                        ${escapeHtml(p.estadoPago)}
                    </span>
                </td>
            `;
            tbody.appendChild(trPago);
        });
    }

    // Mostrar historial de registros de parqueo para la placa en MySQL
    const listaHistorial = Array.isArray(historial) && historial.length > 0
        ? historial
        : registroPrincipal ? [registroPrincipal] : [];

    listaHistorial.forEach(item => {
        const est = String(item.estadoPago || "").toUpperCase();
        const badgeClass = est === "PENDIENTE" ? "badge-warning" : "badge-success";
        const label = est === "PENDIENTE" ? "Activo (PENDIENTE)" : `Finalizado (${est})`;

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${escapeHtml(formatDate(item.horaIngreso))}</td>
            <td>${escapeHtml(formatTime(item.horaIngreso))}</td>
            <td>Ingreso (ID Registro #${escapeHtml(item.idRegistro)} — ${escapeHtml(formatMoney(item.valorCalculado))})</td>
            <td>${escapeHtml(cupoTexto)}</td>
            <td>
                <span class="badge-custom ${badgeClass}">
                    ${escapeHtml(label)}
                </span>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/*==========================================================
 10. SECUENCIA SEGURA DE SALIDA Y PAGO (Sección 4.4)
==========================================================*/

/**
 * Ejecuta la secuencia segura de 8 pasos para registrar la salida y el pago:
 * 1. Comprobar que el registro existe en MySQL (`GET /api/v1/registros-parqueo/{id}`).
 * 2. Comprobar que no esté ya en estado `PAGADO` y verificar pagos previos (`GET /api/v1/pagos/registro/{id}`).
 * 3. Calcular y validar el importe según las reglas reales del proyecto (`valorCalculado` > 0).
 * 4. Registrar el pago (`POST /api/v1/pagos`) solo si no existe ya un pago aprobado para ese `idRegistro`.
 * 5. Comprobar que el pago fue creado correctamente (HTTP 201 y `idPago`).
 * 6. Actualizar el registro de parqueo a `PAGADO` (`PUT /api/v1/registros-parqueo/{id}`).
 * 7. Comprobar la respuesta de actualización (HTTP 200). Si falla tras haber creado el pago,
 *    informar claramente que el pago quedó registrado pero la actualización del estado quedó pendiente.
 * 8. Actualizar la interfaz solo después de confirmar las operaciones en MySQL.
 */
async function ejecutarSalidaYPago(btnRegistrarSalida) {
    if (estadoConsultaActual.procesandoSalida) return;

    const regSeleccionado = estadoConsultaActual.registroActual;
    if (!regSeleccionado || !regSeleccionado.idRegistro) {
        renderPageNotification(
            "warning",
            "Ningún registro activo seleccionado",
            "Primero busque un vehículo por placa antes de registrar su salida."
        );
        return;
    }

    clearPageNotification();
    estadoConsultaActual.procesandoSalida = true;
    startLoading(btnRegistrarSalida, "Procesando salida y pago...");

    let pagoConfirmado = null;

    try {
        // PASO 1: Comprobar en MySQL que el registro existe y obtener su estado actual real
        const registroActualBD = await SmartParkingApi.consultarRegistroPorId(regSeleccionado.idRegistro);
        if (!registroActualBD || !registroActualBD.idRegistro) {
            throw new Error(`El registro de parqueo #${regSeleccionado.idRegistro} no existe en MySQL.`);
        }

        // PASO 2: Comprobar que no esté ya pagado y verificar si ya tiene un pago en la tabla `pago`
        const estadoActual = String(registroActualBD.estadoPago || "").toUpperCase();
        let pagosExistentes = [];
        try {
            const respPagos = await SmartParkingApi.consultarPagosPorRegistro(registroActualBD.idRegistro);
            pagosExistentes = Array.isArray(respPagos) ? respPagos : [];
        } catch (errPagos) {
            if (errPagos.status !== 404) throw errPagos;
        }

        if (estadoActual === "PAGADO") {
            estadoConsultaActual.registroActual = registroActualBD;
            estadoConsultaActual.pagosRegistro = pagosExistentes;
            renderizarResultadosConsulta(
                registroActualBD,
                estadoConsultaActual.historialPlaca,
                pagosExistentes,
                estadoConsultaActual.clienteActual,
                btnRegistrarSalida
            );
            renderPageNotification(
                "info",
                "El registro ya se encuentra PAGADO",
                `El registro #${registroActualBD.idRegistro} (Placa ${registroActualBD.placa}) ya figura como PAGADO en MySQL. No se generó un cobro duplicado.`
            );
            return;
        }

        // PASO 3: Calcular y validar el importe según las reglas reales del proyecto
        let montoLiquidado = Number(registroActualBD.valorCalculado);
        if (Number.isNaN(montoLiquidado) || montoLiquidado <= 0) {
            montoLiquidado = SmartParking.tarifasProyecto.DEFAULT;
        }

        // Verificar si ya existe un pago previo (para evitar pagos duplicados en caso de reintento de consistencia)
        const pagoPrevioValido = pagosExistentes.find(p => {
            const est = String(p.estadoPago || "").toUpperCase();
            return est === "PAGADO" || est === "APROBADO";
        });

        if (pagoPrevioValido) {
            pagoConfirmado = pagoPrevioValido;
        } else {
            // PASO 4: Registrar el pago en POST /api/v1/pagos
            const pagoRequest = {
                idRegistro: registroActualBD.idRegistro,
                valorPagado: montoLiquidado,
                estadoPago: "PAGADO",
                tiempoLimiteSalida: 15
            };
            pagoConfirmado = await SmartParkingApi.registrarPago(pagoRequest);

            // PASO 5: Comprobar que el pago fue creado correctamente
            if (!pagoConfirmado || !pagoConfirmado.idPago) {
                throw new Error("El backend no devolvió la confirmación de creación del pago (idPago ausente).");
            }
        }

        // PASO 6: Actualizar el registro de parqueo a PAGADO (PUT /api/v1/registros-parqueo/{id})
        let registroActualizado = null;
        try {
            const putRequest = {
                placa: registroActualBD.placa,
                horaIngreso: registroActualBD.horaIngreso,
                valorCalculado: montoLiquidado,
                estadoPago: "PAGADO"
            };
            registroActualizado = await SmartParkingApi.actualizarRegistro(registroActualBD.idRegistro, putRequest);
        } catch (putError) {
            // PROTECCIÓN DE CONSISTENCIA: El pago se registró pero PUT falló
            const pagosActualizados = await SmartParkingApi.consultarPagosPorRegistro(registroActualBD.idRegistro).catch(() => [pagoConfirmado]);
            estadoConsultaActual.pagosRegistro = pagosActualizados;
            renderizarResultadosConsulta(
                registroActualBD,
                estadoConsultaActual.historialPlaca,
                pagosActualizados,
                estadoConsultaActual.clienteActual,
                btnRegistrarSalida
            );
            renderPageNotification(
                "warning",
                "Pago registrado, pero actualización de estado pendiente",
                `El pago fue registrado exitosamente en MySQL con ID Pago #${pagoConfirmado.idPago} (${formatMoney(pagoConfirmado.valorPagado)}), pero falló la actualización del estado del registro #${registroActualBD.idRegistro} a PAGADO (${putError.message}). Presione 'Sincronizar Estado a PAGADO' para completar la actualización sin duplicar el pago.`
            );
            return;
        }

        // PASO 7: Comprobar la respuesta de actualización
        if (!registroActualizado || String(registroActualizado.estadoPago || "").toUpperCase() !== "PAGADO") {
            throw new Error("El registro fue actualizado pero el estado devuelto no corresponde a PAGADO.");
        }

        // PASO 8: Actualizar la interfaz tras confirmar ambas operaciones en MySQL
        const [historialFresco, pagosFrescos] = await Promise.all([
            SmartParkingApi.listarRegistros(registroActualizado.placa),
            SmartParkingApi.consultarPagosPorRegistro(registroActualizado.idRegistro)
        ]);

        estadoConsultaActual.registroActual = registroActualizado;
        estadoConsultaActual.historialPlaca = Array.isArray(historialFresco) ? historialFresco : [registroActualizado];
        estadoConsultaActual.pagosRegistro = Array.isArray(pagosFrescos) ? pagosFrescos : [pagoConfirmado];

        renderizarResultadosConsulta(
            registroActualizado,
            estadoConsultaActual.historialPlaca,
            estadoConsultaActual.pagosRegistro,
            estadoConsultaActual.clienteActual,
            btnRegistrarSalida
        );

        renderPageNotification(
            "success",
            "Salida y pago completados en MySQL",
            `Operación confirmada: Pago ID #${pagoConfirmado.idPago} por ${formatMoney(pagoConfirmado.valorPagado)} (tiempo límite de salida: ${pagoConfirmado.tiempoLimiteSalida} min) y Registro #${registroActualizado.idRegistro} (${registroActualizado.placa}) actualizado a estado ${registroActualizado.estadoPago}.`
        );
    } catch (error) {
        renderPageNotification(
            "danger",
            "Error al procesar la salida y el pago",
            error.message || "No se pudo completar el proceso de salida en el servidor."
        );
    } finally {
        estadoConsultaActual.procesandoSalida = false;
        stopLoading(btnRegistrarSalida);
        // Re-sincronizar etiqueta y estado del botón según el estado final
        if (estadoConsultaActual.registroActual) {
            renderizarResultadosConsulta(
                estadoConsultaActual.registroActual,
                estadoConsultaActual.historialPlaca,
                estadoConsultaActual.pagosRegistro,
                estadoConsultaActual.clienteActual,
                btnRegistrarSalida
            );
        }
    }
}

/*==========================================================
 11. NAVEGACIÓN Y MENSAJES GLOBALES
==========================================================*/

function goToIngreso() {
    window.location.href = "ingreso.html";
}

function goToConsulta() {
    window.location.href = "consulta.html";
}

function goToReportes() {
    window.location.href = "reportes.html";
}

function goToConfiguracion() {
    window.location.href = "configuracion.html";
}

function showMessage(message) {
    renderPageNotification("info", SmartParking.appName, message);
}
