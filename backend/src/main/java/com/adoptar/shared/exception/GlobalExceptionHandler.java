package com.adoptar.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único: ProblemDetail
 * (RFC 9457, antes RFC 7807). Así ningún controller tiene que armar errores a mano.
 * <p>
 * Extender {@link ResponseEntityExceptionHandler} hace que los errores propios de
 * Spring MVC (por ejemplo, un {@code @Valid} que falla) salgan con el mismo formato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ReglaDeNegocioException.class)
    ProblemDetail manejarReglaDeNegocio(ReglaDeNegocioException ex) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio violada", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ProblemDetail manejarAccesoDenegado(AccesoDenegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
