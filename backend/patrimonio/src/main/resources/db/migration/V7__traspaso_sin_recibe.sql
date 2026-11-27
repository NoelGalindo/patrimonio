-- En el traspaso solo intervienen los titulares de las áreas: ya no se captura a quien recibe.
ALTER TABLE solicitud_traspaso DROP COLUMN recibe_nombre;
ALTER TABLE solicitud_traspaso DROP COLUMN recibe_cargo;
