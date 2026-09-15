package com.universidad.tareas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de errores para los controladores REST.
 * Intercepta excepciones de validación y las transforma en respuestas JSON
 * estructuradas, con un campo por cada error de validación encontrado.
 *
 * <p>En el controlador MVC, la validación se maneja con {@code BindingResult}
 * para que los errores se rendericen en el HTML nativo de Thymeleaf.
 * En la capa REST, este {@code @RestControllerAdvice} produce JSON,
 * manteniendo cada capa respondiendo en su formato natural.</p>
 */
@RestControllerAdvice(basePackageClasses = TareaApiController.class)
public class ApiErrorHandler {

    /**
     * Captura las excepciones de validación de Bean Validation ({@code @Valid})
     * y construye un mapa estructurado de errores campo a campo.
     *
     * @param ex la excepción lanzada por Spring cuando falla la validación del cuerpo JSON
     * @return mapa {@code campo -> mensaje de error} con HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarErroresValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return errores;
    }
}
