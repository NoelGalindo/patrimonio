package com.xochitepec.patrimonio.usuario;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByRolNombre(RolNombre rol);

    @Query("""
            select u from Usuario u left join u.area a
            where (:q is null
                   or lower(concat(u.nombre, ' ', u.apellidoPaterno, ' ', coalesce(u.apellidoMaterno, ''))) like :q
                   or lower(u.correo) like :q)
              and (:rol is null or u.rol.nombre = :rol)
              and (:areaId is null or a.id = :areaId)
            """)
    Page<Usuario> buscar(@Param("q") String q, @Param("rol") RolNombre rol, @Param("areaId") Long areaId,
            Pageable pageable);
}
