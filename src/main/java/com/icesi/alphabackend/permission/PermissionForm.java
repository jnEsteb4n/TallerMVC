package com.icesi.alphabackend.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PermissionForm {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String name;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String description;

    public static PermissionForm from(Permission permission) {
        PermissionForm form = new PermissionForm();
        form.setName(permission.getName());
        form.setDescription(permission.getDescription());
        return form;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
