package com.icesi.alphabackend.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/** Formulario de rol. permissionIds solo se usa al crear (un rol nace con al menos un permiso). */
public class RoleForm {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String name;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String description;

    private Set<Long> permissionIds = new HashSet<>();

    public static RoleForm from(Role role) {
        RoleForm form = new RoleForm();
        form.setName(role.getName());
        form.setDescription(role.getDescription());
        return form;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
}
