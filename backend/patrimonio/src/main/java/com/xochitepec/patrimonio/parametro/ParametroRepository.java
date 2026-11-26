package com.xochitepec.patrimonio.parametro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametroRepository extends JpaRepository<ParametroSistema, String> {

    List<ParametroSistema> findAllByOrderByClaveAsc();
}
