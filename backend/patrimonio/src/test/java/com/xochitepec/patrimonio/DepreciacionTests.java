package com.xochitepec.patrimonio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;
import com.xochitepec.patrimonio.bien.mueble.BienMueble;
import com.xochitepec.patrimonio.bien.mueble.BienMuebleRepository;
import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.catalogo.DireccionGeneral;
import com.xochitepec.patrimonio.catalogo.DireccionGeneralRepository;
import com.xochitepec.patrimonio.usuario.RolNombre;
import com.xochitepec.patrimonio.usuario.RolRepository;
import com.xochitepec.patrimonio.usuario.Usuario;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

/** Cuentas contables CONAC, cálculo de depreciación por la API, regla configurable y reporte. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class DepreciacionTests {

    private static final String PASSWORD = "Secreta123";

    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;
    @Autowired DireccionGeneralRepository direcciones;
    @Autowired AreaRepository areas;
    @Autowired RolRepository roles;
    @Autowired UsuarioRepository usuarios;
    @Autowired BienMuebleRepository muebles;
    @Autowired PasswordEncoder encoder;

    Area areaA;
    Area areaB;

    @BeforeEach
    void datos() {
        jdbc.sql("""
                TRUNCATE solicitud_traspaso_detalle, solicitud_traspaso, folio_consecutivo,
                         bien_mueble, bien_inmueble, vehiculo, usuario, area, direccion_general
                RESTART IDENTITY CASCADE
                """).update();
        // Catálogo y regla vuelven a su estado precargado
        jdbc.sql("DELETE FROM cuenta_contable WHERE clave LIKE '9.%'").update();
        jdbc.sql("UPDATE cuenta_contable SET vida_util_anios = NULL, activo = TRUE WHERE clave = '1.2.4.5'").update();
        jdbc.sql("UPDATE parametro_sistema SET valor = 'ANIO_SIGUIENTE' WHERE clave = 'depreciacion.inicio'").update();

        DireccionGeneral dg = new DireccionGeneral();
        dg.setNombre("Tesorería");
        direcciones.save(dg);
        areaA = area("Informática", dg);
        areaB = area("Contabilidad", dg);
        usuario("admin@x.mx", RolNombre.ADMINISTRADOR, null);
        usuario("patrimonio@x.mx", RolNombre.PATRIMONIO, null);
        usuario("usuarioa@x.mx", RolNombre.USUARIO, areaA);
        usuario("usuariob@x.mx", RolNombre.USUARIO, areaB);
    }

    @Test
    void altaExigeCuentaValidaParaElTipoDeBien() throws Exception {
        String patrimonio = bearer(login("patrimonio@x.mx"));
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON)
                .content(computadora("A-1", null)).header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(status().isBadRequest());
        // Cuenta de vehículos en un bien mueble
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON)
                .content(computadora("A-2", cuenta("1.2.4.4.1"))).header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(status().isBadRequest());
        // Cuenta sin vida útil definida
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON)
                .content(computadora("A-3", cuenta("1.2.4.5"))).header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", Matchers.containsString("vida útil")));
    }

    @Test
    void altaCalculaDepreciacionYCalendario() throws Exception {
        long id = crearComputadora();
        mvc.perform(get("/api/bienes/muebles/" + id).header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaContable.clave").value("1.2.4.1.3"))
                .andExpect(jsonPath("$.cuentaContable.porcentajeAnual").value(33.33))
                .andExpect(jsonPath("$.depreciacion.depreciacionAnual").value(9999.67))
                .andExpect(jsonPath("$.depreciacion.fechaInicio").value("2027-01-01"))
                .andExpect(jsonPath("$.depreciacion.fechaFin").value("2029-12-31"))
                .andExpect(jsonPath("$.depreciacion.valorLibros").isNumber());

        mvc.perform(get("/api/bienes/muebles/" + id + "/depreciacion")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].anio").value(2027))
                .andExpect(jsonPath("$[2].depreciacion").value(9999.66))
                .andExpect(jsonPath("$[2].valorLibros").value(1.0));
        // Otra área: no existe para ese usuario
        mvc.perform(get("/api/bienes/muebles/" + id + "/depreciacion")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuariob@x.mx"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void bienSinCuentaExplicaPorQueNoSeCalcula() throws Exception {
        BienMueble b = new BienMueble();
        b.setNoControlInventario("VIEJO-1");
        b.setDescripcion("ARCHIVERO");
        b.setCosto(new BigDecimal("800"));
        b.setArea(areaA);
        b = muebles.save(b);
        mvc.perform(get("/api/bienes/muebles/" + b.getId()).header(HttpHeaders.AUTHORIZATION, bearer(login("patrimonio@x.mx"))))
                .andExpect(jsonPath("$.depreciacion.motivo").value("Sin cuenta contable asignada"))
                .andExpect(jsonPath("$.depreciacion.valorLibros").doesNotExist());
    }

    @Test
    void soloElAdministradorCambiaLaReglaYSeRecalcula() throws Exception {
        long id = crearComputadora();
        String regla = """
                [{"clave":"depreciacion.inicio","valor":"%s"}]""";
        mvc.perform(put("/api/admin/parametros").contentType(MediaType.APPLICATION_JSON)
                .content(regla.formatted("MES_SIGUIENTE")).header(HttpHeaders.AUTHORIZATION, bearer(login("patrimonio@x.mx"))))
                .andExpect(status().isForbidden());
        String admin = bearer(login("admin@x.mx"));
        mvc.perform(put("/api/admin/parametros").contentType(MediaType.APPLICATION_JSON)
                .content(regla.formatted("CADA_QUE_QUIERA")).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/parametros").contentType(MediaType.APPLICATION_JSON)
                .content(regla.formatted("MES_SIGUIENTE")).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/bienes/muebles/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.depreciacion.fechaInicio").value("2026-09-01"))
                .andExpect(jsonPath("$.depreciacion.fechaFin").value("2029-08-31"));
    }

    @Test
    void catalogoLoLeenTodosYSoloElAdministradorLoEdita() throws Exception {
        mvc.perform(get("/api/catalogos/cuentas-contables").param("tipoBien", "VEHICULO")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
        // Defensa y Seguridad no se ofrece mientras no tenga vida útil
        String patrimonio = bearer(login("patrimonio@x.mx"));
        mvc.perform(get("/api/catalogos/cuentas-contables").param("tipoBien", "MUEBLE")
                .header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(jsonPath("$[*].clave", Matchers.not(Matchers.hasItem("1.2.4.5"))));

        String defensa = """
                {"clave":"1.2.4.5","concepto":"Equipo de Defensa y Seguridad","tipoBien":"MUEBLE","vidaUtilAnios":8}""";
        long idDefensa = cuenta("1.2.4.5");
        mvc.perform(put("/api/admin/cuentas-contables/" + idDefensa).contentType(MediaType.APPLICATION_JSON)
                .content(defensa).header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(status().isForbidden());
        String admin = bearer(login("admin@x.mx"));
        mvc.perform(put("/api/admin/cuentas-contables/" + idDefensa).contentType(MediaType.APPLICATION_JSON)
                .content(defensa).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeAnual").value(12.5));
        mvc.perform(get("/api/catalogos/cuentas-contables").param("tipoBien", "MUEBLE")
                .header(HttpHeaders.AUTHORIZATION, patrimonio))
                .andExpect(jsonPath("$[*].clave", Matchers.hasItem("1.2.4.5")));

        // Cuenta nueva; la clave no se repite y el tipo no se cambia
        String nueva = """
                {"clave":"9.1","concepto":"Prueba","tipoBien":"MUEBLE","vidaUtilAnios":4}""";
        MvcResult r = mvc.perform(post("/api/admin/cuentas-contables").contentType(MediaType.APPLICATION_JSON)
                .content(nueva).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isCreated()).andReturn();
        mvc.perform(post("/api/admin/cuentas-contables").contentType(MediaType.APPLICATION_JSON)
                .content(nueva).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/admin/cuentas-contables/" + idDe(r)).contentType(MediaType.APPLICATION_JSON)
                .content(nueva.replace("MUEBLE", "VEHICULO")).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportePorCuentaParaPatrimonioYAdministrador() throws Exception {
        crearComputadora();
        BienMueble sinCuenta = new BienMueble();
        sinCuenta.setNoControlInventario("VIEJO-2");
        sinCuenta.setDescripcion("SILLA");
        sinCuenta.setArea(areaB);
        muebles.save(sinCuenta);

        mvc.perform(get("/api/reportes/depreciacion").param("anio", "2027")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/depreciacion").param("anio", "2027")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("patrimonio@x.mx"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regla").value("ANIO_SIGUIENTE"))
                .andExpect(jsonPath("$.cuentas.length()").value(1))
                .andExpect(jsonPath("$.cuentas[0].clave").value("1.2.4.1.3"))
                .andExpect(jsonPath("$.cuentas[0].depreciacionEjercicio").value(9999.67))
                .andExpect(jsonPath("$.cuentas[0].valorLibros").value(20000.33))
                .andExpect(jsonPath("$.total.bienes").value(1))
                .andExpect(jsonPath("$.bienesSinCalculo").value(1));
        // Antes de la compra el bien no aparece
        mvc.perform(get("/api/reportes/depreciacion").param("anio", "2025")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("admin@x.mx"))))
                .andExpect(jsonPath("$.total.bienes").value(0));
    }

    // ---------- Utilidades ----------

    /** Computadora de $30,000 facturada el 15-ago-2026 en el área A. */
    private long crearComputadora() throws Exception {
        MvcResult r = mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON)
                .content(computadora("PC-1", cuenta("1.2.4.1.3")))
                .header(HttpHeaders.AUTHORIZATION, bearer(login("patrimonio@x.mx"))))
                .andExpect(status().isCreated()).andReturn();
        return idDe(r);
    }

    private String computadora(String inventario, Long cuentaId) {
        return """
                {"noControlInventario":"%s","descripcion":"COMPUTADORA","costo":30000,
                 "fechaFactura":"2026-08-15","areaId":%d,"cuentaContableId":%s}
                """.formatted(inventario, areaA.getId(), cuentaId == null ? "null" : cuentaId.toString());
    }

    private long cuenta(String clave) {
        return jdbc.sql("SELECT id FROM cuenta_contable WHERE clave = ?").param(clave).query(Long.class).single();
    }

    private String login(String correo) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"%s\",\"password\":\"%s\"}".formatted(correo, PASSWORD)))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
    }

    private static long idDe(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private Area area(String nombre, DireccionGeneral dg) {
        Area a = new Area();
        a.setNombre(nombre);
        a.setDireccionGeneral(dg);
        return areas.save(a);
    }

    private void usuario(String correo, RolNombre rol, Area area) {
        Usuario u = new Usuario();
        u.setNombre("Prueba");
        u.setApellidoPaterno(rol.name());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(PASSWORD));
        u.setRol(roles.findByNombre(rol).orElseThrow());
        u.setArea(area);
        u.setActivo(true);
        usuarios.save(u);
    }
}
