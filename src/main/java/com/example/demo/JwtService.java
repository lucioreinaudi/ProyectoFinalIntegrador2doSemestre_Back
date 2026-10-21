package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.example.demo.modelo.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Genera y valida el token de sesion.
@Component
public class JwtService {

    private final SecretKey clavePrivada;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                       @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.clavePrivada = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    // El rol va como claim del token, para que el filtro pueda armar las authorities
    // sin ir a buscar al usuario a la base en cada pedido. Esto es seguro en este
    // proyecto porque el rol de un usuario nunca cambia una vez creado (el unico
    // usuario municipio se crea por SQL, no hay reasignacion de roles).
    public String generarToken(String email, Rol rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol.name())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clavePrivada)
                .compact();
    }

    // Devuelve los claims si el token es valido y no expiro; null en cualquier otro caso
    // (firma invalida, token vencido, formato invalido, etc.). No tira excepcion a proposito:
    // quien llama (el filtro) solo necesita saber si hay o no un usuario autenticado.
    public Claims validarClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(clavePrivada)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
