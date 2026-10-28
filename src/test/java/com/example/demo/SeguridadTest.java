package com.example.demo;

import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests de seguridad (SCRUM-95): login y permisos por rol.
// MockMvc llama a los endpoints como si fueran requests HTTP reales (pasando por toda
// la cadena de Spring Security y el filtro JWT), sin levantar un servidor.
// Usan el perfil "test": base H2 en memoria, nunca la de Supabase.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    // El municipio no se puede registrar por la API (el registro siempre crea vecinos),
    // asi que el test lo crea directo en la base, como se hace con SQL en la real.
    private static final String EMAIL_MUNICIPIO = "municipio@test.com";
    private static final String PASS_MUNICIPIO = "Municipio1234!";

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // La base en memoria se comparte entre los tests: se crea solo si no existe.
    @BeforeEach
    void crearMunicipio() {
        if (usuarioRepository.findByEmail(EMAIL_MUNICIPIO).isEmpty()) {
            usuarioRepository.save(new Usuario(EMAIL_MUNICIPIO, "Municipio",
                    passwordEncoder.encode(PASS_MUNICIPIO), Rol.municipio));
        }
    }

    // Registro publico (POST /api/usuarios): siempre crea un vecino.
    private void registrarVecino(String email, String pass) throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "nombre": "Vecino de prueba", "password": "%s"}
                                """.formatted(email, pass)))
                .andExpect(status().isCreated());
    }

    // Login (POST /api/auth/login): devuelve { usuario, token }. Se queda con el token.
    private String login(String email, String pass) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoLogin(email, pass)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private String cuerpoLogin(String email, String pass) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, pass);
    }

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        registrarVecino("ok@test.com", "Clave1234!");
        assertFalse(login("ok@test.com", "Clave1234!").isBlank());
    }

    @Test
    void loginConPasswordIncorrectaDa401() throws Exception {
        registrarVecino("mal@test.com", "Clave1234!");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoLogin("mal@test.com", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    // Sin token, un endpoint que exige sesion responde 401 (SCRUM-96).
    @Test
    void sinTokenDa401() throws Exception {
        mvc.perform(get("/api/reclamos/me"))
                .andExpect(status().isUnauthorized());
    }

    // Logueado pero sin el rol: un vecino en el panel responde 403 (SCRUM-96).
    @Test
    void vecinoEnZonaDeAdminDa403() throws Exception {
        registrarVecino("vecino@test.com", "Clave1234!");
        String token = login("vecino@test.com", "Clave1234!");
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void municipioEnZonaDeAdminDa200() throws Exception {
        String token = login(EMAIL_MUNICIPIO, PASS_MUNICIPIO);
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
