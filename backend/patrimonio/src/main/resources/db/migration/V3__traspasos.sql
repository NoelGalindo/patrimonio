-- Solicitudes de traspaso de bienes muebles entre áreas y datos del vale

CREATE TABLE solicitud_traspaso (
    id                     BIGSERIAL PRIMARY KEY,
    folio                  INTEGER,
    anio_folio             INTEGER,
    oficio_peticion        VARCHAR(100),
    area_origen_id         BIGINT NOT NULL REFERENCES area (id),
    area_destino_id        BIGINT NOT NULL REFERENCES area (id),
    solicitante_id         BIGINT NOT NULL REFERENCES usuario (id),
    motivo                 TEXT,
    recibe_nombre          VARCHAR(200) NOT NULL,
    recibe_cargo           VARCHAR(200),
    estatus                VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud        TIMESTAMPTZ NOT NULL DEFAULT now(),
    resuelto_por_id        BIGINT REFERENCES usuario (id),
    fecha_resolucion       TIMESTAMPTZ,
    comentario_resolucion  TEXT,
    CONSTRAINT ck_traspaso_estatus CHECK (estatus IN ('PENDIENTE', 'AUTORIZADA', 'RECHAZADA', 'CANCELADA')),
    CONSTRAINT ck_traspaso_areas CHECK (area_origen_id <> area_destino_id),
    CONSTRAINT uk_traspaso_folio UNIQUE (anio_folio, folio)
);
CREATE INDEX ix_traspaso_estatus ON solicitud_traspaso (estatus);
CREATE INDEX ix_traspaso_origen ON solicitud_traspaso (area_origen_id);
CREATE INDEX ix_traspaso_destino ON solicitud_traspaso (area_destino_id);

-- Un renglón por bien. Las columnas snap_* congelan los datos del bien al autorizar,
-- para que el vale pueda regenerarse idéntico aunque el bien cambie después.
CREATE TABLE solicitud_traspaso_detalle (
    id                      BIGSERIAL PRIMARY KEY,
    solicitud_id            BIGINT NOT NULL REFERENCES solicitud_traspaso (id) ON DELETE CASCADE,
    bien_mueble_id          BIGINT NOT NULL REFERENCES bien_mueble (id),
    snap_no_inventario      VARCHAR(60),
    snap_descripcion        VARCHAR(300),
    snap_marca              VARCHAR(120),
    snap_modelo             VARCHAR(120),
    snap_serie              VARCHAR(120),
    snap_especificaciones   TEXT,
    snap_estado             VARCHAR(60),
    snap_costo              NUMERIC(14, 2),
    snap_documento_soporte  VARCHAR(60),
    snap_no_factura         VARCHAR(60),
    snap_expedida_por       VARCHAR(200),
    snap_fecha_factura      DATE,
    snap_entrega_nombre     VARCHAR(200),
    snap_entrega_cargo      VARCHAR(200),
    CONSTRAINT uk_traspaso_detalle UNIQUE (solicitud_id, bien_mueble_id)
);
CREATE INDEX ix_traspaso_detalle_bien ON solicitud_traspaso_detalle (bien_mueble_id);

-- Consecutivo de folios de vale por año
CREATE TABLE folio_consecutivo (
    anio    INTEGER PRIMARY KEY,
    ultimo  INTEGER NOT NULL
);

-- Parámetros editables por el administrador (firmantes del vale)
CREATE TABLE parametro_sistema (
    clave        VARCHAR(100) PRIMARY KEY,
    valor        VARCHAR(500),
    descripcion  VARCHAR(250)
);

INSERT INTO parametro_sistema (clave, valor, descripcion) VALUES
    ('firma.patrimonio.nombre',   'LIC. MARISOL CHAVARRIA MALDONADO',          'Nombre de quien autoriza (Patrimonio)'),
    ('firma.patrimonio.cargo',    'DIRECTORA DE PATRIMONIO MUNICIPAL',          'Cargo de quien autoriza (Patrimonio)'),
    ('firma.contabilidad.nombre', 'C.P. JULIETA MONTES DÍAS',                   'Nombre de quien firma por Contabilidad'),
    ('firma.contabilidad.cargo',  'DIRECTORA DE CONTABILIDAD Y CUENTA PÚBLICA', 'Cargo de quien firma por Contabilidad');
