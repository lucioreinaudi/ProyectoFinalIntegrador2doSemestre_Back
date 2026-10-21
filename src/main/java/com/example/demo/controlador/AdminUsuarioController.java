package com.example.demo.controlador;

import com.example.demo.dto.PaginaResponse;
import com.example.demo.dto.UsuarioAdminResponse;
import com.example.demo.service.UsuarioConsultaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Pestana Usuarios del panel: solo lectura (Q5). Solo municipio (/api/admin/**).
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioConsultaService usuarioConsultaService;

    public AdminUsuarioController(UsuarioConsultaService usuarioConsultaService) {
        this.usuarioConsultaService = usuarioConsultaService;
    }

    @GetMapping
    public PaginaResponse<UsuarioAdminResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) String rol,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return usuarioConsultaService.listar(texto, zonaId, rol, pagina, tamanio);
    }
}
