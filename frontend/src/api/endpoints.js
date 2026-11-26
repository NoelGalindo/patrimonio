import { api, descargarArchivo } from "./client";

// ---------- Autenticación ----------
export const authApi = {
    login: (correo, password) => api("/auth/login", { method: "POST", body: { correo, password } }),
    me: () => api("/auth/me"),
};

// ---------- Catálogos ----------
export const catalogosApi = {
    direcciones: () => api("/catalogos/direcciones"),
    areas: (direccionGeneralId) => api("/catalogos/areas", { params: { direccionGeneralId } }),
    crearDireccion: (body) => api("/admin/direcciones", { method: "POST", body }),
    editarDireccion: (id, body) => api(`/admin/direcciones/${id}`, { method: "PUT", body }),
    eliminarDireccion: (id) => api(`/admin/direcciones/${id}`, { method: "DELETE" }),
    crearArea: (body) => api("/admin/areas", { method: "POST", body }),
    editarArea: (id, body) => api(`/admin/areas/${id}`, { method: "PUT", body }),
    eliminarArea: (id) => api(`/admin/areas/${id}`, { method: "DELETE" }),
};

// ---------- Bienes (tipo: "muebles" | "inmuebles" | "vehiculos") ----------
export const bienesApi = {
    listar: (tipo, params) => api(`/bienes/${tipo}`, { params }),
    obtener: (tipo, id) => api(`/bienes/${tipo}/${id}`),
    crear: (tipo, body) => api(`/bienes/${tipo}`, { method: "POST", body }),
    actualizar: (tipo, id, body) => api(`/bienes/${tipo}/${id}`, { method: "PUT", body }),
    darDeBaja: (tipo, id) => api(`/bienes/${tipo}/${id}`, { method: "DELETE" }),
};

// ---------- Usuarios (solo ADMINISTRADOR) ----------
export const usuariosApi = {
    listar: (params) => api("/admin/usuarios", { params }),
    crear: (body) => api("/admin/usuarios", { method: "POST", body }),
    editar: (id, body) => api(`/admin/usuarios/${id}`, { method: "PUT", body }),
    cambiarActivo: (id, activo) => api(`/admin/usuarios/${id}/activo`, { method: "PATCH", body: { activo } }),
    cambiarPassword: (id, password) =>
        api(`/admin/usuarios/${id}/password`, { method: "PATCH", body: { password } }),
};

// ---------- Traspasos ----------
export const traspasosApi = {
    listar: (params) => api("/traspasos", { params }),
    obtener: (id) => api(`/traspasos/${id}`),
    solicitar: (body) => api("/traspasos", { method: "POST", body }),
    cancelar: (id) => api(`/traspasos/${id}/cancelar`, { method: "PATCH" }),
    autorizar: (id, comentario) => api(`/traspasos/${id}/autorizar`, { method: "PATCH", body: { comentario } }),
    rechazar: (id, comentario) => api(`/traspasos/${id}/rechazar`, { method: "PATCH", body: { comentario } }),
    descargarVale: (t) =>
        descargarArchivo(`/traspasos/${t.id}/vale.pdf`, `vale-traspaso-${t.anioFolio}-${String(t.folio).padStart(3, "0")}.pdf`),
};

// ---------- Parámetros del sistema (solo ADMINISTRADOR) ----------
export const parametrosApi = {
    listar: () => api("/admin/parametros"),
    guardar: (lista) => api("/admin/parametros", { method: "PUT", body: lista }),
};
