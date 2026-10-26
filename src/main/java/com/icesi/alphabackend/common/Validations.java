package com.icesi.alphabackend.common;

public final class Validations {

    private Validations() {
    }

    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("El campo " + field + " es obligatorio");
        }
        return value.trim();
    }
}
