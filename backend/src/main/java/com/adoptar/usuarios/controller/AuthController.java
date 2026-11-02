package com.adoptar.usuarios.controller;

import com.adoptar.usuarios.dto.LoginRequest;
import com.adoptar.usuarios.dto.LoginResponse;
import com.adoptar.usuarios.dto.RegistroAdoptanteRequest;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de acceso: registro e inicio de sesión.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Acceso", description = "Registro e inicio de sesión")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro/adoptante")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar adoptante (CU-01)",
            description = "Crea la cuenta con rol ADOPTANTE. Responde 409 si el mail ya está registrado.")
    public UsuarioResponse registrarAdoptante(@Valid @RequestBody RegistroAdoptanteRequest datos) {
        return usuarioService.registrarAdoptante(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Devuelve el token para mandar en cada pedido. Responde 401 si el mail o la contraseña no coinciden.")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }
}
