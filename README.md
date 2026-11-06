# SMART PARKING — Sistema de Gestión de Parqueaderos

* **Proyecto Permanente:** `SMART PARKING — Sistema de Gestión de Parqueaderos`
* **Evidencia Académica Actual:** `GA7-220501096-AA3-EV01 — Codificación de módulos del software stand-alone, web y móvil`
* **Programa de Formación:** Tecnología en Análisis y Desarrollo de Software (ADSO) — Ficha 3336134 | Grupo 6
* **Repositorio Permanente en GitHub:** [https://github.com/jotacabrejo-tech/SMART_PARKING_PROYECTO](https://github.com/jotacabrejo-tech/SMART_PARKING_PROYECTO)

---

## 1. Arquitectura General del Sistema

**Smart Parking** es un sistema integral de software para la administración de parqueaderos que automatiza el registro de ingreso y salida de vehículos, la gestión de clientes/propietarios y el control de recaudos y pagos liquidados sobre la base de datos relacional **MySQL (`smart_parking`)**.

El proyecto integra los siguientes módulos codificados que comparten el mismo esquema de datos en MySQL (nota: el proyecto **no incluye una aplicación móvil nativa**; la experiencia móvil y de escritorio se proporciona mediante el **módulo frontend web responsivo**, acompañado del módulo **stand-alone** de consola y los módulos **web** en Spring Boot y Jakarta Servlets/JSP):

1. **Módulo Stand-Alone (Consola Java + JDBC):** Ejecutable desde [`backend/src/main/java/com/sena/smartparking/Main.java`](backend/src/main/java/com/sena/smartparking/Main.java) para verificar la conexión JDBC y el ciclo CRUD controlado sobre `registro_parqueo`.
2. **Módulo Backend API RESTful (Spring Boot 3 + JDBC):** Ubicado en [`backend/`](backend/README.md), expone los servicios REST en `http://localhost:8080/api/v1` con arquitectura en capas (`Controller` → `Service` → `DAO JDBC` → `MySQL`), validaciones `@Valid`, manejo global de excepciones (`GlobalExceptionHandler`) y documentación OpenAPI 3 / Swagger UI.
3. **Módulo Frontend Web Responsivo (HTML5 + CSS3 + Bootstrap 5 + Fetch API):** Ubicado en la raíz del proyecto (`login.html`, `dashboard.html`, `ingreso.html`, `consulta.html`, `css/style.css`, `js/script.js`), con diseño web responsivo adaptable a pantallas de escritorio y dispositivos móviles, integrado con la API REST de Spring Boot mediante el cliente centralizado `SmartParkingApi` (`fetch()`).
4. **Módulo Web Java EE / Jakarta EE (`web-servlets/`):** Aplicación web independiente empaquetada como `smart-parking-web.war` con **Jakarta Servlet 6.0**, **JSP 3.1**, **JSTL 3.0** y **JDBC** para Apache Tomcat 10.1 (`http://localhost:8081/smart-parking-web/`).

```text
SMART_PARKING/
├── README.md                          # Documentación general e integración (GA7-220501096-AA3-EV01)
├── ENLACE_REPOSITORIO.txt             # Enlace oficial al repositorio GitHub
├── login.html                         # Vista de acceso (demostración local)
├── dashboard.html                     # Panel de control e indicadores conectados a la API REST
├── ingreso.html                       # Formulario de registro de ingreso conectado a POST /api/v1/registros-parqueo y /clientes
├── consulta.html                      # Consulta por placa/documento y liquidación de salida/pago (GET/POST/PUT)
├── css/
│   └── style.css                      # Sistema de diseño visual y layout responsivo (escritorio y móvil)
├── js/
│   └── script.js                      # Cliente HTTP SmartParkingApi (Fetch API) y controladores de vistas
├── backend/                           # Backend Spring Boot 3.3.4 + Módulo Stand-Alone (Main.java) + 58 pruebas
│   ├── pom.xml
│   ├── README.md
│   └── src/
└── web-servlets/                      # Módulo Jakarta Servlet 6.0 / JSP 3.1 / JSTL 3.0 + 19 pruebas
    ├── pom.xml
    ├── README.md
    └── src/
```

---

## 2. Frameworks, Librerías y Tecnologías Utilizadas

| Capa / Módulo | Framework / Tecnología | Versión | Propósito en el Proyecto |
| :--- | :--- | :--- | :--- |
| **Lenguaje Base** | Java LTS | `21` | Codificación de módulos stand-alone, API REST y Servlets |
| **Framework Backend REST** | Spring Boot (`spring-boot-starter-web`) | `3.3.4` | Exposición de controladores RESTful, inyección de dependencias y servidor embebido |
| **Validación Declarativa** | Jakarta Bean Validation / Hibernate Validator | `3.3.4` | Validación `@Valid`, `@NotBlank`, `@Pattern`, `@PastOrPresent`, `@DecimalMin` en DTOs |
| **Documentación API** | Springdoc OpenAPI / Swagger UI | `2.6.0` | Especificación interactiva OpenAPI 3.0.1 en `/swagger-ui/index.html` y `/api-docs` |
| **Módulo Web Servlets** | Jakarta Servlet / JSP / JSTL | `6.0` / `3.1` / `3.0` | Procesamiento de formularios HTML `GET`/`POST` y vistas dinámicas en Tomcat 10.1 |
| **Frontend Web y Móvil** | Bootstrap 5 + Bootstrap Icons + Fetch API | `5.3.7` / ES2022 | Diseño responsivo móvil/escritorio, modales y consumo asíncrono JSON con `fetch()` |
| **Persistencia de Datos** | JDBC Nativo + MySQL Connector/J | `9.2.0` | Consultas parametrizadas (`PreparedStatement`) y gestión `try-with-resources` |
| **Pruebas Automatizadas** | JUnit 5 + Mockito + Spring `MockMvc` | `5.10.x` | 58 pruebas en `backend/` y 19 pruebas en `web-servlets/` (77 pruebas en total) |
| **Construcción** | Apache Maven | `3.9+` | Gestión de dependencias, compilación y ejecución de pruebas |

---

## 3. Requisitos Previos y Configuración de MySQL

### 3.1 Requisitos del Entorno
* **JDK 21 LTS** configurado en `JAVA_HOME`.
* **Apache Maven 3.9+**.
* **MySQL Server 8.x o 9.x** en ejecución sobre `localhost:3306`.
* **Servidor HTTP local** para el frontend (p. ej., extensión **Live Server** de VS Code en el puerto `5500` o `http://localhost:3000`).

### 3.2 Esquema de Base de Datos MySQL (`smart_parking`)
```sql
CREATE DATABASE IF NOT EXISTS smart_parking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE smart_parking;

CREATE TABLE IF NOT EXISTS registro_parqueo (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    hora_ingreso DATETIME NOT NULL,
    valor_calculado DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_doc VARCHAR(20) NOT NULL,
    num_doc VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_registro INT NOT NULL,
    fecha_pago DATETIME NOT NULL,
    valor_pagado DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    tiempo_limite_salida INT NOT NULL,
    CONSTRAINT fk_pago_registro FOREIGN KEY (id_registro)
        REFERENCES registro_parqueo (id_registro)
);
```

### 3.3 Archivo Local de Credenciales (`db.properties`)
Por seguridad, los archivos `db.properties` están excluidos en `.gitignore`. Antes de ejecutar el proyecto:
1. Copie `backend/src/main/resources/db.properties.example` a `backend/src/main/resources/db.properties`.
2. Copie `web-servlets/src/main/resources/db.properties.example` a `web-servlets/src/main/resources/db.properties`.
3. Configure su usuario y contraseña local de MySQL en ambos archivos.

---

## 4. Instrucciones de Ejecución

### 4.1 Ejecución del Backend Spring Boot (Puerto `8080`)
Desde el directorio `backend/`:
```bash
mvn clean compile
mvn spring-boot:run
```
* **URL Base de la API:** `http://localhost:8080/api/v1`
* **Verificación de Salud:** `http://localhost:8080/api/v1/health`
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

### 4.2 Ejecución del Frontend Web/Móvil Integrado
1. Con el backend activo en el puerto `8080`, sirva la carpeta raíz del proyecto mediante un servidor HTTP local en uno de los orígenes autorizados en [`CorsConfig.java`](backend/src/main/java/com/sena/smartparking/config/CorsConfig.java) (`http://localhost:5500`, `http://127.0.0.1:5500`, `http://localhost:3000`, `http://127.0.0.1:3000`, `http://localhost:8080`, `http://localhost:8081`):
   * **Opción recomendada (VS Code Live Server):** Clic derecho sobre `login.html` o `dashboard.html` → **Open with Live Server** (`http://127.0.0.1:5500/dashboard.html`).
   * **Opción por consola (Python / Node):**
     ```bash
     npx http-server -p 5500
     ```
2. *Nota:* No abra los archivos HTML directamente mediante el protocolo `file://`, ya que los navegadores modernos asignan `Origin: null` y bloquean las peticiones CORS por seguridad.

### 4.3 Ejecución del Módulo Stand-Alone y del Módulo `web-servlets`
* **Módulo Stand-Alone (`Main.java`):**
  ```bash
  cd backend
  mvn compile exec:java -Dexec.mainClass="com.sena.smartparking.Main"
  ```
* **Módulo Web Servlets/JSP (`smart-parking-web.war` en Tomcat 10.1, puerto `8081`):**
  ```bash
  cd web-servlets
  mvn cargo:run
  ```
  Disponible en `http://localhost:8081/smart-parking-web/`.

---

## 5. Endpoints Utilizados y Flujo de Integración Frontend ↔ Backend

### 5.1 Tabla de Endpoints Consumidos por `SmartParkingApi` (`js/script.js`)

| Método | Endpoint | Vista(s) que lo consumen | Propósito |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | `dashboard.html` | Verifica el estado del servicio (`UP`), versión (`1.0.0`) y conexión MySQL (`CONNECTED`). |
| `GET` | `/api/v1/registros-parqueo/conteo` | `dashboard.html` | Obtiene el conteo total histórico de filas en `registro_parqueo`. |
| `GET` | `/api/v1/registros-parqueo` | `dashboard.html`, `consulta.html` | Lista todos los registros de parqueo o filtra por placa (`?placa=...`). |
| `GET` | `/api/v1/registros-parqueo/{id}` | `consulta.html` | Verifica la existencia y estado actual de un registro antes de liquidar su salida. |
| `POST` | `/api/v1/registros-parqueo` | `ingreso.html` | Registra un nuevo ingreso de vehículo en MySQL (`HTTP 201 Created`). |
| `PUT` | `/api/v1/registros-parqueo/{id}` | `consulta.html` | Actualiza el estado del registro a `PAGADO` tras confirmar el pago. |
| `DELETE` | `/api/v1/registros-parqueo/{id}` | `SmartParkingApi` | Soporte para eliminación controlada con manejo de `HTTP 204 No Content` y `409 Conflict`. |
| `GET` | `/api/v1/clientes` | `dashboard.html`, `ingreso.html`, `consulta.html` | Lista clientes y permite buscar/verificar propietarios por documento (`?numDoc=...`) o correo. |
| `POST` | `/api/v1/clientes` | `ingreso.html` | Registra al propietario en la tabla `cliente` si no existía previamente (`HTTP 201 Created`). |
| `GET` | `/api/v1/pagos` | `dashboard.html` | Obtiene los pagos confirmados en la tabla `pago` para calcular el recaudo real recibido. |
| `GET` | `/api/v1/pagos/registro/{idRegistro}` | `consulta.html` | Consulta el historial de pagos asociados a un registro de parqueo específico. |
| `POST` | `/api/v1/pagos` | `consulta.html` | Registra el pago liquidado al momento de dar salida al vehículo (`HTTP 201 Created`). |

### 5.2 Flujo de Registro, Consulta y Salida/Pago
1. **Dashboard (`dashboard.html`):**
   * Consulta en paralelo `/health`, `/registros-parqueo/conteo`, `/registros-parqueo`, `/pagos` y `/clientes`.
   * Calcula `#kpiVehiculos` (ingresos de hoy, activos pendientes y total en BD), `#kpiCupos` y `#kpiOcupacion` sobre una **capacidad configurada de 100 cupos** (identificada explícitamente como constante de configuración y no dato de MySQL), y `#kpiIngresos` sumando **únicamente pagos efectivamente recibidos** en la tabla `pago` (`PAGADO`/`APROBADO`).
   * Si el backend está apagado, muestra el estado de desconexión sin presentar datos simulados.
2. **Registro de Ingreso (`ingreso.html`):**
   * Normaliza la placa a mayúsculas y valida su formato (`^[A-Z0-9-]{3,10}$`), valida que la fecha/hora no sea futura (`@PastOrPresent`) y aplica las tarifas existentes del proyecto (`Hora`: `$5.000`, `Convenio`: `$4.500`, `Mensualidad`: `$10.000`, `Día`: `$12.500`).
   * Si el usuario ingresa datos del propietario (`#propietario`, `#documento`, `#correo`), exige que los tres campos estén completos, comprueba primero si el cliente ya existe en MySQL para evitar duplicados y, si es nuevo, lo crea mediante `POST /api/v1/clientes`.
   * Envía `POST /api/v1/registros-parqueo` y, tras confirmar `HTTP 201`, muestra el modal `#modalRegistro` con el `idRegistro` asignado por MySQL y enlace directo para verificarlo en `consulta.html`.
3. **Consulta, Salida y Pago (`consulta.html`):**
   * Permite buscar por placa (`GET /api/v1/registros-parqueo?placa=...`) y/o por documento (`GET /api/v1/clientes?numDoc=...`), cargando también los pagos asociados (`GET /api/v1/pagos/registro/{idRegistro}`).
   * El botón **"Registrar Salida"** ejecuta una secuencia segura de 8 pasos: verifica existencia del registro, comprueba que no esté pagado, valida el monto, registra el pago en `POST /api/v1/pagos` (`HTTP 201`), actualiza el registro a `PAGADO` en `PUT /api/v1/registros-parqueo/{id}` (`HTTP 200`) e incluye **protección de consistencia** (si el pago se crea pero la actualización falla, informa el estado pendiente y permite sincronizar sin duplicar el cobro).

---

## 6. Funcionalidades Demostrativas y Datos Complementarios Locales

Para mantener total transparencia técnica frente al modelo relacional de MySQL:
* **Inicio de Sesión (`login.html`):** Dado que el backend no posee un endpoint de autenticación, el formulario de login opera como demostración local (validación de campos requeridos, opción "Recordarme" sólo para el nombre de usuario en `localStorage` sin almacenar contraseñas y redirección a `dashboard.html`).
* **Atributos Complementarios del Formulario (`color`, `tipoVehiculo`, `telefono`, `cupo`, `servicio`, `estadoVehiculo`, `observaciones`):** Estos campos de diseño visual no existen como columnas en `registro_parqueo`. Cuando se diligencian en `ingreso.html`, se guardan en `localStorage` (`sp_complementarios_locales_v1`) y la interfaz los etiqueta explícitamente como `(Dato local)` o `No aplica en BD` para distinguirlos de los datos confirmados por MySQL.
* **Botones de navegación a `reportes.html` y `configuracion.html`:** Permanecen como enlaces de demostración de la maqueta original.

---

## 7. Ejecución de Pruebas Automatizadas y Validación Funcional

* **Pruebas del Backend Spring Boot (58 pruebas aprobadas — JUnit 5, Mockito y `MockMvc`):**
  ```bash
  cd backend
  mvn test
  ```
* **Pruebas del Módulo `web-servlets` (19 pruebas aprobadas — JUnit 5 y Mockito):**
  ```bash
  cd web-servlets
  mvn test
  ```
* **Pruebas Funcionales y Validación Visual en Google Chrome:**
  * Se comprobó la integración extremo a extremo entre el frontend web responsivo (`SmartParkingApi` con `fetch()`), la API REST de Spring Boot (`http://localhost:8080/api/v1`) y MySQL (`smart_parking`):
    * **Dashboard conectado a MySQL:** Carga en tiempo real del estado del sistema (`UP`), conexión a base de datos (`CONNECTED`), conteos, indicadores KPI y movimientos reales.
    * **Registro de ingreso y propietario (`ingreso.html`):** Creación en MySQL del propietario con **`id_cliente = 4`** en la tabla `cliente` y del vehículo con placa **`IAX770`** e **`id_registro = 13`** en la tabla `registro_parqueo`.
    * **Consulta (`consulta.html`):** Búsqueda verificada por placa (`IAX770`) y por documento del propietario (`id_cliente = 4`), visualizando los detalles y el historial de movimientos.
    * **Liquidación de salida y estado final (`consulta.html`):** Registro del pago **`id_pago = 4`** por **`$5.000 COP`** en la tabla `pago`, actualización del registro **`id_registro = 13`** (`IAX770`) a su **estado final `PAGADO`** (`estado_pago = 'PAGADO'`) en `registro_parqueo`, y actualización inmediata del Dashboard después del pago.
