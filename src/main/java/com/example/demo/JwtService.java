package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    public String generarToken(String email) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(email)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clavePrivada)
                .compact();
    }

    // Devuelve el email (subject) si el token es valido y no expiro; null en cualquier otro caso
    // (firma invalida, token vencido, formato invalido, etc.). No tira excepcion a proposito:
    // quien llama (el filtro) solo necesita saber si hay o no un usuario autenticado.
    public String validarYObtenerEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clavePrivada)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
