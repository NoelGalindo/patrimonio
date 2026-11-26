package com.xochitepec.patrimonio.bien.mueble;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xochitepec.patrimonio.bien.BienService;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.security.CurrentUser;

@Service
public class BienMuebleService extends BienService<BienMueble> {

    public BienMuebleService(BienMuebleRepository repo, AreaRepository areas, CurrentUser currentUser) {
        super(repo, areas, currentUser);
    }

    @Override
    protected String nombreRecurso() {
        return "Bien mueble";
    }

    @Override
    protected List<String> camposBusqueda() {
        return List.of("noControlInventario", "descripcion", "marca", "modelo", "serie", "servidorPublico");
    }
}
