package com.icesi.alphabackend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos del formulario de registro. Las reglas de negocio finales las valida UserService. */
public class RegisterForm {

    @NotBlank(message = "Escribe tu nombre completo")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String fullName;

    @NotBlank(message = "Escribe tu correo institucional")
    @Email(message = "Escribe un correo válido")
    private String email;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String academicProgram;

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Confirma la contraseña")
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAcademicProgram() { return academicProgram; }
    public void setAcademicProgram(String academicProgram) { this.academicProgram = academicProgram; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
