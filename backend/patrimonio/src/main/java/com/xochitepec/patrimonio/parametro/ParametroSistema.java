package com.xochitepec.patrimonio.parametro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametro_sistema")
@Getter
@Setter
@NoArgsConstructor
public class ParametroSistema {

    @Id
    @Column(length = 100)
    private String clave;

    @Column(length = 500)
    private String valor;

    @Column(length = 250)
    private String descripcion;
}
