# Smart Parking — Módulo Backend Java JDBC
**Evidencia SENA:** `GA7-220501096-AA2-EV01 — Codificación de módulos del software`  
**Programa de Formación:** Análisis y Desarrollo de Software (ADSO)  
**Ficha:** 3336134 | **Grupo:** 6  

---

## 1. Descripción del Proyecto

Este módulo implementa la capa de persistencia y lógica de acceso a datos relacionales para el sistema **Smart Parking**, conectándose mediante **JDBC (Java Database Connectivity)** a un motor **MySQL Server 9.7 / 8.x**.

El desarrollo adopta una **arquitectura por capas desacoplada** con objetos de transferencia de datos (DTO), contratos de interfaz (DAO) e implementaciones con sentencias preparadas (`PreparedStatement`) y gestión automática de recursos (`try-with-resources`).

---

## 2. Requisitos del Entorno

* **Java Development Kit (JDK):** Versión 21 LTS (OpenJDK / Eclipse Temurin).
* **Apache Maven:** Versión 3.9.x o superior.
* **Motor RDBMS:** MySQL Server 9.7 (o MySQL 8.0/8.4 LTS) activo en el puerto `3306`.
* **Controlador JDBC:** `com.mysql:mysql-connector-j:9.2.0` (gestionado por Maven).

---

## 3. Cobertura del Modelo de Datos

La base de datos `smart_parking` contiene seis (6) tablas relacionales. El estado de implementación de la capa de software es el siguiente:

| Tabla en MySQL | Modelo DTO (`com.sena.smartparking.model`) | Capa DAO / CRUD (`com.sena.smartparking.dao`) | Estado de Implementación |
| :--- | :--- | :--- | :--- |
| `registro_parqueo` | `RegistroParqueo.java` | `RegistroParqueoDAO.java`<br>`RegistroParqueoDAOImpl.java` | **100% Implementado y Verificado** (CRUD completo) |
| `cliente` | `Cliente.java` | *Pendiente para módulo de clientes* | **DTO Mapeado** |
| `pago` | `Pago.java` | *Pendiente para módulo de recaudos* | **DTO Mapeado** |
| `factura` | `Factura.java` | *Pendiente para módulo de facturación* | **DTO Mapeado** |
| `comprobante` | `Comprobante.java` | *Pendiente para módulo de reportes* | **DTO Mapeado** |
| `transaccion` | `Transaccion.java` | *Pendiente para módulo de pasarela* | **DTO Mapeado** |

> [!NOTE]  
> Para la evidencia `GA7-220501096-AA2-EV01`, el módulo central implementado y verificado en su totalidad corresponde a **`RegistroParqueo`** (control de ingreso, cobro y salida de vehículos), alineado con las interfaces del front-end (`ingreso.html` y `consulta.html`).

---

## 4. Configuración Segura de la Conexión

Por razones estrictas de seguridad, las credenciales reales de la base de datos **nunca se publican en el repositorio de control de versiones** y se encuentran excluidas mediante el archivo `.gitignore` raíz.

### Pasos para configurar el entorno local:
1. Diríjase a `backend/src/main/resources/`.
2. Copie el archivo de plantilla `db.properties.example` y renómbrelo como `db.properties`:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
3. Ingrese sus credenciales locales en `db.properties`:
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
   db.user=smartparking_app
   db.password=SU_CONTRASENA_AQUI
   db.driver=com.mysql.cj.jdbc.Driver
   ```

---

## 5. Compilación y Ejecución

### Compilación limpia del proyecto:
Desde el directorio `backend/`:
```bash
mvn clean compile
```

### Ejecución de pruebas controladas del ciclo CRUD:
```bash
mvn compile exec:java
```

Este comando ejecuta la clase principal `com.sena.smartparking.Main`, la cual:
1. Comprueba la conexión activa con `smart_parking`.
2. Realiza la inserción controlada de un registro de prueba (`TEST-99`).
3. Consulta el registro por clave primaria y por placa.
4. Actualiza los valores de tarifa y estado.
5. Elimina el registro de prueba de forma segura.
6. Confirma la integridad final de la base de datos (0 registros huérfanos).

---

## 6. Estructura de Paquetes

```text
backend/src/main/java/com/sena/smartparking/
├── config/
│   └── ConexionBD.java          # Conexión JDBC Singleton y carga de propiedades
├── model/
│   ├── RegistroParqueo.java     # DTO tabla registro_parqueo
│   ├── Cliente.java             # DTO tabla cliente
│   ├── Pago.java                # DTO tabla pago
│   ├── Factura.java             # DTO tabla factura
│   ├── Comprobante.java         # DTO tabla comprobante
│   └── Transaccion.java         # DTO tabla transaccion
├── dao/
│   └── RegistroParqueoDAO.java  # Contrato de operaciones CRUD
├── dao/impl/
│   └── RegistroParqueoDAOImpl.java # Implementación JDBC con PreparedStatement
├── exception/
│   └── DatabaseException.java   # Excepciones tipificadas
└── Main.java                    # Demostración y pruebas funcionales
```
