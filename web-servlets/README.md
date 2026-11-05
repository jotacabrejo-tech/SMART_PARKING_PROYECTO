# SMART PARKING — Módulo Web Jakarta Servlets & JSP (`web-servlets`)

**Evidencia SENA:** `GA7-220501096-AA2-EV02 — Módulos de software codificados y probados`  
**Proyecto:** SMART PARKING  
**Programa de Formación:** Tecnólogo en Análisis y Desarrollo de Software (ADSO) — SENA  
**Repositorio Permanente:** https://github.com/jotacabrejo-tech/SMART_PARKING_PROYECTO

---

## 1. Descripción General

El módulo `web-servlets` es una aplicación web Java empresarial independiente, empaquetada como archivo **`WAR` (`smart-parking-web.war`)**, que forma parte del proyecto **SMART PARKING**. Permite gestionar desde el navegador web los tres procesos nucleares del parqueadero sobre la base de datos relacional MySQL **`smart_parking`**:

1. **Registros de Parqueo (`/parqueos`):** Ingreso de vehículos, cálculo de valor/tarifa (`valor_calculado`) y consulta filtrada por placa o estado de pago.
2. **Clientes (`/clientes`):** Registro de clientes con validación de unicidad documental (`tipo_doc` + `num_doc`) y búsqueda por documento.
3. **Pagos (`/pagos`):** Registro de pagos asociados a un registro de parqueo existente (respetando la clave foránea `fk_pago_registro`) y filtrado por identificador de registro.

---

## 2. Objetivo del Módulo

Cumplir de forma verificable y trazable los cuatro criterios técnicos exigidos por el instrumento de evaluación del SENA para la evidencia **GA7-220501096-AA2-EV02**:

1. **Contener formularios HTML conectados a Servlets.**
2. **Utilizar los métodos HTTP `GET` y `POST` para el manejo de parámetros.**
3. **Implementar elementos JSP (directivas, Expression Language, JSTL y fragmentos `.jspf`).**
4. **Utilizar herramientas de versionamiento con Git y GitHub.**

---

## 3. Arquitectura

El módulo implementa el patrón arquitectónico **MVC (Modelo – Vista – Controlador)** sobre **Jakarta EE 10**, manteniendo separación estricta de responsabilidades y coexistiendo de manera independiente con el backend Spring Boot del proyecto:

```text
Navegador Web (Formularios HTML GET / POST)
       │
       ▼
Capa Controladora — Jakarta Servlets 6.0 (@WebServlet: doGet / doPost)
       │
       ├──► Capa de Acceso a Datos — DAO + JDBC (PreparedStatement + try-with-resources)
       │           │
       │           ▼
       │    Base de Datos MySQL 9.7.1 (smart_parking)
       │
       ▼
Capa de Presentación — Vistas JSP 3.1 + JSTL 3.0 + EL (/WEB-INF/views/*.jsp)
```

---

## 4. Tecnologías Utilizadas

| Categoría | Tecnología | Versión | Uso en el Módulo |
| :--- | :--- | :--- | :--- |
| Lenguaje | Java (OpenJDK) | `21` (`21.0.12.1+1`) | Compilación y ejecución con características de Java 21. |
| Controladores Web | Jakarta Servlet API | `6.0.0` | Servlets `@WebServlet` con métodos `doGet()` y `doPost()`. |
| Motor de Vistas | Jakarta Server Pages (JSP) | `3.1.1` | Vistas dinámicas `.jsp` y fragmentos `.jspf`. |
| Librería de Etiquetas | Jakarta Standard Tag Library (JSTL) | `3.0.0` / `3.0.1` | Iteración `<c:forEach>`, condicionales `<c:if>`, `<c:choose>` y salida segura `<c:out>`. |
| Persistencia | JDBC Nativo + MySQL Connector/J | `9.2.0` | Conexión SQL segura mediante `PreparedStatement`. |
| Motor de Base de Datos | MySQL Server | `9.7.1` | Base de datos relacional `smart_parking`. |
| Contenedor Web | Apache Tomcat | `10.1.30` | Despliegue y ejecución del artefacto `smart-parking-web.war` en puerto `8081`. |
| Construcción | Apache Maven | `3.9.16` | Gestión de dependencias, pruebas, empaquetado `WAR` y plugin `cargo-maven3-plugin`. |
| Pruebas Automatizadas | JUnit Jupiter + Mockito | `5.10.3` / `5.11.0` | Pruebas unitarias de Servlets y gestión de conexión JDBC. |

