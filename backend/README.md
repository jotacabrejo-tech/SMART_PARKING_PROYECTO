# Smart Parking — Backend API REST con Spring Boot 3 y JDBC

**Evidencia SENA:** `GA7-220501096-AA5-EV03 — Diseño y desarrollo de servicios web – proyecto`  
*(Evolución modular a partir de la evidencia GA7-220501096-AA2-EV01)*  
**Programa de Formación:** Tecnología en Análisis y Desarrollo de Software (ADSO)  
**Ficha:** 3336134 | **Grupo:** 6  
**Repositorio Oficial:** [https://github.com/jotacabrejo-tech/SMART_PARKING_GA7_AA2_EV01](https://github.com/jotacabrejo-tech/SMART_PARKING_GA7_AA2_EV01)

---

## 1. Nombre y Objetivo del Proyecto

**Smart Parking Backend** es el subsistema de servicios web RESTful para la plataforma de gestión de parqueaderos **Smart Parking**. Su objetivo en la evidencia **GA7-220501096-AA5-EV03** es exponer mediante una arquitectura en capas (`Controller` → `Service` → `DAO JDBC` → `MySQL`) las operaciones de negocio para el registro de ingresos y salidas de vehículos, la administración de clientes y la gestión de pagos y recaudos, aplicando estándares HTTP/REST, validación declarativa de datos, manejo centralizado de errores y documentación interactiva OpenAPI 3 / Swagger UI.

---

## 2. Tecnologías Utilizadas y Requisitos Previos

### Stack Tecnológico
* **Lenguaje:** Java 21 LTS.
* **Framework Web:** Spring Boot 3.3.4 (`spring-boot-starter-web`).
* **Validación de Datos:** Jakarta Bean Validation / Hibernate Validator (`spring-boot-starter-validation`).
* **Persistencia:** JDBC Nativo (`PreparedStatement`, `ResultSet`, `try-with-resources`) con conector oficial `com.mysql:mysql-connector-j:9.2.0`.
* **Documentación de API:** Springdoc OpenAPI 2.6.0 (`springdoc-openapi-starter-webmvc-ui` — OpenAPI 3.0.1 y Swagger UI).
* **Pruebas Automatizadas:** JUnit 5, Mockito, Spring Boot Test y `MockMvc` (`spring-boot-starter-test`).
* **Gestor de Construcción:** Apache Maven 3.9+.

### Requisitos Previos de Ejecución
1. **JDK 21 LTS** instalado y configurado en la variable de entorno `JAVA_HOME`.
2. **Apache Maven 3.9+** disponible en el `PATH`.
3. **MySQL Server 8.x o 9.x** en ejecución local (puerto `3306` por defecto) con el esquema `smart_parking` creado.

---

## 3. Configuración de Base de Datos MySQL y Credenciales Locales

### 3.1 Preparación del Esquema MySQL (`smart_parking`)
Asegúrese de contar con la base de datos `smart_parking` activa en su servidor MySQL local con las tablas del modelo relacional del proyecto:
```sql
CREATE DATABASE IF NOT EXISTS smart_parking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE smart_parking;

CREATE TABLE IF NOT EXISTS registro_parqueo (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    hora_ingreso DATETIME NOT NULL,
    tarifa_aplicada DECIMAL(10,2) NOT NULL,
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

### 3.2 Configuración Segura de `db.properties`
Por seguridad, el archivo `db.properties` está excluido del control de versiones mediante `.gitignore` y nunca debe subirse al repositorio.

1. Ubíquese en `backend/src/main/resources/`.
2. Copie la plantilla de ejemplo `db.properties.example` como `db.properties`:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
3. Edite `src/main/resources/db.properties` con sus credenciales locales reales (sin exponerlas en Git):
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
   db.user=TU_USUARIO_MYSQL
   db.password=TU_CONTRASENA_AQUI
   db.driver=com.mysql.cj.jdbc.Driver
   ```
4. El archivo `src/main/resources/application.properties` contiene únicamente parámetros de aplicación seguros para el repositorio (puerto `8080`, codificación `UTF-8` y rutas de OpenAPI/Swagger).

---

## 4. Compilación, Pruebas y Ejecución del Servidor

Ejecute los siguientes comandos desde el directorio `backend/`:

### Compilar el proyecto:
```bash
mvn clean compile
```

### Ejecutar la suite de pruebas unitarias y de controladores (`MockMvc`):
```bash
mvn test
```

### Iniciar el servidor Spring Boot en el puerto `8080`:
```bash
mvn spring-boot:run
```

---

## 5. Documentación Interactiva (Swagger UI y OpenAPI 3)

Con el servidor en ejecución sobre `http://localhost:8080`, acceda a:
* **Swagger UI (Interfaz Interactiva):** `http://localhost:8080/swagger-ui/index.html` *(o mediante redirección desde `http://localhost:8080/swagger-ui.html`)*
* **Especificación OpenAPI 3.0.1 (JSON):** `http://localhost:8080/api-docs`
* **Endpoint de Salud y Estado de BD:** `http://localhost:8080/api/v1/health`

---

## 6. Catálogo de Endpoints RESTful por Módulo

| Módulo | Método HTTP | Endpoint | Propósito | Códigos de Respuesta |
| :--- | :--- | :--- | :--- | :--- |
| **Salud y Monitoreo** | `GET` | `/api/v1/health` | Verifica disponibilidad de la API y estado de conexión JDBC con MySQL | `200` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo` | Lista todos los registros de parqueo o filtra por parámetro opcional `?placa=` | `200`, `500` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo/conteo` | Retorna el conteo total de registros de parqueo almacenados | `200`, `500` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo/{id}` | Consulta el detalle de un registro de parqueo por su ID numérico | `200`, `404`, `500` |
| **Registros de Parqueo** | `POST` | `/api/v1/registros-parqueo` | Registra el ingreso de un vehículo y retorna cabecera `Location` | `201`, `400`, `500` |
| **Registros de Parqueo** | `PUT` | `/api/v1/registros-parqueo/{id}` | Actualiza los datos (placa, tarifa, estado de pago) de un registro existente | `200`, `400`, `404`, `500` |
| **Registros de Parqueo** | `DELETE` | `/api/v1/registros-parqueo/{id}` | Elimina un registro de parqueo si no tiene pagos asociados | `204`, `404`, `409`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes` | Lista todos los clientes registrados en el sistema | `200`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes/{id}` | Consulta el detalle de un cliente por su ID numérico | `200`, `404`, `500` |
| **Clientes** | `POST` | `/api/v1/clientes` | Registra un nuevo cliente validando documento único y formato de correo | `201`, `400`, `409`, `500` |
| **Clientes** | `PUT` | `/api/v1/clientes/{id}` | Actualiza un cliente existente verificando ausencia de colisión de documento | `200`, `400`, `404`, `409`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos` | Lista el historial completo de pagos registrados | `200`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos/{id}` | Consulta el detalle de un pago específico por su ID | `200`, `404`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos/registro/{idRegistro}` | Lista todos los pagos asociados a un registro de parqueo específico | `200`, `404`, `500` |
| **Pagos y Recaudos** | `POST` | `/api/v1/pagos` | Registra un pago liquidado validando que el registro de parqueo exista | `201`, `400`, `404`, `500` |

---

## 7. Resumen de Validaciones y Manejo Global de Errores

### Validaciones de Entrada (`@Valid` / Jakarta Validation)
* **`RegistroParqueoRequest`:**
  * `placa`: Obligatoria, entre 5 y 10 caracteres alfanuméricos (`^[A-Za-z0-9-]+$`), normalizada a mayúsculas.
  * `horaIngreso`: Opcional (`@PastOrPresent`); si no se envía, el servicio asigna la fecha/hora actual del servidor.
  * `tarifaAplicada`: Obligatoria, mayor o igual a `0.00`.
  * `estadoPago`: Obligatorio, valores permitidos `PENDIENTE`, `PAGADO`, `EXENTO` o `ANULADO`.
* **`ClienteRequest`:**
  * `tipoDoc`: Obligatorio, valores permitidos `CC`, `CE`, `TI`, `NIT`, `PAS` o `PEP`.
  * `numDoc`: Obligatorio, entre 3 y 20 caracteres alfanuméricos/guiones. Se valida unicidad de `(tipo_doc, num_doc)`.
  * `nombres`: Obligatorio, entre 2 y 100 caracteres.
  * `correo`: Obligatorio, formato de email válido (`@Email`), máximo 100 caracteres.
* **`PagoRequest`:**
  * `idRegistro`: Obligatorio, entero positivo (`@Positive`) correspondiente a un registro de parqueo existente.
  * `fechaPago`: Opcional (`@PastOrPresent`); por defecto se asigna la hora actual.
  * `valorPagado`: Obligatorio, estrictamente mayor a cero (`@DecimalMin("0.01")`).
  * `estadoPago`: Obligatorio, valores permitidos `APROBADO`, `PAGADO`, `PENDIENTE` o `RECHAZADO`.
  * `tiempoLimiteSalida`: Obligatorio, entre `0` y `1440` minutos.

### Manejo Global de Excepciones (`GlobalExceptionHandler`)
Todas las respuestas de error devuelven el contrato JSON uniforme `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `path` y `fieldErrors` opcional):
* **`400 Bad Request`:** Errores de validación de campos (`MethodArgumentNotValidException`), JSON mal formado (`HttpMessageNotReadableException`) o argumentos inválidos (`IllegalArgumentException`).
* **`404 Not Found`:** Recurso inexistente en base de datos (`ResourceNotFoundException`).
* **`409 Conflict`:** Violaciones de integridad referencial o duplicidad de negocio, como cliente con documento repetido o intento de eliminar un registro de parqueo con pagos vinculados (`IntegrityConflictException`).
* **`500 Internal Server Error`:** Fallos inesperados o de conectividad en la capa de persistencia (`DatabaseException` / `Exception`).

---

## 8. Estructura General de Carpetas

```text
SMART_PARKING/
├── .gitignore                         # Reglas de exclusión (db.properties, target/, logs, IDEs)
├── ENLACE_REPOSITORIO.txt             # Enlace al repositorio GitHub del proyecto
├── login.html / dashboard.html / ...  # Maquetas estáticas del frontend web
└── backend/                           # Proyecto Backend Spring Boot 3 + JDBC
    ├── pom.xml                        # Configuración Maven (Java 21, Spring Boot 3.3.4, MySQL, Springdoc)
    ├── README.md                      # Documentación técnica del backend
    └── src/
        ├── main/
        │   ├── java/com/sena/smartparking/
        │   │   ├── SmartParkingApplication.java
        │   │   ├── Main.java
        │   │   ├── config/            # ConexionBD (JDBC), CorsConfig, OpenApiConfig
        │   │   ├── controller/        # HealthController, RegistroParqueoController, ClienteController, PagoController
        │   │   ├── service/           # Interfaces e implementaciones (*ServiceImpl)
        │   │   ├── dao/               # Interfaces e implementaciones JDBC (*DAOImpl)
        │   │   ├── dto/               # Objetos de transferencia Request/Response y ErrorResponse
        │   │   ├── exception/         # GlobalExceptionHandler y excepciones de negocio/BD
        │   │   └── model/             # Entidades del modelo de dominio relacional
        │   └── resources/
        │       ├── application.properties
        │       └── db.properties.example
        └── test/
            └── java/com/sena/smartparking/
                ├── SmartParkingApplicationTests.java
                ├── controller/        # Pruebas MockMvc de los 4 controladores REST
                └── service/           # Pruebas unitarias con Mockito de los 3 servicios
```

---

## 9. Alcance Actual, Limitaciones e Integraciones Pendientes

Para total transparencia técnica y académica sobre el alcance de la evidencia **GA7-220501096-AA5-EV03**:
1. **Persistencia de Módulos Adicionales (`Factura`, `Transaccion`, `Comprobante`):** Aunque existen las clases de modelo en el paquete `model/`, la exposición de servicios RESTful, lógica de negocio y DAOs en esta entrega se concentra en los módulos núcleo de **Registros de Parqueo**, **Clientes**, **Pagos y Recaudos** y **Salud de la API**.
2. **Sin Pasarelas de Pago Externas ni Facturación Electrónica DIAN:** El módulo de pagos registra los recaudos y su estado en la base de datos local MySQL; no incluye integración activa con pasarelas bancarias reales ni emisión de facturación electrónica ante la DIAN.
3. **Integración con el Frontend Estático:** Se configuró la política CORS (`CorsConfig`) para permitir peticiones desde servidores locales de desarrollo (`localhost:5500`, `localhost:3000`, `localhost:8080`), pero las vistas HTML de la raíz corresponden a prototipos/maquetas cuya integración completa con todos los endpoints REST y autenticación de sesiones queda proyectada para fases posteriores.
