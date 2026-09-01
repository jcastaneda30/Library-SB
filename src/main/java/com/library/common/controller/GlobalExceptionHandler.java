package com.library.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidArgument(IllegalArgumentException ex) {

        // 1. Creamos el objeto con el código HTTP y el detalle del error
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        // 2. Personalizamos los campos del estándar
        problemDetail.setTitle("Datos de entrada inválidos");
        problemDetail.setType(URI.create("https://tu-api.com/errores/datos-invalidos"));

        // 3. ¡Magia! Aquí puedes agregar tu código interno (como el 9756)
        problemDetail.setProperty("codigoInterno", 9756);

        return problemDetail;
    }
}
