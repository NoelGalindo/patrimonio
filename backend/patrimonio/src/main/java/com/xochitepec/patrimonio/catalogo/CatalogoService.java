package com.xochitepec.patrimonio.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRequest;
import com.xochitepec.patrimonio.catalogo.CatalogoDtos.DireccionRequest;
import com.xochitepec.patrimonio.common.ApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final DireccionGeneralRepository direcciones;
    private final AreaRepository areas;

    @Transactional(readOnly = true)
    public List<DireccionGeneral> listarDirecciones() {
        return direcciones.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Area> listarAreas(Long direccionGeneralId) {
        return direccionGeneralId == null
                ? areas.findAllByOrderByDireccionGeneralNombreAscNombreAsc()
                : areas.findByDireccionGeneralIdOrderByNombreAsc(direccionGeneralId);
    }

    @Transactional(readOnly = true)
    public Area obtenerArea(Long id) {
        return areas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Área"));
    }

    @Transactional
    public DireccionGeneral guardarDireccion(Long id, DireccionRequest req) {
        DireccionGeneral d = id == null ? new DireccionGeneral()
                : direcciones.findById(id).orElseThrow(() -> ApiException.noEncontrado("Dirección general"));
        d.setNombre(req.nombre().trim());
        return direcciones.saveAndFlush(d);
    }

    @Transactional
    public void eliminarDireccion(Long id) {
        if (!direcciones.existsById(id)) {
            throw ApiException.noEncontrado("Dirección general");
        }
        direcciones.deleteById(id);
        direcciones.flush();
    }

    @Transactional
    public Area guardarArea(Long id, AreaRequest req) {
        Area a = id == null ? new Area() : obtenerArea(id);
        a.setNombre(req.nombre().trim());
        a.setNombreCargo(req.nombreCargo());
        a.setDireccionGeneral(direcciones.findById(req.direccionGeneralId())
                .orElseThrow(() -> ApiException.solicitudInvalida("La dirección general no existe")));
        return areas.saveAndFlush(a);
    }

    @Transactional
    public void eliminarArea(Long id) {
        if (!areas.existsById(id)) {
            throw ApiException.noEncontrado("Área");
        }
        areas.deleteById(id);
        areas.flush();
    }
}
