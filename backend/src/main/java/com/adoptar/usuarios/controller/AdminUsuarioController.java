package com.adoptar.usuarios.controller;

import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.dto.Pagina;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.service.AdministracionUsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administración de cuentas. Solo ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@Tag(name = "Administración de cuentas", description = "Solo para el rol ADMIN")
public class AdminUsuarioController {

    private final AdministracionUsuariosService administracion;

    public AdminUsuarioController(AdministracionUsuariosService administracion) {
        this.administracion = administracion;
    }

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Paginado y ordenado por id. Se puede filtrar por rol.")
    public Pagina<UsuarioResponse> listar(@RequestParam(required = false) Rol rol,
                                          @RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "20") int tamanio) {
        return administracion.listar(rol, pagina, tamanio);
    }

    @PostMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar una cuenta",
            description = "No se borra: deja de poder iniciar sesión y su token deja de servir. "
                    + "Responde 409 si el admin intenta desactivarse a sí mismo.")
    public UsuarioResponse desactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        return administracion.desactivar(id, Long.valueOf(token.getSubject()));
    }

    @PostMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar una cuenta")
    public UsuarioResponse reactivar(@PathVariable Long id) {
        return administracion.reactivar(id);
    }
}
