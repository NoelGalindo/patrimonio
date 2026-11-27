-- El vale de traspaso solo lo firman las áreas (entrega y recibe) y la Dirección de Patrimonio (autoriza).
-- Contabilidad no firma: se eliminan sus parámetros.
DELETE FROM parametro_sistema WHERE clave IN ('firma.contabilidad.nombre', 'firma.contabilidad.cargo');
