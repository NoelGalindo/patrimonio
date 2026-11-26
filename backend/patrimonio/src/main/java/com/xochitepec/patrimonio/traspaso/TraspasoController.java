package com.xochitepec.patrimonio.traspaso;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.common.PageResponse;
import com.xochitepec.patrimonio.traspaso.TraspasoDtos.CrearTraspasoRequest;
import com.xochitepec.patrimonio.traspaso.TraspasoDtos.ResolverTraspasoRequest;
import com.xochitepec.patrimonio.traspaso.TraspasoDtos.TraspasoResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/traspasos")
@RequiredArgsConstructor
public class TraspasoController {

    private final TraspasoService service;

    /** USUARIO: solicitudes de su área (origen o destino). PATRIMONIO y ADMINISTRADOR: todas. */
    @GetMapping
    public PageResponse<TraspasoResponse> listar(@RequestParam(required = false) EstatusTraspaso estatus,
            @PageableDefault(size = 20, sort = "fechaSolicitud", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(estatus, pageable);
    }

    @GetMapping("/{id}")
    public TraspasoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USUARIO', 'PATRIMONIO')")
    public TraspasoResponse solicitar(@Valid @RequestBody CrearTraspasoRequest req) {
        return service.solicitar(req);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('USUARIO', 'PATRIMONIO')")
    public TraspasoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PatchMapping("/{id}/autorizar")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public TraspasoResponse autorizar(@PathVariable Long id,
            @Valid @RequestBody(required = false) ResolverTraspasoRequest req) {
        return service.autorizar(id, req == null ? null : req.comentario());
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public TraspasoResponse rechazar(@PathVariable Long id, @Valid @RequestBody ResolverTraspasoRequest req) {
        return service.rechazar(id, req.comentario());
    }

    @GetMapping("/{id}/vale.pdf")
    public ResponseEntity<byte[]> vale(@PathVariable Long id) {
        byte[] pdf = service.generarVale(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("vale-traspaso-" + id + ".pdf").build().toString())
                .body(pdf);
    }
}
