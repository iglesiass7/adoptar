-- =====================================================================
-- V1 · Esquema inicial de AdoptAR
-- =====================================================================
-- Reglas para trabajar con migraciones:
--   * Una migración que ya está en main NO se edita nunca. Si algo está mal,
--     se corrige con una migración nueva (V2__..., V3__...).
--   * Los "enums" se guardan como texto, igual que @Enumerated(EnumType.STRING).
--     Si agregan un valor a un enum de Java, hay que agregarlo también en el
--     CHECK de la tabla, con una migración nueva.
--   * Todas las tablas tienen created_at y updated_at: las entidades extienden
--     com.adoptar.shared.EntidadAuditada y Spring las completa solas.
--   * Nada se borra físicamente: se da de baja con un estado o un flag.
-- =====================================================================


-- ---------------------------------------------------------------------
-- M1 · Usuarios y acceso
-- ---------------------------------------------------------------------

CREATE TABLE usuarios (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(255)  NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL,
    nombre         VARCHAR(120)  NOT NULL,
    telefono       VARCHAR(30),
    rol            VARCHAR(20)   NOT NULL,
    activo         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_usuarios_email UNIQUE (email),
    -- El mail se guarda siempre en minúsculas: evita "Ana@x.com" y "ana@x.com" duplicados
    CONSTRAINT ck_usuarios_email_minusculas CHECK (email = lower(email)),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADOPTANTE', 'REFUGIO', 'ADMIN'))
);

-- Perfil del adoptante. La fila se crea al completar el perfil (CU-04):
-- "perfil completo" = existe la fila. Es requisito para enviar solicitudes (regla 4).
CREATE TABLE adoptantes (
    usuario_id        BIGINT        PRIMARY KEY REFERENCES usuarios (id),
    fecha_nacimiento  DATE          NOT NULL,
    provincia         VARCHAR(60)   NOT NULL,
    localidad         VARCHAR(80)   NOT NULL,
    tipo_vivienda     VARCHAR(20)   NOT NULL,
    tiene_patio       BOOLEAN       NOT NULL,
    otras_mascotas    BOOLEAN       NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT ck_adoptantes_tipo_vivienda CHECK (tipo_vivienda IN ('CASA', 'DEPARTAMENTO', 'PH', 'OTRO'))
);


-- ---------------------------------------------------------------------
-- M4 · Refugios
-- ---------------------------------------------------------------------

CREATE TABLE refugios (
    usuario_id           BIGINT         PRIMARY KEY REFERENCES usuarios (id),
    razon_social         VARCHAR(150)   NOT NULL,
    cuit                 VARCHAR(13)    NOT NULL,
    descripcion          TEXT,
    direccion            VARCHAR(200),
    provincia            VARCHAR(60)    NOT NULL,
    localidad            VARCHAR(80)    NOT NULL,
    -- Regla 11: solo un refugio VERIFICADO puede publicar mascotas
    estado_verificacion  VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT uq_refugios_cuit UNIQUE (cuit),
    -- Formato con guiones, por ejemplo 30-12345678-9
    CONSTRAINT ck_refugios_cuit_formato CHECK (cuit ~ '^[0-9]{2}-[0-9]{8}-[0-9]$'),
    CONSTRAINT ck_refugios_estado CHECK (estado_verificacion IN ('PENDIENTE', 'VERIFICADO', 'RECHAZADO'))
);


-- ---------------------------------------------------------------------
-- M2 · Catálogo de mascotas
-- ---------------------------------------------------------------------

-- La fecha de publicación de la mascota es created_at.
CREATE TABLE mascotas (
    id               BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    refugio_id       BIGINT         NOT NULL REFERENCES refugios (usuario_id),
    nombre           VARCHAR(80)    NOT NULL,
    especie          VARCHAR(10)    NOT NULL,
    raza             VARCHAR(80),
    sexo             VARCHAR(10)    NOT NULL,
    tamanio          VARCHAR(10)    NOT NULL,
    fecha_nac_aprox  DATE,
    -- Las tres respuestas de salud son obligatorias para publicar (regla 13)
    castrado         BOOLEAN        NOT NULL,
    vacunado         BOOLEAN        NOT NULL,
    desparasitado    BOOLEAN        NOT NULL,
    descripcion      TEXT,
    -- Dónde está la mascota. Puede no coincidir con el refugio (por ejemplo,
    -- si está en un hogar de tránsito).
    provincia        VARCHAR(60)    NOT NULL,
    localidad        VARCHAR(80)    NOT NULL,
    estado           VARCHAR(15)    NOT NULL DEFAULT 'DISPONIBLE',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT ck_mascotas_especie CHECK (especie IN ('PERRO', 'GATO', 'OTRO')),
    CONSTRAINT ck_mascotas_sexo    CHECK (sexo IN ('MACHO', 'HEMBRA')),
    CONSTRAINT ck_mascotas_tamanio CHECK (tamanio IN ('CHICO', 'MEDIANO', 'GRANDE')),
    CONSTRAINT ck_mascotas_estado  CHECK (estado IN ('DISPONIBLE', 'RESERVADA', 'ADOPTADA', 'NO_DISPONIBLE'))
);

