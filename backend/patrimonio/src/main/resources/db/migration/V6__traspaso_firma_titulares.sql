-- En el vale de traspaso firman los titulares de las áreas (responsables del inventario a su resguardo).
-- Se guardan sus nombres al autorizar para que el vale no cambie si después cambia el titular.
ALTER TABLE solicitud_traspaso ADD COLUMN titular_origen  VARCHAR(200);
ALTER TABLE solicitud_traspaso ADD COLUMN titular_destino VARCHAR(200);

UPDATE solicitud_traspaso s
SET titular_origen  = (SELECT a.nombre_cargo FROM area a WHERE a.id = s.area_origen_id),
    titular_destino = (SELECT a.nombre_cargo FROM area a WHERE a.id = s.area_destino_id)
WHERE s.estatus = 'AUTORIZADA';

-- Quien usará el bien en el área destino ya es opcional (si falta, el resguardo queda a nombre del titular)
ALTER TABLE solicitud_traspaso ALTER COLUMN recibe_nombre DROP NOT NULL;
