package com.adoptar.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Respuestas de los errores 401 y 403 que corta Spring Security antes de llegar a un controller.
 * <p>
 * Salen con el mismo formato ProblemDetail que el resto de los errores de la API
 * (ver {@code GlobalExceptionHandler}), así el frontend los muestra igual. Se mantiene además el
 * encabezado estándar {@code WWW-Authenticate}.
 */
@Component
public class ErroresDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final AuthenticationEntryPoint estandar401 = new BearerTokenAuthenticationEntryPoint();
    private final AccessDeniedHandler estandar403 = new BearerTokenAccessDeniedHandler();
    private final JsonMapper json;

    public ErroresDeSeguridad(JsonMapper json) {
        this.json = json;
    }

    /** 401: no mandó token, o el que mandó no sirve. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException, ServletException {
        estandar401.commence(request, response, ex);
        String detalle = request.getHeader(HttpHeaders.AUTHORIZATION) == null
                ? "Falta el token. Iniciá sesión en POST /api/v1/auth/login y mandalo en el encabezado "
                        + "Authorization: Bearer <token>"
                : "El token no es válido: venció, está mal copiado o la cuenta fue desactivada. Iniciá sesión de nuevo";
        escribir(request, response, HttpStatus.UNAUTHORIZED, "No autenticado", detalle);
    }

    /** 403: el token es válido, pero su rol no puede entrar a esa ruta. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        estandar403.handle(request, response, ex);
        escribir(request, response, HttpStatus.FORBIDDEN, "Acceso denegado", "Tu rol no tiene permiso para esta acción");
    }

    private void escribir(HttpServletRequest request, HttpServletResponse response, HttpStatus estado,
                          String titulo, String detalle) throws IOException {
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("type", "about:blank");
        problema.put("title", titulo);
        problema.put("status", estado.value());
        problema.put("detail", detalle);
        problema.put("instance", request.getRequestURI());

        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), problema);
    }
}
