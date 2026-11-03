package com.adoptar.usuarios.controller;

import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de la cuenta del usuario logueado.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Mi cuenta", description = "Datos del usuario que inició sesión")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/yo")
    @Operation(summary = "Ver mi cuenta", description = "Devuelve los datos del usuario dueño del token.")
    public UsuarioResponse verMiCuenta(@AuthenticationPrincipal Jwt token) {
        return usuarioService.obtener(Long.valueOf(token.getSubject()));
    }
}
