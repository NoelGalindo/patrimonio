package com.xochitepec.patrimonio.bien.vehiculo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xochitepec.patrimonio.bien.BienService;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.depreciacion.DepreciacionService;
import com.xochitepec.patrimonio.depreciacion.TipoBienContable;
import com.xochitepec.patrimonio.security.CurrentUser;

@Service
public class VehiculoService extends BienService<Vehiculo> {

    public VehiculoService(VehiculoRepository repo, AreaRepository areas, CurrentUser currentUser,
            DepreciacionService depreciacion) {
        super(repo, areas, currentUser, depreciacion);
    }

    @Override
    protected String nombreRecurso() {
        return "Vehículo";
    }

    @Override
    protected List<String> camposBusqueda() {
        return List.of("noInventario", "noEconomico", "marca", "version", "placas", "noSerie");
    }

    @Override
    protected TipoBienContable tipoContable() {
        return TipoBienContable.VEHICULO;
    }
}
