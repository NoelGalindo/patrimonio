package com.xochitepec.patrimonio.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionGeneralRepository extends JpaRepository<DireccionGeneral, Long> {

    List<DireccionGeneral> findAllByOrderByNombreAsc();
}
