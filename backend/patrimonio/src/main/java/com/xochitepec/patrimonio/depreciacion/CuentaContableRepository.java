package com.xochitepec.patrimonio.depreciacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaContableRepository extends JpaRepository<CuentaContable, Long> {

    List<CuentaContable> findAllByOrderByClaveAsc();

    /** Cuentas que se pueden asignar a un bien del tipo dado. */
    List<CuentaContable> findByTipoBienAndActivoTrueAndVidaUtilAniosNotNullOrderByClaveAsc(TipoBienContable tipoBien);

    boolean existsByClave(String clave);
}
