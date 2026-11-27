package com.xochitepec.patrimonio.traspaso;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.bien.mueble.BienMueble;
import com.xochitepec.patrimonio.bien.mueble.BienMuebleRepository;
import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.common.PageResponse;
import com.xochitepec.patrimonio.security.CurrentUser;
import com.xochitepec.patrimonio.security.UsuarioActual;
import com.xochitepec.patrimonio.traspaso.TraspasoDtos.CrearTraspasoRequest;
import com.xochitepec.patrimonio.traspaso.TraspasoDtos.TraspasoResponse;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Flujo de traspaso de bienes muebles entre áreas:
 * el área origen solicita, Patrimonio autoriza o rechaza, y al autorizar el bien cambia de área
 * y queda disponible el vale de traspaso.
 */
@Service
@RequiredArgsConstructor
public class TraspasoService {

    public static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final SolicitudTraspasoRepository solicitudes;
    private final BienMuebleRepository muebles;
    private final AreaRepository areas;
    private final UsuarioRepository usuarios;
    private final FolioService folios;
    private final ValeTraspasoPdfService valePdf;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public PageResponse<TraspasoResponse> listar(EstatusTraspaso estatus, Pageable pageable) {
        UsuarioActual u = currentUser.get();
        Specification<SolicitudTraspaso> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (estatus != null) {
                ps.add(cb.equal(root.get("estatus"), estatus));
            }
            if (u.esUsuario()) {
                // El usuario ve las solicitudes que salen de su área o que llegan a ella
                ps.add(u.areaId() == null ? cb.disjunction()
                        : cb.or(cb.equal(root.get("areaOrigen").get("id"), u.areaId()),
                                cb.equal(root.get("areaDestino").get("id"), u.areaId())));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        Page<SolicitudTraspaso> page = solicitudes.findAll(spec, pageable);
        return PageResponse.of(page, TraspasoResponse::from);
    }

    @Transactional(readOnly = true)
    public TraspasoResponse obtener(Long id) {
        return TraspasoResponse.from(cargarVisible(id));
    }

    @Transactional
    public TraspasoResponse solicitar(CrearTraspasoRequest req) {
        UsuarioActual u = currentUser.get();
        Set<Long> ids = new LinkedHashSet<>(req.bienIds());
        Map<Long, BienMueble> encontrados = muebles.findAllById(ids).stream()
                .collect(Collectors.toMap(BienMueble::getId, Function.identity()));

        List<BienMueble> bienes = new ArrayList<>();
        for (Long id : ids) {
            BienMueble b = encontrados.get(id);
            // Un USUARIO no puede ni confirmar la existencia de bienes de otra área
            if (b == null || (u.esUsuario() && !b.getArea().getId().equals(u.areaId()))) {
                throw ApiException.noEncontrado("Bien mueble con id " + id);
            }
            if (!b.isActivo()) {
                throw ApiException.solicitudInvalida("El bien " + b.getNoControlInventario() + " está dado de baja");
            }
            bienes.add(b);
        }

        Area origen = bienes.get(0).getArea();
        if (bienes.stream().anyMatch(b -> !b.getArea().getId().equals(origen.getId()))) {
            throw ApiException.solicitudInvalida("Todos los bienes de una solicitud deben pertenecer a la misma área");
        }
        Area destino = areas.findById(req.areaDestinoId())
                .orElseThrow(() -> ApiException.solicitudInvalida("El área destino no existe"));
        if (destino.getId().equals(origen.getId())) {
            throw ApiException.solicitudInvalida("El área destino debe ser distinta del área actual del bien");
        }
        List<String> pendientes = solicitudes.inventariosEnSolicitudPendiente(ids);
        if (!pendientes.isEmpty()) {
            throw ApiException.conflicto("Ya existe una solicitud pendiente para: " + String.join(", ", pendientes));
        }

        SolicitudTraspaso s = new SolicitudTraspaso();
        s.setAreaOrigen(origen);
        s.setAreaDestino(destino);
        s.setSolicitante(usuarios.getReferenceById(u.id()));
        s.setOficioPeticion(blankToNull(req.oficioPeticion()));
        s.setMotivo(blankToNull(req.motivo()));
        for (BienMueble b : bienes) {
            SolicitudTraspasoDetalle d = new SolicitudTraspasoDetalle();
            d.setBienMueble(b);
            s.agregarDetalle(d);
        }
        solicitudes.saveAndFlush(s);
        return TraspasoResponse.from(solicitudes.findById(s.getId()).orElseThrow());
    }

    @Transactional
    public TraspasoResponse cancelar(Long id) {
        UsuarioActual u = currentUser.get();
        SolicitudTraspaso s = verificarVisible(solicitudes.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.noEncontrado("Solicitud de traspaso")));
        if (!s.getSolicitante().getId().equals(u.id())) {
            throw ApiException.prohibido("Solo quien hizo la solicitud puede cancelarla");
        }
        exigirPendiente(s);
        resolver(s, EstatusTraspaso.CANCELADA, null, u);
        return TraspasoResponse.from(s);
    }

