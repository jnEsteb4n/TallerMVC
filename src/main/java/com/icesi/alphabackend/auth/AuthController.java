package com.icesi.alphabackend.auth;

import com.icesi.alphabackend.common.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * El POST /login lo procesa Spring Security (formLogin); aquí solo se muestra la vista.
 */
@Controller
public class AuthController {

    private static final String FORM = "registerForm";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Authentication authentication, Model model) {
        if (isLoggedIn(authentication)) {
            return "redirect:/";
        }
        if (!model.containsAttribute(FORM)) {
            model.addAttribute(FORM, new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute(FORM) RegisterForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            keepForm(redirect, form, result);
            return "redirect:/register";
        }
        try {
            authService.register(form.getFullName(), form.getEmail(), form.getPassword(),
                    form.getConfirmPassword(), form.getAcademicProgram());
            redirect.addFlashAttribute("success", "Cuenta creada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            keepForm(redirect, form, null);
            return "redirect:/register";
        }
    }

    /** Devuelve el formulario (sin contraseñas) y sus errores a la vista tras el redirect. */
    private void keepForm(RedirectAttributes redirect, RegisterForm form, BindingResult result) {
        form.setPassword("");
        form.setConfirmPassword("");
        redirect.addFlashAttribute(FORM, form);
        if (result != null) {
            redirect.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM, result);
        }
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