---

## 5. Estructura de Carpetas

```text
web-servlets/
├── pom.xml                                     # Configuración Maven (packaging: war, Java 21, Cargo Tomcat 10.1)
├── README.md                                   # Documentación técnica completa del módulo
├── sql/
│   └── esquema_smart_parking.sql               # DDL SQL de referencia de la base de datos smart_parking
└── src/
    ├── main/
    │   ├── java/com/sena/smartparking/web/
    │   │   ├── config/
    │   │   │   └── ConexionBD.java             # Gestor de conexiones JDBC (variables de entorno o db.properties)
    │   │   ├── model/
    │   │   │   ├── RegistroParqueo.java        # JavaBean mapeado a la tabla registro_parqueo
    │   │   │   ├── Cliente.java                # JavaBean mapeado a la tabla cliente
    │   │   │   └── Pago.java                   # JavaBean mapeado a la tabla pago
    │   │   ├── dao/
    │   │   │   ├── RegistroParqueoDAO.java
    │   │   │   ├── ClienteDAO.java
    │   │   │   ├── PagoDAO.java
    │   │   │   └── impl/
    │   │   │       ├── RegistroParqueoDAOImpl.java
    │   │   │       ├── ClienteDAOImpl.java
    │   │   │       └── PagoDAOImpl.java
    │   │   └── servlet/
    │   │       ├── InicioServlet.java          # @WebServlet("/inicio")
    │   │       ├── RegistroParqueoServlet.java # @WebServlet("/parqueos")
    │   │       ├── ClienteServlet.java         # @WebServlet("/clientes")
    │   │       └── PagoServlet.java            # @WebServlet("/pagos")
    │   ├── resources/
    │   │   ├── db.properties.example           # Plantilla de conexión sin credenciales reales (versionada)
    │   │   └── db.properties                   # Credenciales locales (ignoradas por Git mediante .gitignore)
    │   └── webapp/
    │       ├── index.jsp                       # Página de bienvenida con redirección a /inicio
    │       ├── css/
    │       │   └── estilos.css                 # Hoja de estilos responsiva de Smart Parking
    │       └── WEB-INF/
    │           ├── web.xml                     # Descriptor estándar Jakarta EE 10 (Servlet 6.0)
    │           └── views/
    │               ├── fragments/
    │               │   ├── header.jspf         # Cabecera y barra de navegación común
    │               │   └── footer.jspf         # Pie de página institucional
    │               ├── inicio.jsp              # Panel principal con KPIs en tiempo real
    │               ├── parqueos.jsp            # Formularios POST/GET y listado JSP de parqueos
    │               ├── clientes.jsp            # Formularios POST/GET y listado JSP de clientes
    │               └── pagos.jsp               # Formularios POST/GET y listado JSP de pagos
    └── test/
        └── java/com/sena/smartparking/web/
            ├── dao/
            │   └── ConexionBDTest.java
            └── servlet/
                ├── RegistroParqueoServletTest.java
                ├── ClienteServletTest.java
                └── PagoServletTest.java
```

---

## 6. Descripción de los Modelos (`JavaBeans`)