    @Transactional
    public TraspasoResponse rechazar(Long id, String comentario) {
        if (comentario == null || comentario.isBlank()) {
            throw ApiException.solicitudInvalida("Indica el motivo del rechazo");
        }
        SolicitudTraspaso s = solicitudes.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.noEncontrado("Solicitud de traspaso"));
        exigirPendiente(s);
        resolver(s, EstatusTraspaso.RECHAZADA, comentario, currentUser.get());
        return TraspasoResponse.from(s);
    }

    @Transactional
    public TraspasoResponse autorizar(Long id, String comentario) {
        SolicitudTraspaso s = solicitudes.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.noEncontrado("Solicitud de traspaso"));
        exigirPendiente(s);
        // Firman el vale los titulares de ambas áreas (responsables del inventario a su resguardo)
        String titularOrigen = titular(s.getAreaOrigen());
        String titularDestino = titular(s.getAreaDestino());

        LocalDate hoy = LocalDate.now(ZONA);
        for (SolicitudTraspasoDetalle d : s.getDetalles()) {
            BienMueble b = d.getBienMueble();
            // Pudo cambiar desde que se solicitó (edición o baja hecha por Patrimonio)
            if (!b.isActivo() || !b.getArea().getId().equals(s.getAreaOrigen().getId())) {
                throw ApiException.conflicto("El bien " + b.getNoControlInventario()
                        + " ya no está activo en el área origen; rechaza la solicitud o corrígela");
            }
            d.tomarSnapshot(b);
            b.setArea(s.getAreaDestino());
            // El resguardo pasa al titular del área destino
            b.setServidorPublico(titularDestino);
            b.setCargo("Titular de " + s.getAreaDestino().getNombre());
            b.setFechaResguardo(hoy);
        }
        s.setTitularOrigen(titularOrigen);
        s.setTitularDestino(titularDestino);
        s.setAnioFolio(hoy.getYear());
        s.setFolio(folios.siguiente(hoy.getYear()));
        resolver(s, EstatusTraspaso.AUTORIZADA, comentario, currentUser.get());
        return TraspasoResponse.from(s);
    }

    private static String titular(Area area) {
        String t = area.getNombreCargo();
        if (t == null || t.isBlank()) {
            throw ApiException.solicitudInvalida("El área " + area.getNombre()
                    + " no tiene titular registrado; regístralo en Áreas antes de autorizar (firma el vale)");
        }
        return t.trim();
    }

    /** Genera el PDF del vale de una solicitud autorizada y visible para el usuario actual. */
    @Transactional(readOnly = true)
    public byte[] generarVale(Long id) {
        SolicitudTraspaso s = cargarVisible(id);
        if (s.getEstatus() != EstatusTraspaso.AUTORIZADA) {
            throw ApiException.conflicto("El vale solo está disponible para solicitudes autorizadas");
        }
        return valePdf.generar(s);
    }

    private SolicitudTraspaso cargarVisible(Long id) {
        return verificarVisible(solicitudes.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Solicitud de traspaso")));
    }

    /** Un USUARIO solo ve solicitudes que salen de su área o llegan a ella; las demás son 404. */
    private SolicitudTraspaso verificarVisible(SolicitudTraspaso s) {
        UsuarioActual u = currentUser.get();
        if (u.esUsuario()) {
            Long area = u.areaId();
            boolean visible = area != null
                    && (area.equals(s.getAreaOrigen().getId()) || area.equals(s.getAreaDestino().getId()));
            if (!visible) {
                throw ApiException.noEncontrado("Solicitud de traspaso");
            }
        }
        return s;
    }

    private void exigirPendiente(SolicitudTraspaso s) {
        if (s.getEstatus() != EstatusTraspaso.PENDIENTE) {
            throw ApiException.conflicto("La solicitud ya fue resuelta (" + s.getEstatus() + ")");
        }
    }

    private void resolver(SolicitudTraspaso s, EstatusTraspaso estatus, String comentario, UsuarioActual u) {
        s.setEstatus(estatus);
        s.setComentarioResolucion(blankToNull(comentario));
        s.setResueltoPor(usuarios.getReferenceById(u.id()));
        s.setFechaResolucion(Instant.now());
        solicitudes.flush();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
