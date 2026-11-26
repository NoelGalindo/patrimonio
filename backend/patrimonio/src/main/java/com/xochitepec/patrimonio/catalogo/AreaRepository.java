package com.xochitepec.patrimonio.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {

    List<Area> findAllByOrderByDireccionGeneralNombreAscNombreAsc();

    List<Area> findByDireccionGeneralIdOrderByNombreAsc(Long direccionGeneralId);
}
