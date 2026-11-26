// Roles definidos en el backend (enum RolNombre)
export const ROLES = {
    ADMINISTRADOR: "ADMINISTRADOR",
    PATRIMONIO: "PATRIMONIO",
    USUARIO: "USUARIO",
};

export const ROL_INFO = {
    ADMINISTRADOR: {
        label: "Administrador",
        color: "primary",
        descripcion: "Crea usuarios, asigna roles y administra catálogos. Consulta todos los bienes.",
    },
    PATRIMONIO: {
        label: "Patrimonio",
        color: "secondary",
        descripcion: "Captura y mantiene los bienes de todas las áreas y autoriza traspasos.",
    },
    USUARIO: {
        label: "Usuario de Área",
        color: "info",
        descripcion: "Consulta los bienes de su área y solicita traspasos.",
    },
};
