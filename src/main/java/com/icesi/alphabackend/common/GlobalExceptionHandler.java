package com.icesi.alphabackend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Muestra una página 404 cuando una vista pide un registro que no existe.
 * No captura Exception en general para no tragarse los AccessDeniedException
 * que Spring Security convierte en 403.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/not-found");
        mav.addObject("message", ex.getMessage());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }
}
