package com.icesi.alphabackend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario de edición de usuario (el correo y la contraseña no se editan aquí). */
public class UserForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String fullName;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String academicProgram;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String photoUrl;

    private boolean enabled = true;

    public static UserForm from(User user) {
        UserForm form = new UserForm();
        form.setFullName(user.getFullName());
        form.setAcademicProgram(user.getAcademicProgram());
        form.setPhotoUrl(user.getPhotoUrl());
        form.setEnabled(user.isEnabled());
        return form;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAcademicProgram() { return academicProgram; }
    public void setAcademicProgram(String academicProgram) { this.academicProgram = academicProgram; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
