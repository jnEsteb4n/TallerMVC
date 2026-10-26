package com.icesi.alphabackend.permission;

import com.icesi.alphabackend.common.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

/**
 * GET exige PERMISSION_READ (SecurityFilterChain); toda modificación exige PERMISSION_WRITE.
 */
@Controller
@RequestMapping("/permissions")
public class PermissionController {

    private static final String FORM = "permissionForm";

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("permissions", permissionService.findAll().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .toList());
        return "permissions/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public String createForm(Model model) {
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, new PermissionForm());
        }
        return "permissions/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public String create(@Valid @ModelAttribute(FORM) PermissionForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/permissions/new";
        }
        try {
            Permission permission = permissionService.create(form.getName(), form.getDescription());
            redirect.addFlashAttribute("success", "Permiso " + permission.getName() + " creado");
            return "redirect:/permissions";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/permissions/new";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public String editForm(@PathVariable Long id, Model model) {
        Permission permission = permissionService.findById(id);
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, PermissionForm.from(permission));
        }
        model.addAttribute("permissionId", id);
        return "permissions/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute(FORM) PermissionForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/permissions/" + id + "/edit";
        }
        try {
            Permission permission = permissionService.update(id, form.getName(), form.getDescription());
            redirect.addFlashAttribute("success", "Permiso " + permission.getName() + " actualizado");
            return "redirect:/permissions";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/permissions/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Permission permission = permissionService.findById(id);
            permissionService.delete(id);
            redirect.addFlashAttribute("success", "Permiso " + permission.getName() + " eliminado");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/permissions";
    }

    private void keepForm(RedirectAttributes redirect, PermissionForm form, BindingResult result) {
        redirect.addFlashAttribute(FORM, form);
        if (result != null) {
            redirect.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM, result);
        }
    }
}
