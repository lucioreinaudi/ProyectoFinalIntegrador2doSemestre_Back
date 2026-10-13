package com.example.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Genera el token de sesion. Todavia nada lo exige (ver AuthController):
// eso es la proxima card (validar el JWT en los endpoints protegidos).
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
}