CREATE TABLE fotos_mascota (
    id          BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mascota_id  BIGINT         NOT NULL REFERENCES mascotas (id) ON DELETE CASCADE,
    -- Se guarda la URL de la imagen, nunca los bytes
    url         VARCHAR(500)   NOT NULL,
    orden       SMALLINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------
-- M3 · Solicitudes de adopción (el núcleo)
-- ---------------------------------------------------------------------

-- Cuestionario propio de cada refugio (CU-12).
-- Las preguntas no se borran: se desactivan, porque las respuestas viejas las referencian.
CREATE TABLE preguntas (
    id           BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    refugio_id   BIGINT         NOT NULL REFERENCES refugios (usuario_id),
    texto        VARCHAR(300)   NOT NULL,
    tipo         VARCHAR(10)    NOT NULL,
    obligatoria  BOOLEAN        NOT NULL DEFAULT TRUE,
    orden        SMALLINT       NOT NULL DEFAULT 0,
    activa       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT ck_preguntas_tipo CHECK (tipo IN ('TEXTO', 'SI_NO', 'NUMERO'))
);

-- La fecha de envío de la solicitud es created_at.
CREATE TABLE solicitudes (
    id              BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mascota_id      BIGINT         NOT NULL REFERENCES mascotas (id),
    adoptante_id    BIGINT         NOT NULL REFERENCES adoptantes (usuario_id),
    estado          VARCHAR(15)    NOT NULL DEFAULT 'ENVIADA',
    motivo_rechazo  VARCHAR(500),
    fecha_decision  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT ck_solicitudes_estado CHECK (estado IN (
        'ENVIADA', 'EN_REVISION', 'ENTREVISTA', 'APROBADA',
        'COMPLETADA', 'RECHAZADA', 'CANCELADA', 'EXPIRADA')),
    -- Regla 10: un rechazo siempre lleva motivo (y no puede ser un texto vacío)
    CONSTRAINT ck_solicitudes_rechazo_con_motivo CHECK (
        estado <> 'RECHAZADA'
        OR (motivo_rechazo IS NOT NULL AND length(trim(motivo_rechazo)) > 0))
);

-- Regla 2, también en la base: un adoptante no puede tener dos solicitudes activas
-- por la misma mascota. La regla vive en el dominio (para dar un buen mensaje de error);
-- este índice la hace imposible de violar aunque lleguen dos clicks a la vez.
CREATE UNIQUE INDEX uq_solicitud_activa_por_mascota
    ON solicitudes (adoptante_id, mascota_id)
    WHERE estado IN ('ENVIADA', 'EN_REVISION', 'ENTREVISTA', 'APROBADA');

-- Regla 7, también en la base: una mascota no puede tener dos solicitudes aprobadas a la vez.
CREATE UNIQUE INDEX uq_una_aprobada_por_mascota
    ON solicitudes (mascota_id)
    WHERE estado = 'APROBADA';

CREATE TABLE respuestas (
    id            BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id  BIGINT        NOT NULL REFERENCES solicitudes (id) ON DELETE CASCADE,
    pregunta_id   BIGINT        NOT NULL REFERENCES preguntas (id),
    valor         TEXT          NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_respuestas_pregunta UNIQUE (solicitud_id, pregunta_id)
);

-- Encuentro previo a la entrega: 0 o 1 por solicitud.
CREATE TABLE entrevistas (
    id            BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id  BIGINT        NOT NULL REFERENCES solicitudes (id),
    fecha         TIMESTAMPTZ   NOT NULL,
    modalidad     VARCHAR(15)   NOT NULL,
    notas         TEXT,
    -- Queda vacío hasta que la entrevista se hace
    resultado     VARCHAR(15),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_entrevistas_solicitud UNIQUE (solicitud_id),
    CONSTRAINT ck_entrevistas_modalidad CHECK (modalidad IN ('PRESENCIAL', 'VIDEOLLAMADA', 'TELEFONICA')),
    CONSTRAINT ck_entrevistas_resultado CHECK (resultado IN ('FAVORABLE', 'DESFAVORABLE'))
);

-- Registro de la entrega concretada (CU-20). Se crea recién al completar la adopción.
CREATE TABLE adopciones (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id   BIGINT        NOT NULL REFERENCES solicitudes (id),
    fecha_entrega  DATE          NOT NULL,
    observaciones  TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_adopciones_solicitud UNIQUE (solicitud_id)
);


-- ---------------------------------------------------------------------
-- M5 · Notificaciones
-- ---------------------------------------------------------------------

CREATE TABLE notificaciones (
    id          BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT         NOT NULL REFERENCES usuarios (id),
    tipo        VARCHAR(40)    NOT NULL,
    mensaje     VARCHAR(500)   NOT NULL,
    leida       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------
-- Índices: solo donde realmente se busca. Cada índice hace más lenta cada escritura.
-- (Las restricciones UNIQUE de arriba ya crean su propio índice.)
-- ---------------------------------------------------------------------

-- Filtro principal del catálogo: GET /mascotas
CREATE INDEX ix_mascotas_busqueda ON mascotas (estado, especie, provincia);
-- "Las mascotas de este refugio": panel del refugio y perfil público
CREATE INDEX ix_mascotas_refugio ON mascotas (refugio_id);
-- Las fotos de una ficha
CREATE INDEX ix_fotos_mascota ON fotos_mascota (mascota_id);
-- El cuestionario de un refugio
CREATE INDEX ix_preguntas_refugio ON preguntas (refugio_id);
-- Buscar las solicitudes competidoras al aprobar una (CU-18)
CREATE INDEX ix_solicitudes_mascota_estado ON solicitudes (mascota_id, estado);
-- "Mis solicitudes" y contar las activas de un adoptante (regla 3)
CREATE INDEX ix_solicitudes_adoptante_estado ON solicitudes (adoptante_id, estado);
-- Notificaciones de un usuario, las no leídas y más nuevas primero
CREATE INDEX ix_notificaciones_usuario ON notificaciones (usuario_id, leida, created_at DESC);
