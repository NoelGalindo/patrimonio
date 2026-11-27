package com.xochitepec.patrimonio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
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

/** Matriz de permisos por rol, aislamiento por área y flujo completo de traspaso. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class SeguridadYTraspasosTests {

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
    BienMueble bienA;
    BienMueble bienA2;
    BienMueble bienB;

    @BeforeEach
    void datos() {
        jdbc.sql("""
                TRUNCATE solicitud_traspaso_detalle, solicitud_traspaso, folio_consecutivo,
                         bien_mueble, bien_inmueble, vehiculo, usuario, area, direccion_general
                RESTART IDENTITY CASCADE
                """).update();

        DireccionGeneral dg = new DireccionGeneral();
        dg.setNombre("Secretaría del Ayuntamiento");
        direcciones.save(dg);
        areaA = area("Recursos Humanos", dg);
        areaB = area("Planeación del Desarrollo Urbano", dg);

        usuario("admin@x.mx", RolNombre.ADMINISTRADOR, null, true);
        usuario("patrimonio@x.mx", RolNombre.PATRIMONIO, null, true);
        usuario("usuarioa@x.mx", RolNombre.USUARIO, areaA, true);
        usuario("usuariob@x.mx", RolNombre.USUARIO, areaB, true);
        usuario("inactivo@x.mx", RolNombre.USUARIO, areaA, false);

        bienA = mueble("0625-A", "MONITOR", areaA);
        bienA2 = mueble("0626-A", "TECLADO", areaA);
        bienB = mueble("0700-B", "ESCRITORIO", areaB);
    }

    // ---------- Autenticación ----------

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/bienes/muebles")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginIncorrectoOInactivoResponde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"usuarioa@x.mx\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"inactivo@x.mx\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDevuelvePerfilYMeFunciona() throws Exception {
        String token = login("usuarioa@x.mx");
        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.area.id").value(areaA.getId()));
    }

    @Test
    void usuarioDesactivadoPierdeAccesoAunConTokenVigente() throws Exception {
        String token = login("usuarioa@x.mx");
        jdbc.sql("UPDATE usuario SET activo = false WHERE correo = 'usuarioa@x.mx'").update();
        mvc.perform(get("/api/bienes/muebles").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Aislamiento por área ----------

    @Test
    void usuarioSoloVeBienesDeSuArea() throws Exception {
        String token = login("usuarioa@x.mx");
        mvc.perform(get("/api/bienes/muebles").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        // Aunque pida explícitamente otra área, se le fuerza la suya
        mvc.perform(get("/api/bienes/muebles").param("areaId", areaB.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[0].area.id").value(areaA.getId()));
        mvc.perform(get("/api/bienes/muebles/" + bienB.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patrimonioYAdminVenTodasLasAreas() throws Exception {
        for (String correo : List.of("patrimonio@x.mx", "admin@x.mx")) {
            mvc.perform(get("/api/bienes/muebles").header(HttpHeaders.AUTHORIZATION, bearer(login(correo))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(3));
        }
    }

    // ---------- Permisos de escritura ----------

    @Test
    void soloPatrimonioCapturaBienes() throws Exception {
        String body = """
                {"noControlInventario":"N-1","descripcion":"SILLA","areaId":%d,"costo":100,"cuentaContableId":%d}
                """.formatted(areaA.getId(), jdbc.sql("SELECT id FROM cuenta_contable WHERE clave = '1.2.4.1.1'")
                        .query(Long.class).single());
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, bearer(login("admin@x.mx"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/bienes/muebles").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, bearer(login("patrimonio@x.mx"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.noControlInventario").value("N-1"));
    }

    @Test
    void administradorGestionaCualquierRol() throws Exception {
        String admin = bearer(login("admin@x.mx"));
        mvc.perform(post("/api/admin/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson("nuevo.patrimonio@x.mx", "PATRIMONIO", null))
                .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PATRIMONIO"));
        // Un USUARIO sin área se rechaza
        mvc.perform(post("/api/admin/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson("sin.area@x.mx", "USUARIO", null))
                .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
        // Un USUARIO de área no gestiona usuarios
        mvc.perform(get("/api/admin/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void patrimonioSoloGestionaUsuariosDeArea() throws Exception {
        String pat = bearer(login("patrimonio@x.mx"));
        // Puede crear usuarios de área
        mvc.perform(post("/api/admin/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson("ana@x.mx", "USUARIO", areaB.getId()))
                .header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"));
        // Pero no de otros roles
        for (String rol : List.of("PATRIMONIO", "ADMINISTRADOR")) {
            mvc.perform(post("/api/admin/usuarios").contentType(MediaType.APPLICATION_JSON)
                    .content(usuarioJson("otro." + rol.toLowerCase() + "@x.mx", rol, areaB.getId()))
                    .header(HttpHeaders.AUTHORIZATION, pat))
                    .andExpect(status().isForbidden());
        }
        // Solo ve usuarios de área (3 del set de prueba + la recién creada), nunca admins ni Patrimonio
        mvc.perform(get("/api/admin/usuarios").header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4));
        Long adminId = usuarios.findByCorreoIgnoreCase("admin@x.mx").orElseThrow().getId();
        mvc.perform(get("/api/admin/usuarios/" + adminId).header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/admin/usuarios/" + adminId + "/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"Hackeo1234\"}").header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isNotFound());
        // No puede ascender a un usuario de área
        Long usuarioAId = usuarios.findByCorreoIgnoreCase("usuarioa@x.mx").orElseThrow().getId();
        mvc.perform(put("/api/admin/usuarios/" + usuarioAId).contentType(MediaType.APPLICATION_JSON)
                .content(usuarioJson("usuarioa@x.mx", "PATRIMONIO", areaA.getId()))
                .header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isForbidden());
    }

    @Test
    void patrimonioRegistraAreasPeroNoDireccionesNiParametros() throws Exception {
        String pat = bearer(login("patrimonio@x.mx"));
        Long dgId = areaA.getDireccionGeneral().getId();
        mvc.perform(post("/api/admin/areas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Catastro\",\"direccionGeneralId\":" + dgId + "}")
                .header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/admin/direcciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Tesorería\"}").header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/parametros").header(HttpHeaders.AUTHORIZATION, pat))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/areas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"X\",\"direccionGeneralId\":" + dgId + "}")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuarioa@x.mx"))))
                .andExpect(status().isForbidden());
    }

    private static String usuarioJson(String correo, String rol, Long areaId) {
        return """
                {"nombre":"Ana","apellidoPaterno":"López","correo":"%s","password":"Password1",
                 "rol":"%s","areaId":%s}
                """.formatted(correo, rol, areaId);
    }

    // ---------- Traspasos ----------

    @Test
    void flujoCompletoDeTraspaso() throws Exception {
        String tokenA = login("usuarioa@x.mx");
        String tokenPat = login("patrimonio@x.mx");

        // Un usuario no puede pedir bienes de otra área
        solicitar(tokenA, bienB.getId(), areaA.getId()).andExpect(status().isNotFound());

        MvcResult r = solicitar(tokenA, bienA.getId(), areaB.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estatus").value("PENDIENTE"))
                .andReturn();
        long id = ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();

        // El mismo bien no puede estar en dos solicitudes pendientes
        solicitar(tokenA, bienA.getId(), areaB.getId()).andExpect(status().isConflict());
        // El usuario no puede autorizar
        mvc.perform(patch("/api/traspasos/" + id + "/autorizar").header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(status().isForbidden());
        // Sin autorizar no hay vale
        mvc.perform(get("/api/traspasos/" + id + "/vale.pdf").header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/traspasos/" + id + "/autorizar").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("AUTORIZADA"))
                .andExpect(jsonPath("$.folio").value(1));

        BienMueble actualizado = muebles.findById(bienA.getId()).orElseThrow();
        assertThat(actualizado.getArea().getId()).isEqualTo(areaB.getId());
        // El resguardo pasa al titular del área destino
        assertThat(actualizado.getServidorPublico()).isEqualTo("Titular de Planeación del Desarrollo Urbano");

        // El área destino ya ve el bien y el área origen ya no
        mvc.perform(get("/api/bienes/muebles").header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/api/bienes/muebles").header(HttpHeaders.AUTHORIZATION, bearer(login("usuariob@x.mx"))))
                .andExpect(jsonPath("$.totalElementos").value(2));

        MvcResult pdf = mvc.perform(get("/api/traspasos/" + id + "/vale.pdf")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        byte[] bytes = pdf.getResponse().getContentAsByteArray();
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        // Firman entrega, recibe y autoriza (Patrimonio); Contabilidad no firma el vale de traspaso
        String texto = new PdfTextExtractor(new PdfReader(bytes)).getTextFromPage(1);
        assertThat(texto).contains("ENTREGA EN CONDICIONES FUNCIONALES", "RECIBE EN CONDICIONES FUNCIONALES", "AUTORIZA",
                        "TITULAR DE RECURSOS HUMANOS", "TITULAR DE PLANEACIÓN DEL DESARROLLO URBANO")
                .doesNotContain("Vo. Bo.").doesNotContainIgnoringCase("CONTABILIDAD");
        if (System.getProperty("vale.salida") != null) {
            java.nio.file.Files.write(java.nio.file.Path.of(System.getProperty("vale.salida")), bytes);
        }

        // Ya resuelta, no se puede volver a autorizar
        mvc.perform(patch("/api/traspasos/" + id + "/autorizar").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isConflict());
    }

    @Test
    void autorizarExigeTitularEnAmbasAreasYLoGuarda() throws Exception {
        String tokenA = login("usuarioa@x.mx");
        String tokenPat = login("patrimonio@x.mx");
        long id = idDe(solicitar(tokenA, bienA.getId(), areaB.getId()).andExpect(status().isCreated()).andReturn());

        jdbc.sql("UPDATE area SET nombre_cargo = NULL WHERE id = ?").param(areaB.getId()).update();
        mvc.perform(patch("/api/traspasos/" + id + "/autorizar").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", org.hamcrest.Matchers.containsString("no tiene titular")));

        jdbc.sql("UPDATE area SET nombre_cargo = 'Lic. Ana Pérez' WHERE id = ?").param(areaB.getId()).update();
        mvc.perform(patch("/api/traspasos/" + id + "/autorizar").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titularDestino").value("Lic. Ana Pérez"));

        // Cambiar después al titular no altera el vale ya autorizado
        jdbc.sql("UPDATE area SET nombre_cargo = 'Otra Persona' WHERE id = ?").param(areaB.getId()).update();
        mvc.perform(get("/api/traspasos/" + id).header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(jsonPath("$.titularDestino").value("Lic. Ana Pérez"));
        byte[] vale = mvc.perform(get("/api/traspasos/" + id + "/vale.pdf").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(new PdfTextExtractor(new PdfReader(vale)).getTextFromPage(1)).contains("LIC. ANA PÉREZ")
                .doesNotContain("OTRA PERSONA");
    }

    @Test
    void rechazoRequiereComentarioYCancelacionSoloDelSolicitante() throws Exception {
        String tokenA = login("usuarioa@x.mx");
        String tokenPat = login("patrimonio@x.mx");
        long id1 = idDe(solicitar(tokenA, bienA.getId(), areaB.getId()).andExpect(status().isCreated()).andReturn());

        mvc.perform(patch("/api/traspasos/" + id1 + "/rechazar").contentType(MediaType.APPLICATION_JSON)
                .content("{}").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/traspasos/" + id1 + "/rechazar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"comentario\":\"Falta oficio\"}").header(HttpHeaders.AUTHORIZATION, bearer(tokenPat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("RECHAZADA"));

        long id2 = idDe(solicitar(tokenA, bienA.getId(), areaB.getId()).andExpect(status().isCreated()).andReturn());
        // El área destino ve la solicitud pero no puede cancelarla
        mvc.perform(patch("/api/traspasos/" + id2 + "/cancelar")
                .header(HttpHeaders.AUTHORIZATION, bearer(login("usuariob@x.mx"))))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/traspasos/" + id2 + "/cancelar").header(HttpHeaders.AUTHORIZATION, bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("CANCELADA"));
    }

    @Test
    void autorizacionesSimultaneasRecibenFoliosDistintos() throws Exception {
        String tokenA = login("usuarioa@x.mx");
        String tokenPat = login("patrimonio@x.mx");
        long id1 = idDe(solicitar(tokenA, bienA.getId(), areaB.getId()).andReturn());
        long id2 = idDe(solicitar(tokenA, bienA2.getId(), areaB.getId()).andReturn());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> tareas = List.of(id1, id2).stream().<Callable<Integer>>map(id -> () -> {
                MvcResult r = mvc.perform(patch("/api/traspasos/" + id + "/autorizar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenPat))).andReturn();
                assertThat(r.getResponse().getStatus()).isEqualTo(200);
                return JsonPath.read(r.getResponse().getContentAsString(), "$.folio");
            }).toList();
            List<Integer> folios = pool.invokeAll(tareas).stream().map(SeguridadYTraspasosTests::valor).toList();
            assertThat(folios).containsExactlyInAnyOrder(1, 2);
        } finally {
            pool.shutdown();
        }
    }

    // ---------- Utilidades ----------

    private org.springframework.test.web.servlet.ResultActions solicitar(String token, Long bienId, Long destinoId)
            throws Exception {
        String body = """
                {"bienIds":[%d],"areaDestinoId":%d,"oficioPeticion":"DPV/1/2026"}
                """.formatted(bienId, destinoId);
        return mvc.perform(post("/api/traspasos").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private String login(String correo) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"" + correo + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
    }

    private static long idDe(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static <T> T valor(Future<T> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Area area(String nombre, DireccionGeneral dg) {
        Area a = new Area();
        a.setNombre(nombre);
        a.setNombreCargo("Titular de " + nombre); // firma el vale de traspaso
        a.setDireccionGeneral(dg);
        return areas.save(a);
    }

    private void usuario(String correo, RolNombre rol, Area area, boolean activo) {
        Usuario u = new Usuario();
        u.setNombre("Prueba");
        u.setApellidoPaterno(rol.name());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(PASSWORD));
        u.setRol(roles.findByNombre(rol).orElseThrow());
        u.setArea(area);
        u.setActivo(activo);
        usuarios.save(u);
    }

    private BienMueble mueble(String inventario, String descripcion, Area area) {
        BienMueble b = new BienMueble();
        b.setNoControlInventario(inventario);
        b.setDescripcion(descripcion);
        b.setMarca("AOC");
        b.setEstado("BUENO");
        b.setServidorPublico("Servidor Origen");
        b.setDocumentoSoporte("FACTURA");
        b.setNoFactura("F 168");
        b.setCosto(new BigDecimal("1500.00"));
        b.setArea(area);
        return muebles.save(b);
    }
}
