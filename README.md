# SMART PARKING — Sistema de Gestión de Parqueaderos

* **Proyecto Permanente:** `SMART PARKING — Sistema de Gestión de Parqueaderos`
* **Evidencia Académica Actual:** `GA7-220501096-AA5-EV04 — API del proyecto`
* **Programa de Formación:** Tecnología en Análisis y Desarrollo de Software (ADSO) — Ficha 3336134 | Grupo 6
* **Repositorio Permanente en GitHub:** [https://github.com/jotacabrejo-tech/SMART_PARKING_PROYECTO](https://github.com/jotacabrejo-tech/SMART_PARKING_PROYECTO)

---

## 1. Descripción General de Smart Parking

**Smart Parking** es un sistema de software para la administración integral de parqueaderos que automatiza el registro de ingreso y salida de vehículos, la gestión de clientes/propietarios y el control de recaudos y pagos liquidados.

En la evidencia **`GA7-220501096-AA5-EV04 — API del proyecto`**, se presenta la **API RESTful** del sistema implementada en el directorio [`backend/`](backend/README.md) sobre **Java 21 LTS**, **Spring Boot 3.3.4** y **JDBC nativo con MySQL**, junto con su documentación interactiva **OpenAPI 3 / Swagger UI**, guía de pruebas en **Postman** y suite de **58 pruebas automatizadas** (`JUnit 5`, `Mockito`, `MockMvc`).

---

## 2. Tecnologías Utilizadas

| Capa / Componente | Tecnología / Versión |
| :--- | :--- |
| **Lenguaje Base** | Java 21 LTS |
| **Framework de API REST** | Spring Boot 3.3.4 (`spring-boot-starter-web`) |
| **Validación de Datos** | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| **Acceso a Datos** | JDBC Nativo (`PreparedStatement`, `try-with-resources`) |
| **Motor de Base de Datos** | MySQL 8.x / 9.x (`mysql-connector-j:9.2.0`) |
| **Documentación de API** | Springdoc OpenAPI 2.6.0 (OpenAPI 3.0.1 / Swagger UI) |
| **Pruebas Automatizadas** | JUnit 5, Mockito, Spring Boot Test y `MockMvc` |
| **Construcción y Dependencias** | Apache Maven 3.9+ |
| **Cliente de Pruebas de API** | Postman / Swagger UI |

---

## 3. Arquitectura del Proyecto

```text
SMART_PARKING/
├── README.md                          # Presentación general de la API del proyecto (EV04)
├── ENLACE_REPOSITORIO.txt             # Enlace oficial al repositorio GitHub
├── .gitignore                         # Reglas de exclusión (db.properties, target/, entregas/)
├── login.html / dashboard.html / ...  # Vistas estáticas del prototipo web
└── backend/                           # API REST del proyecto (Spring Boot 3 + JDBC)
    ├── pom.xml                        # Configuración Maven
    ├── README.md                      # Documentación detallada de la API, Swagger y Postman
    └── src/
        ├── main/
        │   ├── java/com/sena/smartparking/
        │   │   ├── SmartParkingApplication.java
        │   │   ├── config/            # ConexionBD, CorsConfig, OpenApiConfig
        │   │   ├── controller/        # HealthController, RegistroParqueoController, ClienteController, PagoController
        │   │   ├── service/           # Lógica de negocio (*Service y *ServiceImpl)
        │   │   ├── dao/               # Persistencia JDBC (*DAO y *DAOImpl)
        │   │   ├── dto/               # Objetos Request/Response con validaciones @Valid
        │   │   ├── exception/         # GlobalExceptionHandler y excepciones HTTP 404/409/500
        │   │   └── model/             # Entidades del modelo relacional
        │   └── resources/
        │       ├── application.properties
        │       └── db.properties.example
        └── test/
            └── java/com/sena/smartparking/ # 58 pruebas unitarias y de controladores (MockMvc)
```

---

## 4. Resumen de Endpoints REST de la API (`http://localhost:8080`)

| Módulo | Método HTTP | Endpoint | Función Principal | Códigos HTTP |
| :--- | :---: | :--- | :--- | :--- |
| **Salud** | `GET` | `/api/v1/health` | Verifica el estado operativo de la API y la conexión con MySQL | `200` |
| **Parqueos** | `GET` | `/api/v1/registros-parqueo` | Lista todos los registros de parqueo o filtra por `?placa=` | `200`, `500` |
| **Parqueos** | `GET` | `/api/v1/registros-parqueo/conteo` | Retorna el conteo total de registros de parqueo | `200`, `500` |
| **Parqueos** | `GET` | `/api/v1/registros-parqueo/{id}` | Consulta un registro de parqueo por su ID | `200`, `404`, `500` |
| **Parqueos** | `POST` | `/api/v1/registros-parqueo` | Registra un nuevo ingreso de vehículo | `201`, `400`, `500` |
| **Parqueos** | `PUT` | `/api/v1/registros-parqueo/{id}` | Actualiza placa, hora, tarifa o estado de un registro | `200`, `400`, `404`, `500` |
| **Parqueos** | `DELETE` | `/api/v1/registros-parqueo/{id}` | Elimina un registro si no tiene pagos asociados | `204`, `404`, `409`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes` | Lista todos los clientes registrados | `200`, `500` |
| **Clientes** | `GET` | `/api/v1/clientes/{id}` | Consulta un cliente por su ID | `200`, `404`, `500` |
| **Clientes** | `POST` | `/api/v1/clientes` | Registra un nuevo cliente validando documento único | `201`, `400`, `409`, `500` |
| **Clientes** | `PUT` | `/api/v1/clientes/{id}` | Actualiza los datos de un cliente existente | `200`, `400`, `404`, `409`, `500` |
| **Pagos** | `GET` | `/api/v1/pagos` | Lista todos los pagos registrados | `200`, `500` |
| **Pagos** | `GET` | `/api/v1/pagos/{id}` | Consulta un pago específico por su ID | `200`, `404`, `500` |
| **Pagos** | `GET` | `/api/v1/pagos/registro/{idRegistro}` | Lista los pagos asociados a un registro de parqueo | `200`, `404`, `500` |
| **Pagos** | `POST` | `/api/v1/pagos` | Registra un pago liquidado para un parqueo existente | `201`, `400`, `404`, `500` |

---

## 5. Inicio Rápido y Documentación Detallada

1. Configure su archivo local `backend/src/main/resources/db.properties` a partir de `db.properties.example`.
2. Desde la carpeta `backend/`, ejecute:
   ```bash
   mvn clean compile
   mvn test
   mvn spring-boot:run
   ```
3. Consulte la documentación interactiva en:
   * **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
   * **OpenAPI JSON:** `http://localhost:8080/api-docs`
4. Consulte la guía técnica completa de endpoints y pruebas en Postman en [`backend/README.md`](backend/README.md).

