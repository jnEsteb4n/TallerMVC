package com.icesi.alphabackend.role;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.permission.Permission;
import com.icesi.alphabackend.permission.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * GET exige ROLE_READ (SecurityFilterChain); toda modificación exige ROLE_WRITE.
 * Eliminar un rol exige además el rol ADMIN.
 */
@Controller
@RequestMapping("/roles")
public class RoleController {

    private static final String FORM = "roleForm";

    private final RoleService roleService;
    private final PermissionService permissionService;

    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roleService.findAll().stream()
                .sorted(Comparator.comparing(Role::getName))
                .toList());
        return "roles/list";
    }

    // ---------- Crear (con sus permisos iniciales) ----------

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String createForm(Model model) {
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, new RoleForm());
        }
        model.addAttribute("permissions", sortedPermissions());
        return "roles/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String create(@Valid @ModelAttribute(FORM) RoleForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/roles/new";
        }
        try {
            Role role = roleService.create(form.getName(), form.getDescription(), form.getPermissionIds());
            redirect.addFlashAttribute("success", "Rol " + role.getName() + " creado");
            return "redirect:/roles/" + role.getId() + "/permissions";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/roles/new";
        }
    }

    // ---------- Editar nombre y descripción ----------

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String editForm(@PathVariable Long id, Model model) {
        Role role = roleService.findById(id);
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, RoleForm.from(role));
        }
        model.addAttribute("roleId", id);
        return "roles/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute(FORM) RoleForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/roles/" + id + "/edit";
        }
        try {
            Role role = roleService.update(id, form.getName(), form.getDescription());
            redirect.addFlashAttribute("success", "Rol " + role.getName() + " actualizado");
            return "redirect:/roles";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/roles/" + id + "/edit";
        }
    }

    // ---------- Eliminar ----------

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_WRITE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Role role = roleService.findById(id);
            roleService.delete(id);
            redirect.addFlashAttribute("success", "Rol " + role.getName() + " eliminado");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles";
    }

    // ---------- Permisos del rol: asociar y quitar ----------

    @GetMapping("/{id}/permissions")
    public String permissions(@PathVariable Long id, Model model) {
        Role role = roleService.findById(id);
        List<Permission> availablePermissions = sortedPermissions().stream()
                .filter(p -> role.getPermissions().stream().noneMatch(rp -> rp.getId().equals(p.getId())))
                .toList();
        model.addAttribute("role", role);
        model.addAttribute("assignedPermissions", role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .toList());
        model.addAttribute("availablePermissions", availablePermissions);
        return "roles/permissions";
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String addPermission(@PathVariable Long id, @RequestParam Long permissionId,
                                RedirectAttributes redirect) {
        try {
            roleService.addPermission(id, permissionId);
            redirect.addFlashAttribute("success", "Permiso asociado al rol");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles/" + id + "/permissions";
    }

    @PostMapping("/{id}/permissions/{permissionId}/remove")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public String removePermission(@PathVariable Long id, @PathVariable Long permissionId,
                                   RedirectAttributes redirect) {
        try {
            roleService.removePermission(id, permissionId);
            redirect.addFlashAttribute("success", "Permiso quitado del rol");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles/" + id + "/permissions";
    }

    private List<Permission> sortedPermissions() {
        return permissionService.findAll().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .toList();
    }

    private void keepForm(RedirectAttributes redirect, RoleForm form, BindingResult result) {
        redirect.addFlashAttribute(FORM, form);
        if (result != null) {
            redirect.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM, result);
        }
    }
}
