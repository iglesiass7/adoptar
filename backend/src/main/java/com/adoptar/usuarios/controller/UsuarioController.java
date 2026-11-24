package com.adoptar.usuarios.controller;

import com.adoptar.usuarios.dto.ActualizarDatosRequest;
import com.adoptar.usuarios.dto.CambioPasswordRequest;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de la cuenta del usuario logueado (CU-05).
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

    @PutMapping("/yo")
    @Operation(summary = "Editar mis datos (CU-05)", description = "Cambia el nombre y el teléfono. El mail y el rol no se cambian.")
    public UsuarioResponse editarMisDatos(@AuthenticationPrincipal Jwt token,
                                          @Valid @RequestBody ActualizarDatosRequest datos) {
        return usuarioService.actualizarDatos(Long.valueOf(token.getSubject()), datos);
    }

    @PutMapping("/yo/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambiar mi contraseña (CU-05)",
            description = "Pide la contraseña actual. Responde 400 si no coincide.")
    public void cambiarMiPassword(@AuthenticationPrincipal Jwt token,
                                  @Valid @RequestBody CambioPasswordRequest datos) {
        usuarioService.cambiarPassword(Long.valueOf(token.getSubject()), datos);
    }
}
