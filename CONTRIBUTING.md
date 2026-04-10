# Guía de Contribución

Esta guía explica cómo trabajar con Git en este proyecto, tanto desde la terminal como desde VSCode. Está pensada para quienes están aprendiendo el flujo de trabajo con ramas. 

**NOTA: Ignoren lo de las convenciones en los nombres y eso, fue solo una sugerencia de Claude por ser el estandar. Pero el flujo si es así. Cualquier problema solo avisenme. ATTE. Daniel:)**
---

## ¿Por qué usamos ramas?

Cuando varias personas trabajan en el mismo proyecto, no es seguro que todos hagan cambios directamente sobre `main`. Si alguien sube código con errores, rompe el proyecto para todos.

La solución es que cada persona trabaje en su propia **rama** — una copia paralela del código donde puede hacer cambios sin afectar a nadie más. Cuando el trabajo está listo y revisado, se une (`merge`) a `main`.

```
main ────────────────────────────────────► (código estable)
         │
         └── feature/vista-alumnos ──────► (tu trabajo aislado)
```

---

## Estructura de ramas

| Rama | Propósito |
|---|---|
| `main` | Código estable. Nadie trabaja directamente aquí. |
| `feature/nombre` | Nueva funcionalidad. |
| `fix/nombre` | Corrección de un bug. |
| `refactor/nombre` | Reestructuración sin cambio de comportamiento. |

**Ejemplos de nombres válidos:**
```
feature/vista-alumnos
feature/importacion-csv
fix/left-join-inscripcion
refactor/navegacion-util
```

---

## Flujo de trabajo completo

Cada vez que vayas a trabajar en algo nuevo, sigue estos pasos en orden.

### Paso 1 — Asegúrate de tener `main` actualizado

Antes de crear una rama, sincroniza tu copia local con lo que hay en el repositorio remoto.

**Desde la terminal:**
```bash
git checkout main
git pull
```

**Desde VSCode:**
1. En la esquina inferior izquierda verás el nombre de la rama actual. Haz clic ahí.
2. Selecciona `main` en la lista que aparece arriba.
3. Abre la paleta de comandos con `Ctrl+Shift+P`.
4. Escribe `Git: Pull` y presiona Enter.

---

### Paso 2 — Crea tu rama

**Desde la terminal:**
```bash
git checkout -b feature/nombre-de-tu-tarea
```

El flag `-b` crea la rama y cambia a ella en un solo comando.

**Desde VSCode:**
1. Haz clic en el nombre de la rama en la esquina inferior izquierda.
2. Selecciona `Crear nueva rama...` en el menú que aparece.
3. Escribe el nombre de la rama: `feature/nombre-de-tu-tarea`.
4. Presiona Enter. VSCode crea la rama y cambia a ella automáticamente.

Puedes confirmar en qué rama estás mirando la esquina inferior izquierda de VSCode o ejecutando:
```bash
git branch
```
La rama activa tiene un `*` al lado.

---

### Paso 3 — Trabaja en tu tarea

Edita, crea o elimina archivos con normalidad. Mientras estés en tu rama, tus cambios no afectan a nadie más.

---

### Paso 4 — Guarda tus cambios (commit)

Un commit es una fotografía del estado de tu código en un momento determinado. Se recomienda hacer commits frecuentes y pequeños — no esperar a tener todo listo para hacer uno solo.

**Desde la terminal:**
```bash
# Ver qué archivos cambiaste
git status

# Agregar los archivos que quieres incluir en el commit
git add src/main/java/com/academico/controller/AlumnosController.java
git add src/main/resources/com/academico/fxml/alumnos.fxml

# O agregar todos los cambios de una vez
git add .

# Crear el commit con un mensaje descriptivo
git commit -m "feat: agrega vista de gestión de alumnos"
```

**Desde VSCode:**
1. Abre el panel de Control de Código Fuente con `Ctrl+Shift+G`.
2. Verás la lista de archivos modificados bajo `Cambios`.
3. Pasa el cursor sobre cada archivo y haz clic en `+` para agregarlo al área de staging. O haz clic en `+` junto a `Cambios` para agregar todos.
4. Los archivos agregados aparecen bajo `Cambios preparados`.
5. Escribe el mensaje del commit en el campo de texto arriba.
6. Haz clic en el botón `Confirmar` (o presiona `Ctrl+Enter`).

---

### Paso 5 — Sube tu rama al repositorio remoto

La primera vez que subes una rama nueva necesitas indicarle a Git dónde publicarla.

**Desde la terminal:**
```bash
# Primera vez que subes esta rama
git push -u origin feature/nombre-de-tu-tarea

# Las veces siguientes, con esto es suficiente
git push
```

