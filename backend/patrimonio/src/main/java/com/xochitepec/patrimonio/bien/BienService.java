package com.xochitepec.patrimonio.bien;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.security.CurrentUser;
import com.xochitepec.patrimonio.security.UsuarioActual;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Lógica común de los tres tipos de bien. Aquí se aplica la regla de visibilidad:
 * un USUARIO solo ve bienes de su propia área, sin importar los filtros que envíe el cliente.
 */
public abstract class BienService<E extends BienBase> {

    protected final BienRepository<E> repo;
    protected final AreaRepository areas;
    protected final CurrentUser currentUser;

    protected BienService(BienRepository<E> repo, AreaRepository areas, CurrentUser currentUser) {
        this.repo = repo;
        this.areas = areas;
        this.currentUser = currentUser;
    }

    /** Nombre para mensajes de error ("Bien mueble", "Vehículo"...). */
    protected abstract String nombreRecurso();

    /** Atributos de texto en los que busca el parámetro `q`. */
    protected abstract List<String> camposBusqueda();

    @Transactional(readOnly = true)
    public Page<E> listar(String q, Long areaId, Boolean activo, Pageable pageable) {
        UsuarioActual u = currentUser.get();
        // Para USUARIO se ignora el areaId recibido y se fuerza el área propia
        Long areaFiltro = u.esUsuario() ? u.areaId() : areaId;
        boolean soloActivos = activo == null || activo;
        String patron = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";

        Specification<E> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (u.esUsuario() && areaFiltro == null) {
                ps.add(cb.disjunction()); // USUARIO sin área: no ve nada
            } else if (areaFiltro != null) {
                ps.add(cb.equal(root.get("area").get("id"), areaFiltro));
            }
            ps.add(cb.equal(root.get("activo"), soloActivos));
            if (patron != null) {
                List<Predicate> texto = new ArrayList<>();
                for (String campo : camposBusqueda()) {
                    Expression<String> exp = root.get(campo);
                    texto.add(cb.like(cb.lower(exp), patron));
                }
                ps.add(cb.or(texto.toArray(Predicate[]::new)));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return repo.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public E obtener(Long id) {
        E bien = repo.findById(id).orElseThrow(() -> ApiException.noEncontrado(nombreRecurso()));
        verificarVisibilidad(bien);
        return bien;
    }

    @Transactional
    public E crear(E bien, Long areaId) {
        bien.setArea(area(areaId));
        bien.setCreadoPor(currentUser.get().id());
        return repo.saveAndFlush(bien);
    }

    @Transactional
    public E actualizar(Long id, Long areaId, Consumer<E> cambios) {
        E bien = obtener(id);
        cambios.accept(bien);
        bien.setArea(area(areaId));
        return repo.saveAndFlush(bien);
    }

    /** Baja lógica: el bien deja de aparecer en el inventario activo pero se conserva el registro. */
    @Transactional
    public void darDeBaja(Long id) {
        obtener(id).setActivo(false);
    }

    /** Un bien de otra área se reporta como inexistente (404) para no revelar su existencia. */
    protected void verificarVisibilidad(E bien) {
        UsuarioActual u = currentUser.get();
        if (u.esUsuario() && (u.areaId() == null || !u.areaId().equals(bien.getArea().getId()))) {
            throw ApiException.noEncontrado(nombreRecurso());
        }
    }

    protected Area area(Long areaId) {
        if (areaId == null) {
            throw ApiException.solicitudInvalida("El área es obligatoria");
        }
        return areas.findById(areaId).orElseThrow(() -> ApiException.solicitudInvalida("El área no existe"));
    }
}
