package com.xochitepec.patrimonio.traspaso;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Consecutivo de folios de vale por año. */
@Service
@RequiredArgsConstructor
public class FolioService {

    private final JdbcClient jdbc;

    /**
     * Incrementa y devuelve el siguiente folio del año de forma atómica: el UPSERT toma un candado
     * de fila sobre folio_consecutivo hasta que termina la transacción, así que dos autorizaciones
     * simultáneas nunca obtienen el mismo número.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int siguiente(int anio) {
        return jdbc.sql("""
                INSERT INTO folio_consecutivo (anio, ultimo) VALUES (:anio, 1)
                ON CONFLICT (anio) DO UPDATE SET ultimo = folio_consecutivo.ultimo + 1
                RETURNING ultimo
                """)
                .param("anio", anio)
                .query(Integer.class)
                .single();
    }
}
