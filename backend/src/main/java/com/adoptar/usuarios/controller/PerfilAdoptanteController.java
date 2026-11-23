package com.adoptar.usuarios.controller;

import com.adoptar.usuarios.dto.PerfilAdoptanteRequest;
import com.adoptar.usuarios.dto.PerfilAdoptanteResponse;
import com.adoptar.usuarios.service.PerfilAdoptanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil del adoptante logueado. Solo para el rol ADOPTANTE (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/usuarios/yo/perfil")
@Tag(name = "Mi perfil de adoptante", description = "Datos que el adoptante completa antes de enviar solicitudes")
public class PerfilAdoptanteController {

    private final PerfilAdoptanteService perfilService;

    public PerfilAdoptanteController(PerfilAdoptanteService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @Operation(summary = "Ver mi perfil (CU-04)", description = "Responde 404 si todavía no lo completé.")
    public PerfilAdoptanteResponse verMiPerfil(@AuthenticationPrincipal Jwt token) {
        return perfilService.obtener(Long.valueOf(token.getSubject()));
    }

    @PutMapping
    @Operation(summary = "Completar o editar mi perfil (CU-04)",
            description = "La primera vez lo crea y las siguientes lo actualiza.")
    public PerfilAdoptanteResponse guardarMiPerfil(@AuthenticationPrincipal Jwt token,
                                                   @Valid @RequestBody PerfilAdoptanteRequest datos) {
        return perfilService.guardar(Long.valueOf(token.getSubject()), datos);
    }
}
