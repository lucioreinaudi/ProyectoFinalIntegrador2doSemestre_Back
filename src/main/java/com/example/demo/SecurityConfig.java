package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Configuracion de Spring Security.
// Publicos: login, registro y el clima (no se puede pedir un token para conseguir
// un token, ni estar logueado para poder registrarse; el clima es informacion
// publica). Todo lo demas exige un JWT valido, que valida JwtAuthenticationFilter
// en cada pedido.
//
// Restricciones por rol: solo cubren lo que se puede decidir mirando el metodo y
// el path (ver el detalle de cada bloque mas abajo). Las reglas que dependen de
// datos (por ejemplo "un vecino no puede crear un reclamo", o "no puede ver el
// historial de un reclamo ajeno") estan adentro de los controladores
// correspondientes, porque SecurityConfig no puede saber de quien es cada fila.
//
// El orden de las reglas importa: Spring Security usa la PRIMERA que matchea.
// Por eso /api/reclamos/me va antes que /api/reclamos/*, si no esta ultima
// (que es para el detalle por id, solo municipio) la tapa.
//
// El CORS se configura ACA, no en un WebMvcConfigurer aparte (asi estaba antes,
// en CorsConfig.java, que por eso se borro). Spring Security intercepta los
// pedidos antes que el resto de la app: una configuracion de CORS fuera de la
// cadena de seguridad no llega a aplicarse al preflight (OPTIONS), y el
// navegador lo bloquea con "No Access-Control-Allow-Origin header".
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Origenes permitidos, separados por coma (variable CORS_ORIGINS).
    @Value("${cors.origins:http://localhost:5173}")
    private String corsOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin token, o con uno vencido/invalido (el filtro JWT no autentica): 401.
                // Si no se configura, Spring Security responde 403 tambien en ese caso, y el
                // front (client.js) no se entera de que la sesion vencio. El 403 queda solo
                // para "autenticado pero sin el rol" (ej. un vecino en /api/admin/**).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clima/**").permitAll()

                        // Las alertas se ven sin login (Home y detalle publico): las activas
                        // y el detalle de una por id. El resto de /api/alertas sigue con token.
                        .requestMatchers(HttpMethod.GET, "/api/alertas/estado/activa").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/alertas/*").permitAll()

                        // Zonas y categorias activas: publicas (filtro de zonas del Home sin
                        // login y formularios). Las dadas de baja solo las ve el panel.
                        .requestMatchers(HttpMethod.GET, "/api/zonas", "/api/zonas/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/*").permitAll()
                        // Tipos de alerta: el Home los usa para el icono y la imagen de referencia.
                        .requestMatchers(HttpMethod.GET, "/api/tipos-alerta", "/api/tipos-alerta/*").permitAll()

                        // "Lo mio" del vecino: va antes que las reglas de abajo, para que no lo tapen.
                        .requestMatchers(HttpMethod.GET, "/api/reclamos/me").authenticated()

                        // Reclamos ajenos: solo municipio. Cubre el listado completo, por usuario,
                        // por zona, por categoria, y el detalle por id.
                        .requestMatchers(HttpMethod.GET, "/api/reclamos").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.GET, "/api/reclamos/usuario/**").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.GET, "/api/reclamos/zona/**").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.GET, "/api/reclamos/categoria/**").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.GET, "/api/reclamos/*").hasRole("MUNICIPIO")

                        // Listado de usuarios y notificaciones ajenas: solo municipio.
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.GET, "/api/notificaciones/usuario/**").hasRole("MUNICIPIO")

                        // Crear/cambiar alertas, vinculos, estados de reclamo, zonas y categorias: solo municipio.
                        .requestMatchers(HttpMethod.POST, "/api/alertas").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/alertas/*/estado").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PUT, "/api/alertas/*").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/alertas/*").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.POST, "/api/alertas-reclamos").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/alertas-reclamos/**").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.POST, "/api/estados-reclamo").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.POST, "/api/zonas").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.POST, "/api/categorias").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PUT, "/api/zonas/*", "/api/categorias/*").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/zonas/*/activa", "/api/categorias/*/activa").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.POST, "/api/tipos-alerta").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PUT, "/api/tipos-alerta/*").hasRole("MUNICIPIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/tipos-alerta/*/activa").hasRole("MUNICIPIO")

                        // Todo el panel de administracion (cualquier metodo): solo municipio.
                        .requestMatchers("/api/admin/**").hasRole("MUNICIPIO")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
