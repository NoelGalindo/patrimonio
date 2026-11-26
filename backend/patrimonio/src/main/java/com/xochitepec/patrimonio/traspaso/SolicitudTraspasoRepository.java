package com.xochitepec.patrimonio.traspaso;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SolicitudTraspasoRepository
        extends JpaRepository<SolicitudTraspaso, Long>, JpaSpecificationExecutor<SolicitudTraspaso> {

    /** Bloquea la fila para que dos personas no resuelvan la misma solicitud a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitudTraspaso s where s.id = :id")
    Optional<SolicitudTraspaso> findByIdForUpdate(@Param("id") Long id);

    /** Inventarios de los bienes indicados que ya están en una solicitud pendiente. */
    @Query("""
            select d.bienMueble.noControlInventario from SolicitudTraspasoDetalle d
            where d.solicitud.estatus = com.xochitepec.patrimonio.traspaso.EstatusTraspaso.PENDIENTE
              and d.bienMueble.id in :bienIds
            """)
    List<String> inventariosEnSolicitudPendiente(@Param("bienIds") Collection<Long> bienIds);
}
