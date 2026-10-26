package com.icesi.alphabackend.user;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * GET exige USER_READ (SecurityFilterChain); toda modificación exige USER_WRITE.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final String FORM = "userForm";

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // ---------- Listar ----------

    @GetMapping
    public String list(Model model) {
        List<User> users = userService.findAll().stream()
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("users", users);
        return "users/list";
    }

    // ---------- Editar ----------

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, UserForm.from(user));
        }
        model.addAttribute("user", user);
        return "users/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute(FORM) UserForm form,
                         BindingResult result,
                         Authentication authentication,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/users/" + id + "/edit";
        }
        try {
            User user = userService.findById(id);
            if (!form.isEnabled() && user.getEmail().equals(authentication.getName())) {
                throw new BusinessException("No puedes desactivar tu propia cuenta");
            }
            userService.update(id, form.getFullName(), form.getAcademicProgram(), form.getPhotoUrl());
            if (user.isEnabled() != form.isEnabled()) {
                userService.setEnabled(id, form.isEnabled());
            }
            redirect.addFlashAttribute("success", "Usuario " + form.getFullName() + " actualizado");
            return "redirect:/users";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/users/" + id + "/edit";
        }
    }

    // ---------- Eliminar ----------

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
        User user = userService.findById(id);
        if (user.getEmail().equals(authentication.getName())) {
            redirect.addFlashAttribute("error", "No puedes eliminar tu propia cuenta");
            return "redirect:/users";
        }
        try {
            userService.delete(id);
            redirect.addFlashAttribute("success", "Usuario " + user.getFullName() + " eliminado");
        } catch (DataAccessException e) {
            // Tiene cuentas, movimientos u otra información financiera asociada (llaves foráneas)
            redirect.addFlashAttribute("error", user.getFullName()
                    + " tiene información financiera registrada. Desactiva la cuenta en lugar de eliminarla.");
        }
        return "redirect:/users";
    }

    // ---------- Roles del usuario: asociar y quitar ----------

    @GetMapping("/{id}/roles")
    public String roles(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        List<Role> availableRoles = roleService.findAll().stream()
                .filter(role -> user.getRoles().stream().noneMatch(r -> r.getId().equals(role.getId())))
                .sorted(Comparator.comparing(Role::getName))
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("availableRoles", availableRoles);
        return "users/roles";
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public String addRole(@PathVariable Long id, @RequestParam Long roleId, RedirectAttributes redirect) {
        try {
            userService.addRole(id, roleId);
            redirect.addFlashAttribute("success",
                    "Rol asignado. El usuario lo verá en su próximo inicio de sesión.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + id + "/roles";
    }

    @PostMapping("/{id}/roles/{roleId}/remove")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public String removeRole(@PathVariable Long id, @PathVariable Long roleId, RedirectAttributes redirect) {
        try {
            userService.removeRole(id, roleId);
            redirect.addFlashAttribute("success", "Rol quitado");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + id + "/roles";
    }

    private void keepForm(RedirectAttributes redirect, UserForm form, BindingResult result) {
        redirect.addFlashAttribute(FORM, form);
        if (result != null) {
            redirect.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM, result);
        }
    }
}
