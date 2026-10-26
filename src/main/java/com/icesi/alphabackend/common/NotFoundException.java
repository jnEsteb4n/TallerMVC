package com.icesi.alphabackend.common;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entity, Long id) {
        super(entity + " con id " + id + " no existe");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