- **`RegistroParqueo.java`:** Mapea la tabla `registro_parqueo` con los atributos `idRegistro` (`id_registro`), `placa` (`placa`), `horaIngreso` (`hora_ingreso`), `valorCalculado` (`valor_calculado`, tipo `BigDecimal`) y `estadoPago` (`estado_pago`). Incluye el método auxiliar `getHoraIngresoFormateada()` para su presentación en JSP.
- **`Cliente.java`:** Mapea la tabla `cliente` con los atributos `idCliente` (`id_cliente`), `tipoDoc` (`tipo_doc`), `numDoc` (`num_doc`), `nombres` (`nombres`) y `correo` (`correo`).
- **`Pago.java`:** Mapea la tabla `pago` con los atributos `idPago` (`id_pago`), `idRegistro` (`id_registro`, clave foránea hacia `registro_parqueo`), `fechaPago` (`fecha_pago`), `valorPagado` (`valor_pagado`), `estadoPago` (`estado_pago`) y `tiempoLimiteSalida` (`tiempo_limite_salida`). Incluye `getFechaPagoFormateada()`.

---

## 7. Descripción de los DAO (`JDBC`)

Todas las implementaciones DAO utilizan `try-with-resources` para el cierre automático de `Connection`, `PreparedStatement` y `ResultSet`, y consultas parametrizadas con `?` para prevenir inyección SQL:

- **`RegistroParqueoDAO` / `RegistroParqueoDAOImpl`:**
  - `insertar(RegistroParqueo)`: Ejecuta `INSERT INTO registro_parqueo (placa, hora_ingreso, valor_calculado, estado_pago) VALUES (?, ?, ?, ?)` y recupera la llave primaria autogenerada.
  - `buscarPorId(int)`: Consulta un registro puntual por `id_registro`.
  - `listarTodos()`: Retorna todos los registros ordenados por `id_registro DESC`.
  - `buscarPorFiltros(String placa, String estadoPago)`: Filtra dinámicamente por placa (`LIKE ?`) y/o estado de pago (`= ?`).
- **`ClienteDAO` / `ClienteDAOImpl`:**
  - `insertar(Cliente)`: Inserta un nuevo cliente en la tabla `cliente`.
  - `buscarPorDocumento(String tipoDoc, String numDoc)`: Verifica si ya existe un cliente con la misma combinación de tipo y número de documento.
  - `listarTodos()` y `buscarPorFiltros(String tipoDoc, String numDoc)`: Lista todos los clientes o filtra por tipo y número de documento.
- **`PagoDAO` / `PagoDAOImpl`:**
  - `insertar(Pago)`: Inserta un pago asociado a un `id_registro` válido en la tabla `pago`.
  - `listarTodos()` y `listarPorIdRegistro(int idRegistro)`: Lista todos los pagos o filtra aquellos vinculados a un registro de parqueo específico.

---

## 8. Descripción de los Servlets

| Servlet | Ruta (`@WebServlet`) | Responsabilidad en `doGet()` | Responsabilidad en `doPost()` | Vista JSP |
| :--- | :--- | :--- | :--- | :--- |
| `InicioServlet` | `/inicio` | Consulta totales de registros, pendientes, clientes y pagos en MySQL y carga los últimos 5 ingresos. | No aplica (panel de consulta). | `/WEB-INF/views/inicio.jsp` |
| `RegistroParqueoServlet` | `/parqueos` | Captura parámetros `placa` y `estadoPago` por URL, filtra en el DAO y envía `registros` a la vista. | Captura `placa`, `valorCalculado`, `horaIngreso` y `estadoPago`; valida formato de placa y monto no negativo, inserta en MySQL y retorna mensaje de confirmación o validación. | `/WEB-INF/views/parqueos.jsp` |
| `ClienteServlet` | `/clientes` | Captura parámetros `tipoDoc` y `numDoc`, ejecuta la búsqueda en el DAO y envía `clientes` a la vista. | Captura `tipoDoc`, `numDoc`, `nombres` y `correo`; valida campos obligatorios, formato de email y ausencia de duplicados antes de insertar en MySQL. | `/WEB-INF/views/clientes.jsp` |
| `PagoServlet` | `/pagos` | Captura parámetro `idRegistro` para filtrar pagos y carga `registrosDisponibles` para poblar el selector del formulario. | Captura `idRegistro`, `valorPagado`, `fechaPago`, `estadoPago` y `tiempoLimiteSalida`; comprueba que exista el `id_registro` en `registro_parqueo` (clave foránea) e inserta el pago. | `/WEB-INF/views/pagos.jsp` |