**Desde VSCode:**
1. En el panel `Ctrl+Shift+G`, haz clic en los `...` (menú de opciones).
2. Selecciona `Insertar` (Push).
3. Si es la primera vez, VSCode te preguntará si quieres publicar la rama — acepta.

---

### Paso 6 — Une tu rama a `main` (merge)

Cuando tu tarea esté terminada y los tests pasen, es momento de unir tu trabajo a `main`.

**Desde la terminal:**
```bash
# Primero asegúrate de que los tests pasan
mvn test

# Cambia a main
git checkout main

# Actualiza main por si hubo cambios mientras trabajabas
git pull

# Une tu rama
git merge feature/nombre-de-tu-tarea

# Sube main actualizado
git push
```

**Desde VSCode:**
1. Cambia a `main` haciendo clic en la rama en la esquina inferior izquierda.
2. Abre la paleta de comandos `Ctrl+Shift+P`.
3. Escribe `Git: Merge Branch` y presiona Enter.
4. Selecciona tu rama `feature/nombre-de-tu-tarea` de la lista.
5. Haz clic en `Sincronizar cambios` en la barra inferior para hacer push.

---

### Paso 7 — Elimina la rama (opcional pero recomendado)

Una vez que tu rama fue unida a `main`, ya no la necesitas. Eliminarla mantiene el repositorio limpio.

**Desde la terminal:**
```bash
# Eliminar rama local
git branch -d feature/nombre-de-tu-tarea

# Eliminar rama remota
git push origin --delete feature/nombre-de-tu-tarea
```

**Desde VSCode:**
1. Haz clic en el nombre de la rama en la esquina inferior izquierda.
2. En la lista que aparece, busca tu rama.
3. Haz clic en el ícono de papelera que aparece al pasar el cursor.

---

## Resolución de conflictos

Un conflicto ocurre cuando dos personas modificaron el mismo archivo en el mismo lugar. Git no sabe cuál versión conservar y te pide que decidas.

Cuando hay un conflicto, el archivo afectado se ve así:

```
<<<<<< HEAD (tu rama actual — main)
    private String nombre;
=======
    private String nombreCompleto;
>>>>>> feature/vista-alumnos (la rama que estás uniendo)
```

**Cómo resolverlo:**

1. Abre el archivo con conflicto. VSCode lo resalta en rojo en el panel de control de código fuente.
2. VSCode muestra botones encima del conflicto:
   - `Aceptar cambio actual` — conserva lo que está en `main`
   - `Aceptar cambio entrante` — conserva lo que viene de tu rama
   - `Aceptar ambos cambios` — conserva las dos versiones
3. Elige la opción correcta o edita manualmente el resultado final.
4. Guarda el archivo.
5. Haz un commit para cerrar el conflicto:

```bash
git add .
git commit -m "fix: resuelve conflicto en modelo Alumno"
```

---

## Convenciones de commits

El mensaje de un commit debe describir **qué hace** el cambio, no cómo lo hace.

```
tipo: descripción corta en español, imperativo, minúsculas
```

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `refactor` | Cambio de código sin cambio de comportamiento |
| `style` | Formato, espaciado, renombrado |
| `test` | Agregar o modificar tests |
| `docs` | Cambios en documentación |
| `chore` | Dependencias, configuración |

**Ejemplos:**
```bash
git commit -m "feat: agrega importación de alumnos por CSV"
git commit -m "fix: corrige LEFT JOIN faltante en InscripcionDAO"
git commit -m "refactor: extrae navegación a NavegacionUtil"
git commit -m "test: agrega casos límite a CalificacionServiceTest"
```

**Reglas:**
- Descripción en español, minúsculas, sin punto al final
- Máximo 72 caracteres
- Si necesitas más contexto, agrega un cuerpo separado por una línea en blanco:

```bash
git commit -m "fix: corrige cálculo de promedio con actividades nulas

Antes las actividades sin calificación se ignoraban del conteo total,
lo que inflaba el promedio. Ahora se cuentan pero aportan cero."
```

---

## Checklist antes de hacer merge a main

- [ ] El código compila sin errores (`mvn compile`)
- [ ] Todos los tests pasan (`mvn test`)
- [ ] No hay credenciales ni contraseñas en el código
- [ ] El mensaje de commit describe claramente el cambio
- [ ] `main` está actualizado antes del merge (`git pull`)

---

## Comandos de referencia rápida

```bash
# Ver en qué rama estás y qué cambios tienes
git status

# Ver el historial de commits
git log --oneline

# Ver todas las ramas (locales y remotas)
git branch -a

# Descartar cambios en un archivo (¡cuidado, es irreversible!)
git checkout -- nombre-del-archivo.java

# Ver qué cambió en un archivo antes de hacer commit
git diff nombre-del-archivo.java
```