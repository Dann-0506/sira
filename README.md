# Sistema de Registro de Resultados Académicos

> **Aviso de Proyecto Académico**
> Este repositorio contiene un proyecto desarrollado con fines puramente académicos y de aprendizaje universitario. No está diseñado para su uso en entornos de producción reales.
>
> **Estado del proyecto:** En desarrollo activo (WIP). El backend (modelos, DAOs, servicios y base de datos) está completo y validado mediante suite de pruebas. La interfaz gráfica (JavaFX) se encuentra en construcción.

Sistema de escritorio para gestionar el ciclo completo de evaluación académica: alumnos, maestros, materias, grupos, actividades, calificaciones y reportes.

---

## Arquitectura

El proyecto sigue una **arquitectura en capas clásica (MVC + DAO)**, organizando el código por tipo de responsabilidad:

```
src/main/java/com/academico/
├── controller/     ← Controladores JavaFX (lógica de UI)
├── dao/            ← Acceso a datos (SQL con JDBC)
├── db/             ← Configuración del pool de conexiones
├── model/          ← POJOs — representación de entidades
├── service/        ← Lógica de negocio y cálculos
└── util/           ← Utilidades transversales
```

### Capas

**Model** — POJOs puros sin dependencias externas. Representan las entidades del dominio: `Alumno`, `Grupo`, `Resultado`, `CalificacionFinal`, etc.

**DAO** — Toda la SQL vive aquí. Cada DAO gestiona las operaciones CRUD de una tabla o conjunto relacionado. Usan HikariCP para obtener conexiones del pool.

**Service** — Lógica de negocio que no pertenece a ninguna tabla específica: cálculo de promedios ponderados, validación de ponderaciones, aplicación de bonus, generación de reportes.

**Controller** — Controladores JavaFX que conectan la UI con los servicios. No contienen SQL ni lógica de negocio.

**Util** — Herramientas transversales: `SessionManager` (sesión activa), `NavegacionUtil` (navegación entre vistas), `CsvUtil` (lectura/escritura CSV), `BackupUtil` (respaldo y restauración de BD).

---

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| UI | JavaFX 21 + AtlantaFX |
| Base de datos | PostgreSQL |
| Gestor de dependencias | Maven |
| Pool de conexiones | HikariCP |
| Variables de entorno | dotenv-java |
| Hasheo de contraseñas | BCrypt |
| Importación CSV | OpenCSV |
| Pruebas | JUnit 5 + Mockito |

---

## Requisitos Previos

- JDK 21 o superior (se recomienda instalar con [SDKMAN](https://sdkman.io/))
- Maven 3.9+
- PostgreSQL 14+

---

## Configuración del Entorno

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd registro-academico
```

### 2. Crear el archivo de variables de entorno

Crea un archivo `.env` en la raíz del proyecto (ya está en `.gitignore`):

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=registro_academico
DB_USER=tu_usuario
DB_PASSWORD=tu_contraseña
```

### 3. Crear la base de datos

```bash
psql -U postgres -c "CREATE DATABASE registro_academico;"
```

El esquema de tablas se aplica automáticamente al arrancar la aplicación por primera vez desde `src/main/resources/com/academico/db/schema.sql`.

### 4. Compilar el proyecto

```bash
mvn compile
```

### 5. Ejecutar la aplicación

```bash
mvn javafx:run
```

### 6. Ejecutar las pruebas

```bash
mvn test
```

---

## Roles del Sistema

El sistema define tres roles con niveles de acceso distintos:

| Rol | Responsabilidad |
|---|---|
| **Administrador** | Gestiona la estructura académica: alumnos, maestros, materias, grupos, inscripciones, configuración y utilerías. |
| **Maestro** | Opera dentro de sus grupos asignados: define actividades, registra calificaciones, aplica bonus y genera reportes. |
| **Alumno** | Consulta sus propios resultados. Solo lectura. *(Implementación futura)* |

Para más detalle ver `docs/roles_permisos.docx`.

---

## Credenciales de Prueba

Para desarrollo local, inserta usuarios de prueba generando los hashes con:

```bash
mvn compile exec:java -Dexec.mainClass="com.academico.MainApp"
# Descomentar temporalmente la generación de hashes en main()
```

---

## Estructura de la Base de Datos

Las tablas principales son:

```
usuario → maestro / alumno
materia → unidad
grupo (materia + maestro) → inscripcion (alumno + grupo)
inscripcion → resultado (calificacion por actividad)
actividad_grupo (grupo + unidad + ponderacion) → resultado
inscripcion → bonus
```

El esquema completo con índices y vistas de cálculo está en `src/main/resources/com/academico/db/schema.sql`.