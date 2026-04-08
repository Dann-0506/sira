# Sistema de Registro Académico

> **Aviso de Proyecto Académico y Estado Actual**
> Este repositorio contiene un proyecto desarrollado con fines puramente académicos y de aprendizaje universitario. No está diseñado para su uso en entornos reales. 
> 
> **Estado del proyecto:** Actualmente se encuentra en fase de desarrollo (WIP). La lógica de negocio (Backend) y la base de datos están estructuradas, pero las vistas (UI) y los controladores de la aplicación aún no están implementados. Por lo tanto, el proyecto **aún no se puede ejecutar como una aplicación independiente**, pero sus módulos pueden ser validados mediante la suite de pruebas.

Sistema de gestión para administrar materias, grupos, alumnos, maestros y calificaciones.

## Arquitectura

El proyecto utiliza una **Arquitectura Basada en Funcionalidades (Feature-based)**. En lugar de organizar el código por tipo de archivo (Modelos, Vistas, Controladores), se organiza por el dominio del negocio. Esto facilita la mantenibilidad y el crecimiento del sistema.

### Estructura de Paquetes
* `core/`: Configuración global, utilidades (`CsvUtil`, `BackupUtil`) y conexión a base de datos.
* `auth/`: Gestión de usuarios, roles, contraseñas y autenticación.
* `academia/`: Estructura del curso (Materias, Grupos, Unidades y Maestros).
* `inscripciones/`: Gestión de alumnos y su asignación a los grupos (incluye importación masiva).
* `calificaciones/`: El corazón del sistema para registrar notas, aplicar bonus, calcular promedios y generar reportes finales.

## Tecnologías

* **Lenguaje:** Java 21
* **Gestor de dependencias:** Maven
* **Base de Datos:** PostgreSQL
* **Bibliotecas principales:**
  * `HikariCP` (Pool de conexiones a DB)
  * `dotenv-java` (Manejo de variables de entorno)
  * `BCrypt` (Hasheo de contraseñas de usuarios)
  * `OpenCSV` (Lectura e importación de datos masivos)
  * `JUnit 5` & `Mockito` (Pruebas unitarias)

## Configuración del Entorno

Aunque la aplicación no tiene una interfaz gráfica ejecutable en este momento, puedes configurar el entorno para correr las pruebas unitarias o continuar con el desarrollo.

### 1. Prerrequisitos
* Tener instalado **JDK 21** o superior.
* Tener instalado **Maven**.
* Tener una instancia de **PostgreSQL** corriendo.

### 2. Variables de Entorno
Crea un archivo llamado `.env` en la raíz del proyecto (este archivo está ignorado en Git por seguridad) y configura tus credenciales de base de datos:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=registro_academico
DB_USER=tu_usuario
DB_PASSWORD=tu_contraseña
```

### 3. Base de Datos
El script para generar la estructura de las tablas se encuentra en `src/main/resources/com/academico/core/db/schema.sql`.


## Pruebas Unitarias
Para validar la integridad del sistema y las reglas de negocio se verifican mediante la suite de pruebas automatizadas.
Para descargar las dependencias y ejecutar los test, abre tu terminal y ejecuta.

```bash
mvn clean test
```