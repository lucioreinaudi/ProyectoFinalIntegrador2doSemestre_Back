package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Comprueba que la aplicacion arranca. Con el perfil "test" usa la base H2 en memoria
// (sin el perfil intentaba conectarse a Supabase y fallaba en "mvn test").
@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
