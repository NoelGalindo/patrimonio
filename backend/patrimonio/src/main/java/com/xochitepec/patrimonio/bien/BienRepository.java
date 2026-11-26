package com.xochitepec.patrimonio.bien;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BienRepository<E extends BienBase> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {
}
