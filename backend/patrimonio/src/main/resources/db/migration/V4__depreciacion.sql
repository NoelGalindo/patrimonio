-- Depreciación según los "Parámetros de Estimación de Vida Útil" del CONAC (reforma DOF 10-12-2025).
-- El % anual no se guarda: se calcula como 100 / vida útil para que la depreciación llegue exacta al 100 %.

CREATE TABLE cuenta_contable (
    id               BIGSERIAL PRIMARY KEY,
    clave            VARCHAR(20)  NOT NULL UNIQUE,
    concepto         VARCHAR(200) NOT NULL,
    tipo_bien        VARCHAR(20)  NOT NULL CHECK (tipo_bien IN ('MUEBLE', 'INMUEBLE', 'VEHICULO')),
    -- NULL = sin vida útil definida (no se puede asignar a un bien hasta definirla)
    vida_util_anios  INT CHECK (vida_util_anios > 0),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO cuenta_contable (clave, concepto, tipo_bien, vida_util_anios) VALUES
    -- 1.2.3 Bienes inmuebles, infraestructura y construcciones en proceso (se deprecia solo la construcción)
    ('1.2.3.2',   'Viviendas', 'INMUEBLE', 50),
    ('1.2.3.3',   'Edificios No Habitacionales', 'INMUEBLE', 30),
    ('1.2.3.4',   'Infraestructura', 'INMUEBLE', 25),
    ('1.2.3.9',   'Otros Bienes Inmuebles', 'INMUEBLE', 20),
    -- 1.2.4.1 Mobiliario y equipo de administración
    ('1.2.4.1.1', 'Muebles de Oficina y Estantería', 'MUEBLE', 10),
    ('1.2.4.1.2', 'Muebles, Excepto de Oficina y Estantería', 'MUEBLE', 10),
    ('1.2.4.1.3', 'Equipo de Cómputo y de Tecnologías de la Información', 'MUEBLE', 3),
    ('1.2.4.1.9', 'Otros Mobiliarios y Equipos de Administración', 'MUEBLE', 10),
    -- 1.2.4.2 Mobiliario y equipo educacional y recreativo
    ('1.2.4.2.1', 'Equipos y Aparatos Audiovisuales', 'MUEBLE', 3),
    ('1.2.4.2.2', 'Aparatos Deportivos', 'MUEBLE', 5),
    ('1.2.4.2.3', 'Cámaras Fotográficas y de Video', 'MUEBLE', 3),
    ('1.2.4.2.9', 'Otro Mobiliario y Equipo Educacional y Recreativo', 'MUEBLE', 5),
    -- 1.2.4.3 Equipo e instrumental médico y de laboratorio
    ('1.2.4.3.1', 'Equipo Médico y de Laboratorio', 'MUEBLE', 5),
    ('1.2.4.3.2', 'Instrumental Médico y de Laboratorio', 'MUEBLE', 5),
    -- 1.2.4.4 Equipo de transporte
    ('1.2.4.4.1', 'Automóviles y Equipo Terrestre', 'VEHICULO', 5),
    ('1.2.4.4.2', 'Carrocerías y Remolques', 'VEHICULO', 5),
    ('1.2.4.4.3', 'Equipo Aeroespacial', 'VEHICULO', 5),
    ('1.2.4.4.4', 'Equipo Ferroviario', 'VEHICULO', 5),
    ('1.2.4.4.5', 'Embarcaciones', 'VEHICULO', 5),
    ('1.2.4.4.9', 'Otros Equipos de Transporte', 'VEHICULO', 5),
    -- 1.2.4.5 Equipo de defensa y seguridad: la Guía no fija vida útil ("de acuerdo a las características")
    ('1.2.4.5',   'Equipo de Defensa y Seguridad', 'MUEBLE', NULL),
    -- 1.2.4.6 Maquinaria, otros equipos y herramientas
    ('1.2.4.6.1', 'Maquinaria y Equipo Agropecuario', 'MUEBLE', 10),
    ('1.2.4.6.2', 'Maquinaria y Equipo Industrial', 'MUEBLE', 10),
    ('1.2.4.6.3', 'Maquinaria y Equipo de Construcción', 'MUEBLE', 10),
    ('1.2.4.6.4', 'Sistemas de Aire Acondicionado, Calefacción y de Refrigeración Industrial y Comercial', 'MUEBLE', 10),
    ('1.2.4.6.5', 'Equipo de Comunicación y Telecomunicación', 'MUEBLE', 10),
    ('1.2.4.6.6', 'Equipos de Generación Eléctrica, Aparatos y Accesorios Eléctricos', 'MUEBLE', 10),
    ('1.2.4.6.7', 'Herramientas y Máquinas-Herramienta', 'MUEBLE', 10),
    ('1.2.4.6.9', 'Otros Equipos', 'MUEBLE', 10),
    -- 1.2.4.8 Activos biológicos
    ('1.2.4.8.1', 'Bovinos', 'MUEBLE', 5),
    ('1.2.4.8.2', 'Porcinos', 'MUEBLE', 5),
    ('1.2.4.8.3', 'Aves', 'MUEBLE', 5),
    ('1.2.4.8.4', 'Ovinos y Caprinos', 'MUEBLE', 5),
    ('1.2.4.8.5', 'Peces y Acuicultura', 'MUEBLE', 5),
    ('1.2.4.8.6', 'Equinos', 'MUEBLE', 5),
    ('1.2.4.8.7', 'Especies Menores y de Zoológico', 'MUEBLE', 5),
    ('1.2.4.8.8', 'Árboles y Plantas', 'MUEBLE', 5),
    ('1.2.4.8.9', 'Otros Activos Biológicos', 'MUEBLE', 5);

-- Cada bien se liga a su cuenta (obligatoria al capturar; nullable aquí por compatibilidad)
ALTER TABLE bien_mueble   ADD COLUMN cuenta_contable_id BIGINT REFERENCES cuenta_contable (id);
ALTER TABLE bien_inmueble ADD COLUMN cuenta_contable_id BIGINT REFERENCES cuenta_contable (id);
ALTER TABLE vehiculo      ADD COLUMN cuenta_contable_id BIGINT REFERENCES cuenta_contable (id);

-- El valor depreciado ahora se calcula; ya no se captura
ALTER TABLE vehiculo DROP COLUMN valor_depreciado;

INSERT INTO parametro_sistema (clave, valor, descripcion) VALUES
    ('depreciacion.inicio', 'ANIO_SIGUIENTE', 'Regla de inicio de la depreciación');
