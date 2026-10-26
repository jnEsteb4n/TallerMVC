package com.icesi.alphabackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración de la capa web: levanta la aplicación con la base de pruebas
 * (datos semilla de data.sql) y verifica autenticación y autorización.
 * Es @Transactional: lo que se crea en cada prueba se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityWebIntegrationTest {

    private static final String ADMIN = "admin@u.icesi.edu.co";
    private static final String ANA = "ana.torres@u.icesi.edu.co";
    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    // ---------- Autenticación ----------

    @Test
    void loginAndRegisterPagesArePublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
        mockMvc.perform(get("/register")).andExpect(status().isOk()).andExpect(view().name("auth/register"));
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void seededAdminCanLogInWithEmail() throws Exception {
        mockMvc.perform(formLogin("/login").userParameter("email").user(ADMIN).password(PASSWORD))
                .andExpect(authenticated().withUsername(ADMIN))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin("/login").userParameter("email").user(ADMIN).password("incorrecta"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void registerCreatesAccountThatCanLogIn() throws Exception {
        String email = "nuevo" + System.nanoTime() + "@u.icesi.edu.co";

        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Usuario Nuevo")
                        .param("email", email)
                        .param("academicProgram", "Ingeniería de Sistemas")
                        .param("password", PASSWORD)
                        .param("confirmPassword", PASSWORD))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));

        mockMvc.perform(formLogin("/login").userParameter("email").user(email).password(PASSWORD))
                .andExpect(authenticated().withUsername(email));
    }

    @Test
    void registerWithNonInstitutionalEmailShowsError() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Externo")
                        .param("email", "externo@gmail.com")
                        .param("password", PASSWORD)
                        .param("confirmPassword", PASSWORD))
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "X").param("email", "x@u.icesi.edu.co")
                        .param("password", PASSWORD).param("confirmPassword", PASSWORD))
                .andExpect(status().isForbidden());
    }

    // ---------- Autorización con usuarios reales de data.sql ----------

    @Test
    @WithUserDetails(ANA)
    void userRoleCanListUsersButNotRoles() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(view().name("users/list"));
        mockMvc.perform(get("/roles")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ANA)
    void userRoleCannotOpenEditFormBecauseOfPreAuthorize() throws Exception {
        mockMvc.perform(get("/users/2/edit")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN)
    void adminCanSeeRolePermissions() throws Exception {
        mockMvc.perform(get("/roles/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("roles/permissions"));
    }

    @Test
    @WithUserDetails(ADMIN)
    void deletingRoleAssignedToUsersShowsBusinessError() throws Exception {
        mockMvc.perform(post("/roles/2/delete").with(csrf()))
                .andExpect(redirectedUrl("/roles"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithUserDetails(ADMIN)
    void removingLastRoleOfUserShowsBusinessError() throws Exception {
        mockMvc.perform(post("/users/2/roles/2/remove").with(csrf()))
                .andExpect(redirectedUrl("/users/2/roles"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "editor@u.icesi.edu.co", authorities = {"ROLE_READ", "ROLE_WRITE"})
    void deletingRoleRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/roles/2/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
