# Smart Parking — API del Proyecto (`GA7-220501096-AA5-EV04`)

**Evidencia SENA:** `GA7-220501096-AA5-EV04 — API del proyecto`  
**Proyecto:** Smart Parking — Sistema de Gestión de Parqueaderos  
**Programa de Formación:** Tecnología en Análisis y Desarrollo de Software (ADSO)  
**Ficha:** 3336134 | **Grupo:** 6  
**Repositorio Oficial en GitHub:** [https://github.com/jotacabrejo-tech/SMART_PARKING_GA7_AA2_EV01](https://github.com/jotacabrejo-tech/SMART_PARKING_GA7_AA2_EV01)

---

## 1. Descripción General de Smart Parking y de la API del Proyecto

**Smart Parking** es un sistema de gestión de parqueaderos diseñado para controlar el flujo de ingreso y salida de vehículos, la administración de clientes frecuentes o propietarios, y el registro de pagos y recaudos derivados del servicio de estacionamiento.

Este módulo (`backend/`) constituye la **API REST del proyecto** correspondiente a la evidencia **`GA7-220501096-AA5-EV04 — API del proyecto`**. Implementa una arquitectura limpia en capas (`Controller` → `Service` → `DAO JDBC` → `MySQL`) utilizando **Spring Boot 3.3.4** y **Java 21 LTS**, exponiendo endpoints RESTful con formato JSON, validaciones declarativas (`@Valid`), manejo centralizado de errores HTTP y documentación interactiva con **OpenAPI 3 / Swagger UI**.

---

## 2. Tecnologías Utilizadas

* **Lenguaje:** Java 21 LTS.
* **Framework Backend:** Spring Boot 3.3.4 (`spring-boot-starter-web`).
* **Validación de Datos:** Jakarta Bean Validation / Hibernate Validator (`spring-boot-starter-validation`).
* **Persistencia:** JDBC Nativo (`PreparedStatement`, `ResultSet`, `try-with-resources`) con conector oficial `com.mysql:mysql-connector-j:9.2.0`.
* **Documentación de la API:** Springdoc OpenAPI 2.6.0 (`springdoc-openapi-starter-webmvc-ui` — OpenAPI 3.0.1 y Swagger UI).
* **Pruebas Automatizadas:** JUnit 5, Mockito, Spring Boot Test y `MockMvc` (`spring-boot-starter-test`).
* **Gestor de Construcción:** Apache Maven 3.9+.

---

## 3. Arquitectura del Backend

La API está estructurada bajo el patrón de separación de responsabilidades en capas:

1. **Capa de Exposición REST (`controller/`):** Recibe las peticiones HTTP en formato JSON, aplica la validación `@Valid` sobre los DTOs de entrada y retorna respuestas estandarizadas (`ResponseEntity`) con los códigos de estado HTTP apropiados (`200`, `201`, `204`, `400`, `404`, `409`, `500`).
2. **Capa de Lógica de Negocio (`service/` y `service/impl/`):** Orquesta las reglas de negocio (normalización de placas y documentos, asignación de fecha/hora por defecto, verificación de unicidad documental de clientes y validación de existencia previa de registros de parqueo antes de registrar o eliminar pagos).
3. **Capa de Acceso a Datos (`dao/` y `dao/impl/`):** Ejecuta consultas SQL parametrizadas mediante `PreparedStatement` y gestiona el cierre automático de conexiones con bloques `try-with-resources`.
4. **Capa Transversal (`config/`, `dto/`, `exception/`, `model/`):** Centraliza la conexión JDBC (`ConexionBD`), políticas CORS (`CorsConfig`), especificación OpenAPI (`OpenApiConfig`), contratos de transferencia (`dto/`) y captura global de excepciones (`GlobalExceptionHandler`).

```text
backend/
├── pom.xml                                # Configuración Maven y dependencias
├── README.md                              # Documentación de la API del proyecto (EV04)
└── src/
    ├── main/
    │   ├── java/com/sena/smartparking/
    │   │   ├── SmartParkingApplication.java
    │   │   ├── Main.java
    │   │   ├── config/                    # ConexionBD, CorsConfig, OpenApiConfig
    │   │   ├── controller/                # HealthController, RegistroParqueoController, ClienteController, PagoController
    │   │   ├── service/                   # Interfaces y clases *ServiceImpl
    │   │   ├── dao/                       # Interfaces y clases *DAOImpl (JDBC)
    │   │   ├── dto/                       # Objetos Request, Response y ErrorResponse
    │   │   ├── exception/                 # GlobalExceptionHandler y excepciones HTTP
    │   │   └── model/                     # Entidades del modelo de datos
    │   └── resources/
    │       ├── application.properties     # Puerto 8080, UTF-8 y rutas de Swagger
    │       └── db.properties.example      # Plantilla segura de conexión MySQL
    └── test/
        └── java/com/sena/smartparking/
            ├── SmartParkingApplicationTests.java
            ├── controller/                # Pruebas MockMvc de los controladores REST
            └── service/                   # Pruebas unitarias con Mockito de los servicios
```

---

## 4. Configuración de la Conexión a MySQL e Instrucciones de Ejecución

### 4.1 Esquema de Base de Datos MySQL (`smart_parking`)
Asegúrese de tener **MySQL Server 8.x o 9.x** activo en el puerto `3306` y ejecute el siguiente script DDL si aún no existe el esquema:

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

### 4.2 Configuración Local Segura (`db.properties`)
El archivo `db.properties` está excluido del repositorio mediante `.gitignore` para proteger las credenciales locales.

1. En `backend/src/main/resources/`, copie `db.properties.example` como `db.properties`:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
2. Configure sus credenciales locales en `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
   db.user=TU_USUARIO_MYSQL
   db.password=TU_CONTRASENA_AQUI
   db.driver=com.mysql.cj.jdbc.Driver
   ```

### 4.3 Comandos para Compilar, Ejecutar Pruebas e Iniciar la API
Desde el directorio `backend/`:

```bash
# 1. Compilar el proyecto
mvn clean compile

# 2. Ejecutar la suite de 58 pruebas automatizadas
mvn test

# 3. Iniciar el servidor de la API en http://localhost:8080
mvn spring-boot:run
```

---

## 5. Documentación Interactiva con Swagger / OpenAPI 3

Una vez iniciado el servidor en el puerto `8080`, la documentación interactiva de la API está disponible en:
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` *(o `http://localhost:8080/swagger-ui.html`)*
* **Especificación OpenAPI 3.0.1 (JSON):** `http://localhost:8080/api-docs`

---

## 6. Endpoints REST Disponibles

| Módulo | Método HTTP | Endpoint | Descripción / Función | Códigos HTTP |
| :--- | :---: | :--- | :--- | :--- |
| **Salud y Monitoreo** | `GET` | `/api/v1/health` | Verifica disponibilidad de la API y estado de conexión con MySQL (`CONNECTED`) | `200` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo` | Lista todos los registros de parqueo o filtra por parámetro opcional `?placa=` | `200`, `500` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo/conteo` | Retorna el total de registros de parqueo en la base de datos | `200`, `500` |
| **Registros de Parqueo** | `GET` | `/api/v1/registros-parqueo/{id}` | Consulta un registro de parqueo por su ID numérico | `200`, `404`, `500` |
| **Registros de Parqueo** | `POST` | `/api/v1/registros-parqueo` | Registra el ingreso de un vehículo y retorna cabecera `Location` | `201`, `400`, `500` |
| **Registros de Parqueo** | `PUT` | `/api/v1/registros-parqueo/{id}` | Actualiza placa, hora, tarifa o estado de pago de un registro existente | `200`, `400`, `404`, `500` |
| **Registros de Parqueo** | `DELETE` | `/api/v1/registros-parqueo/{id}` | Elimina un registro de parqueo si no posee pagos asociados | `204`, `404`, `409`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes` | Lista todos los clientes registrados en el sistema | `200`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes/{id}` | Consulta el detalle de un cliente por su ID | `200`, `404`, `500` |
| **Clientes** | `POST` | `/api/v1/clientes` | Registra un nuevo cliente validando documento único y formato de correo | `201`, `400`, `409`, `500` |
| **Clientes** | `PUT` | `/api/v1/clientes/{id}` | Actualiza los datos de un cliente existente verificando ausencia de duplicidad | `200`, `400`, `404`, `409`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos` | Lista el historial completo de pagos registrados | `200`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos/{id}` | Consulta el detalle de un pago específico por su ID | `200`, `404`, `500` |
| **Pagos y Recaudos** | `GET` | `/api/v1/pagos/registro/{idRegistro}` | Lista los pagos asociados a un registro de parqueo específico | `200`, `404`, `500` |
| **Pagos y Recaudos** | `POST` | `/api/v1/pagos` | Registra un pago liquidado validando existencia previa del registro de parqueo | `201`, `400`, `404`, `500` |

---

## 7. Instrucciones para Ejecutar y Probar la API con Postman

1. **Iniciar el servidor:** Ejecute `mvn spring-boot:run` en `backend/` y verifique que responda en `http://localhost:8080/api/v1/health`.
2. **Configurar cabeceras en Postman:** Para peticiones `POST` y `PUT`, agregue el encabezado `Content-Type: application/json` y seleccione `Body` → `raw` → `JSON`.
3. **Ejemplos de Cuerpos JSON para Pruebas en Postman:**

   * **Registrar Ingreso de Vehículo (`POST http://localhost:8080/api/v1/registros-parqueo`):**
     ```json
     {
       "placa": "KLM789",
       "tarifaAplicada": 6500.00,
       "estadoPago": "PENDIENTE"
     }
     ```
   * **Actualizar Registro de Parqueo (`PUT http://localhost:8080/api/v1/registros-parqueo/{id}`):**
     ```json
     {
       "placa": "KLM789",
       "tarifaAplicada": 6500.00,
       "estadoPago": "PAGADO"
     }
     ```
   * **Registrar Nuevo Cliente (`POST http://localhost:8080/api/v1/clientes`):**
     ```json
     {
       "tipoDoc": "CC",
       "numDoc": "1020304050",
       "nombres": "Carlos Alberto Mendoza",
       "correo": "carlos.mendoza@email.com"
     }
     ```
   * **Registrar Pago de Parqueadero (`POST http://localhost:8080/api/v1/pagos`):**
     ```json
     {
       "idRegistro": 1,
       "valorPagado": 6500.00,
       "estadoPago": "PAGADO",
       "tiempoLimiteSalida": 15
     }
     ```

---

## 8. Resultados Generales de las Pruebas Realizadas

* **Compilación (`mvn clean compile`):** `BUILD SUCCESS` (45 clases compiladas con Java 21, 0 errores).
* **Pruebas Automatizadas (`mvn test`):** `BUILD SUCCESS` — **58 pruebas ejecutadas, 58 aprobadas, 0 fallos, 0 errores, 0 omitidas**:
  * `RegistroParqueoControllerTest` (12 pruebas) y `RegistroParqueoServiceTest` (10 pruebas)
  * `ClienteControllerTest` (9 pruebas) y `ClienteServiceTest` (8 pruebas)
  * `PagoControllerTest` (10 pruebas) y `PagoServiceTest` (7 pruebas)
  * `HealthControllerTest` (1 prueba) y `SmartParkingApplicationTests` (1 prueba)
* **Verificación de Salud y Conectividad MySQL (`/api/v1/health`):** `HTTP 200 OK` (`status: UP`, `database: CONNECTED`).
* **Verificación de Especificación OpenAPI (`/api-docs` y `/swagger-ui/index.html`):** `HTTP 200 OK` con los 4 módulos documentados.
