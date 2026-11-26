package com.xochitepec.patrimonio.bien.inmueble;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xochitepec.patrimonio.bien.BienService;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.security.CurrentUser;

@Service
public class BienInmuebleService extends BienService<BienInmueble> {

    public BienInmuebleService(BienInmuebleRepository repo, AreaRepository areas, CurrentUser currentUser) {
        super(repo, areas, currentUser);
    }

    @Override
    protected String nombreRecurso() {
        return "Bien inmueble";
    }

    @Override
    protected List<String> camposBusqueda() {
        return List.of("noExpediente", "denominacion", "colonia", "calle", "claveCatastral");
    }
}
