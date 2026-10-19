/*==========================================================
 SMART PARKING
 Sistema de Gestión de Parqueaderos
 Proyecto ADSO - SENA

 Archivo:
 script.js

 Autores:
 Autores: grupo 6 ficha 3336134

 Descripción:
 Control general del Front-End.

==========================================================*/

"use strict";

/*==========================================================
CONFIGURACIÓN GLOBAL
==========================================================*/

const SmartParking = {

    version: "2.0.0",

    appName: "SMART PARKING",

    loginDelay: 1500

};



/*==========================================================
INICIALIZACIÓN
==========================================================*/

document.addEventListener("DOMContentLoaded", () => {

    initializeApp();

});



function initializeApp() {

    initializeLogin();

    initializeDashboard();

}



/*==========================================================
LOGIN
==========================================================*/

function initializeLogin() {

    const loginForm = document.getElementById("loginForm");

    if (!loginForm) return;

    initializePassword();

    initializeRememberMe();

    loginForm.addEventListener("submit", loginSubmit);

}



/*==========================================================
MOSTRAR / OCULTAR CONTRASEÑA
==========================================================*/

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



/*==========================================================
RECORDAR USUARIO
==========================================================*/

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



/*==========================================================
ENVÍO DEL LOGIN
==========================================================*/

function loginSubmit(event) {

    event.preventDefault();

    const usuario = document.getElementById("usuario")

    const password = document.getElementById("password");

    clearValidation();

    let valido = true;

    if (usuario.value.trim() === "") {

        showError(usuario);

        valido = false;

    }

    if (password.value.trim() === "") {

        showError(password);

        valido = false;

    }

    if (!valido) return;

    const boton = document.querySelector("#loginForm button[type='submit']");

    boton.disabled = true;

    boton.innerHTML = `
        <span class="spinner-border spinner-border-sm me-2"></span>
        Ingresando...
    `;

    setTimeout(() => {

        window.location.href = "dashboard.html";

    }, 1000);

}



/*==========================================================
VALIDAR FORMULARIO
==========================================================*/

function validateLogin(usuario, password) {

    let valid = true;

    if (usuario.value.trim() === "") {

        showError(usuario);

        valid = false;

    }

    if (password.value.trim() === "") {

        showError(password);

        valid = false;

    }

    return valid;

}



/*==========================================================
AUTENTICACIÓN
==========================================================*/

function authenticate(usuario, password) {

    setTimeout(() => {

        stopLoading();

        window.location.href = "dashboard.html";

    }, SmartParking.loginDelay);

}



/*==========================================================
LOADING BOTÓN
==========================================================*/

let currentButton = null;

let originalButton = "";



function startLoading(button) {

    currentButton = button;

    originalButton = button.innerHTML;

    button.disabled = true;

    button.innerHTML =

        '<span class="spinner-border spinner-border-sm me-2"></span>Ingresando...';

}



function stopLoading() {

    if (!currentButton) return;

    currentButton.disabled = false;

    currentButton.innerHTML = originalButton;

}



/*==========================================================
VALIDACIONES VISUALES
==========================================================*/

function showError(input) {

    input.classList.add("is-invalid");

    input.focus();

}



function clearValidation() {

    document

        .querySelectorAll(".is-invalid")

        .forEach(item => {

            item.classList.remove("is-invalid");

        });

}



/*==========================================================
UTILIDADES
==========================================================*/

function currentDate() {

    return new Date();

}



function formatMoney(value) {

    return new Intl.NumberFormat(

        "es-CO",

        {

            style: "currency",

            currency: "COP"

        }

    ).format(value);

}



function formatDate(date) {

    return date.toLocaleDateString(

        "es-CO"

    );

}



function formatTime(date) {

    return date.toLocaleTimeString(

        "es-CO"

    );

}
/*==========================================================
DASHBOARD
==========================================================*/

function initializeDashboard() {

    const dashboard = document.getElementById("dashboardPage");

    if (!dashboard) return;

    initializeClock();

    loadDashboard();

}



/*==========================================================
CARGAR DASHBOARD
==========================================================*/

function loadDashboard() {

    loadIndicators();

    loadRecentActivity();

}



/*==========================================================
RELOJ
==========================================================*/

let clockInterval = null;



function initializeClock() {

    updateClock();

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



/*==========================================================
INDICADORES
==========================================================*/

const dashboardData = {

    vehiculosHoy: 32,

    cuposDisponibles: 118,

    ingresosHoy: 425000,

    ocupacion: 27

};



function loadIndicators() {

    setValue("kpiVehiculos", dashboardData.vehiculosHoy);

    setValue("kpiCupos", dashboardData.cuposDisponibles);

    setValue(

        "kpiIngresos",

        formatMoney(dashboardData.ingresosHoy)

    );

    setValue(

        "kpiOcupacion",

        dashboardData.ocupacion + "%"

    );

}



/*==========================================================
ACTIVIDAD RECIENTE
==========================================================*/

const recentActivity = [

    {

        placa: "ABC123",

        accion: "Ingreso"

    },

    {

        placa: "XYZ987",

        accion: "Salida"

    },

    {

        placa: "JKL456",

        accion: "Ingreso"

    },

    {

        placa: "MNO741",

        accion: "Ingreso"

    }

];



function loadRecentActivity() {

    const container = document.getElementById("recentActivity");

    if (!container) return;

    container.innerHTML = "";

    recentActivity.forEach(item => {

        const row = document.createElement("div");

        row.className = "d-flex justify-content-between align-items-center border-bottom py-2";

        row.innerHTML = `

            <strong>${item.placa}</strong>

            <span>${item.accion}</span>

        `;

        container.appendChild(row);

    });

}



/*==========================================================
UTILIDAD
==========================================================*/

function setValue(id, value) {

    const element = document.getElementById(id);

    if (!element) return;

    element.textContent = value;

}



/*==========================================================
NAVEGACIÓN
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



/*==========================================================
MENSAJE
==========================================================*/

function showMessage(message) {

    alert(message);

}