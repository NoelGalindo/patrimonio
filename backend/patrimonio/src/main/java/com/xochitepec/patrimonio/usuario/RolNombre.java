package com.xochitepec.patrimonio.usuario;

/** Roles del sistema. Los nombres coinciden con la tabla `rol` y con las autoridades ROLE_*. */
public enum RolNombre {
    /** Crea usuarios, asigna roles y administra catálogos. Ve los bienes en solo lectura. */
    ADMINISTRADOR,
    /** Captura y mantiene los bienes de todas las áreas y autoriza traspasos. */
    PATRIMONIO,
    /** Consulta los bienes de su área y solicita traspasos. */
    USUARIO
}