---

## 9. Formularios HTML y Manejo de Métodos `GET` y `POST`

Cada uno de los tres módulos cuenta con **dos formularios HTML diferenciados** en su vista JSP:

1. **Formularios con `method="post"` (Creación y Persistencia en MySQL):**
   - En `parqueos.jsp`: `<form method="post" action="${pageContext.request.contextPath}/parqueos">`
   - En `clientes.jsp`: `<form method="post" action="${pageContext.request.contextPath}/clientes">`
   - En `pagos.jsp`: `<form method="post" action="${pageContext.request.contextPath}/pagos">`
   - Envían los datos en el cuerpo de la petición HTTP hacia el método `doPost()` del Servlet respectivo. Si ocurre un error de validación, el Servlet preserva los valores ingresados (`formPlaca`, `formNumDoc`, etc.) para no obligar al usuario a digitar todo de nuevo.
2. **Formularios con `method="get"` (Búsqueda y Filtrado Parametrizado):**
   - En `parqueos.jsp`: `<form method="get" action="${pageContext.request.contextPath}/parqueos">` (parámetros `placa` y `estadoPago`).
   - En `clientes.jsp`: `<form method="get" action="${pageContext.request.contextPath}/clientes">` (parámetros `tipoDoc` y `numDoc`).
   - En `pagos.jsp`: `<form method="get" action="${pageContext.request.contextPath}/pagos">` (parámetro `idRegistro`).
   - Envían los criterios de consulta en la cadena de consulta (`Query String`) hacia el método `doGet()` del Servlet.

---

## 10. Uso de JSP, JSTL y Expression Language (EL)

Las vistas ubicadas en `src/main/webapp/WEB-INF/views/` implementan:
- **Directivas JSP:** `<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>`, `<%@ taglib prefix="c" uri="jakarta.tags.core" %>` e inclusiones estáticas `<%@ include file="fragments/header.jspf" %>` y `<%@ include file="fragments/footer.jspf" %>`.
- **Expression Language (`EL`):** Acceso limpio a propiedades de los JavaBeans (`${r.idRegistro}`, `${r.placa}`, `${r.valorCalculado}`, `${c.nombres}`, `${p.valorPagado}`) y al contexto de la aplicación (`${pageContext.request.contextPath}`).
- **Etiquetas JSTL 3.0 (`jakarta.tags.core`):**
  - `<c:forEach>`: Renderizado dinámico de filas en las tablas de registros de parqueo, clientes y pagos, así como de las opciones `<option>` en el selector de registros de `pagos.jsp`.
  - `<c:if>`: Visualización condicional de alertas de éxito (`mensajeExito`), alertas de error (`mensajeError`) e indicadores de filtro activo.
  - `<c:choose>`, `<c:when>`, `<c:otherwise>`: Asignación de insignias visuales de estado (`PAGADO`, `PENDIENTE`, `APROBADO`, `FINALIZADO`) y manejo de tablas vacías.
  - `<c:out>`: Escape seguro de caracteres HTML para prevenir vulnerabilidades XSS.

---

## 11. Conexión JDBC y Configuración de la Base de Datos MySQL

