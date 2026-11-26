-- Catálogos de estructura municipal, roles y usuarios

CREATE TABLE direccion_general (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE area (
    id                   BIGSERIAL PRIMARY KEY,
    nombre               VARCHAR(200) NOT NULL,
    nombre_cargo         VARCHAR(200),
    direccion_general_id BIGINT NOT NULL REFERENCES direccion_general (id),
    CONSTRAINT uk_area_nombre_direccion UNIQUE (nombre, direccion_general_id)
);

CREATE TABLE rol (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('PATRIMONIO'), ('USUARIO');

CREATE TABLE usuario (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    apellido_paterno  VARCHAR(100) NOT NULL,
    apellido_materno  VARCHAR(100),
    correo            VARCHAR(150) NOT NULL,
    password_hash     VARCHAR(100) NOT NULL,
    rol_id            BIGINT NOT NULL REFERENCES rol (id),
    area_id           BIGINT REFERENCES area (id),
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_usuario_correo ON usuario (LOWER(correo));
CREATE INDEX ix_usuario_area ON usuario (area_id);
