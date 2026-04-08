-- ============================================================
-- Sistema de Registro y Cálculo de Resultados Académicos
-- ============================================================

CREATE TABLE IF NOT EXISTS configuracion (
    clave       VARCHAR(50)  PRIMARY KEY,
    valor       VARCHAR(100) NOT NULL,
    descripcion TEXT
);

INSERT INTO configuracion (clave, valor, descripcion) 
VALUES
    ('calificacion_minima_aprobatoria', '70',  'Calificación mínima para aprobar'),
    ('calificacion_maxima',            '100', 'Calificación máxima permitida')
ON CONFLICT (clave) DO NOTHING;

CREATE TABLE IF NOT EXISTS usuario (
    id            SERIAL       PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL CHECK (rol IN ('admin', 'maestro', 'alumno')),
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS maestro (
    id           SERIAL      PRIMARY KEY,
    usuario_id   INT         NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    num_empleado VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS alumno (
    id         SERIAL      PRIMARY KEY,
    usuario_id INT         UNIQUE REFERENCES usuario(id) ON DELETE SET NULL,
    matricula  VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS materia (
    id             SERIAL       PRIMARY KEY,
    clave          VARCHAR(20)  NOT NULL UNIQUE,
    nombre         VARCHAR(150) NOT NULL,
    total_unidades INT          NOT NULL CHECK (total_unidades > 0)
);

CREATE TABLE IF NOT EXISTS unidad (
    id     SERIAL       PRIMARY KEY,
    numero INT          NOT NULL CHECK (numero > 0),
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS grupo (
    id         SERIAL      PRIMARY KEY,
    materia_id INT         NOT NULL REFERENCES materia(id)  ON DELETE RESTRICT,
    maestro_id INT         NOT NULL REFERENCES maestro(id)  ON DELETE RESTRICT,
    clave      VARCHAR(20) NOT NULL UNIQUE,
    semestre   VARCHAR(20) NOT NULL,
    activo     BOOLEAN     NOT NULL DEFAULT TRUE,
    creado_en  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inscripcion (
    id                          SERIAL       PRIMARY KEY,
    alumno_id                   INT          NOT NULL REFERENCES alumno(id)  ON DELETE RESTRICT,
    grupo_id                    INT          NOT NULL REFERENCES grupo(id)   ON DELETE RESTRICT,
    fecha                       DATE         NOT NULL DEFAULT CURRENT_DATE,
    calificacion_final_override DECIMAL(5,2) CHECK (calificacion_final_override BETWEEN 0 AND 100),
    override_justificacion      TEXT,
    UNIQUE (alumno_id, grupo_id)
);

CREATE TABLE IF NOT EXISTS actividad_grupo (
    id          SERIAL        PRIMARY KEY,
    grupo_id    INT           NOT NULL REFERENCES grupo(id)  ON DELETE CASCADE,
    unidad_id   INT           NOT NULL REFERENCES unidad(id) ON DELETE RESTRICT,
    nombre      VARCHAR(150)  NOT NULL,
    ponderacion DECIMAL(5,2)  NOT NULL CHECK (ponderacion > 0 AND ponderacion <= 100),
    creado_en   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resultado (
    id                 SERIAL       PRIMARY KEY,
    inscripcion_id     INT          NOT NULL REFERENCES inscripcion(id)     ON DELETE CASCADE,
    actividad_grupo_id INT          NOT NULL REFERENCES actividad_grupo(id) ON DELETE CASCADE,
    calificacion       DECIMAL(5,2) CHECK (calificacion BETWEEN 0 AND 100),
    modificado_en      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (inscripcion_id, actividad_grupo_id)
);

CREATE TABLE IF NOT EXISTS bonus (
    id             SERIAL       PRIMARY KEY,
    inscripcion_id INT          NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    unidad_id      INT          REFERENCES unidad(id) ON DELETE RESTRICT,
    tipo           VARCHAR(10)  NOT NULL CHECK (tipo IN ('unidad', 'materia')),
    puntos         DECIMAL(5,2) NOT NULL CHECK (puntos > 0),
    justificacion  TEXT,
    otorgado_en    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS estado_unidad (
    id SERIAL PRIMARY KEY,
    grupo_id INT NOT NULL REFERENCES grupo(id) ON DELETE CASCADE,
    unidad_id INT NOT NULL REFERENCES unidad(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA', 'CERRADA')),
    actualizado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (grupo_id, unidad_id)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_grupo_materia        ON grupo           (materia_id);
CREATE INDEX IF NOT EXISTS idx_grupo_maestro        ON grupo           (maestro_id);
CREATE INDEX IF NOT EXISTS idx_inscripcion_grupo    ON inscripcion     (grupo_id);
CREATE INDEX IF NOT EXISTS idx_inscripcion_alumno   ON inscripcion     (alumno_id);
CREATE INDEX IF NOT EXISTS idx_actividad_grupo      ON actividad_grupo (grupo_id, unidad_id);
CREATE INDEX IF NOT EXISTS idx_resultado_insc       ON resultado       (inscripcion_id);
CREATE INDEX IF NOT EXISTS idx_resultado_act        ON resultado       (actividad_grupo_id);
CREATE INDEX IF NOT EXISTS idx_bonus_insc           ON bonus           (inscripcion_id);
CREATE INDEX IF NOT EXISTS idx_bonus_unidad         ON bonus           (unidad_id);
CREATE INDEX IF NOT EXISTS idx_estado_unidad        ON estado_unidad   (grupo_id, unidad_id);