La clase [`ConexionBD.java`](src/main/java/com/sena/smartparking/web/config/ConexionBD.java) administra la conexión hacia el esquema **`smart_parking`** en MySQL:
1. Prioriza las variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` y `DB_DRIVER` si están definidas en el sistema operativo.
2. En su defecto, lee el archivo `src/main/resources/db.properties` desde el classpath (`WEB-INF/classes/db.properties`).
3. Si ocurre un fallo de conexión, lanza una `SQLException` controlada sin exponer contraseñas ni trazas sensibles al usuario final.

### Pasos para configurar `db.properties`
1. Verificar que el servidor MySQL esté activo en `localhost:3306` y que exista la base de datos `smart_parking` (puede consultarse el archivo de referencia `sql/esquema_smart_parking.sql`).
2. Copiar la plantilla `src/main/resources/db.properties.example` como `src/main/resources/db.properties`:
   ```powershell
   Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
   ```
3. Editar `src/main/resources/db.properties` con el usuario y contraseña local de MySQL:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_parking?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
   db.user=root
   db.password=TU_PASSWORD_AQUI
   db.driver=com.mysql.cj.jdbc.Driver
   ```
   *(El archivo `db.properties` está excluido de Git mediante `**/db.properties` en `.gitignore`).*

---

## 12. Instrucciones de Compilación y Ejecución con Apache Tomcat 10.1

### Compilar, ejecutar pruebas unitarias y empaquetar el archivo WAR
Desde el directorio `web-servlets/`:
```powershell
mvn clean test package
```
Este comando compila el código con Java 21, ejecuta las 19 pruebas unitarias automatizadas y genera el archivo:
```text
web-servlets/target/smart-parking-web.war
```

### Ejecutar en Apache Tomcat 10.1 mediante Maven Cargo (Puerto `8081`)
Desde el directorio `web-servlets/`:
```powershell
mvn clean package cargo:run
```
El plugin Cargo descarga e inicia automáticamente **Apache Tomcat `10.1.30`** en el puerto **`8081`** (evitando cualquier colisión con el puerto `8080` del backend Spring Boot) y despliega el contexto `/smart-parking-web`.

### Dirección Local de Acceso
- **Inicio / Menú Principal:** `http://localhost:8081/smart-parking-web/` (redirige a `/inicio`)
- **Panel de Indicadores:** `http://localhost:8081/smart-parking-web/inicio`
- **Módulo de Registros de Parqueo:** `http://localhost:8081/smart-parking-web/parqueos`
- **Módulo de Clientes:** `http://localhost:8081/smart-parking-web/clientes`
- **Módulo de Pagos:** `http://localhost:8081/smart-parking-web/pagos`

---

## 13. Resultados de Pruebas

### 13.1. Pruebas Unitarias Automatizadas (`JUnit 5` + `Mockito`)
Ejecutadas mediante `mvn clean test package`:
- `ConexionBDTest`: **4 pruebas ejecutadas, 4 aprobadas, 0 fallidas.**
- `RegistroParqueoServletTest`: **6 pruebas ejecutadas, 6 aprobadas, 0 fallidas.**
- `ClienteServletTest`: **5 pruebas ejecutadas, 5 aprobadas, 0 fallidas.**
- `PagoServletTest`: **4 pruebas ejecutadas, 4 aprobadas, 0 fallidas.**
- **Total Módulo `web-servlets`:** **19 pruebas ejecutadas, 19 aprobadas, 0 fallos, 0 errores (`BUILD SUCCESS`).**

### 13.2. Pruebas de Integración HTTP y Persistencia MySQL sobre Tomcat 10.1.30
Se verificó el despliegue real de `smart-parking-web.war` en Apache Tomcat `10.1.30` (puerto `8081`) comprobando respuestas `HTTP 200 OK` en todas las rutas (`GET /inicio`, `GET /parqueos`, `GET /clientes`, `GET /pagos`), el envío de formularios `POST` y la consulta filtrada `GET` contra MySQL `9.7.1`.

### 13.3. Pruebas Manuales en Navegador Realizadas por el Usuario
El funcionamiento integral de la aplicación web fue verificado manualmente por el usuario desde el navegador web interactuando directamente con los tres módulos codificados (**Registros de Parqueo**, **Clientes** y **Pagos**), comprobando la carga visual de las vistas JSP, el envío de formularios HTML (`GET` y `POST`) y la persistencia y consulta de datos en MySQL.
