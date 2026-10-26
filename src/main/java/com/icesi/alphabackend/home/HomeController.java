package com.icesi.alphabackend.home;

import com.icesi.alphabackend.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        String displayName = authentication.getPrincipal() instanceof SecurityUser user
                ? user.getFullName()
                : authentication.getName();

        model.addAttribute("displayName", displayName);
        model.addAttribute("email", authentication.getName());
        model.addAttribute("myRoles", authorities.stream()
                .filter(a -> a.startsWith("ROLE_") && !isPermissionName(a))
                .map(a -> a.substring("ROLE_".length()))
                .toList());
        model.addAttribute("myPermissions", authorities.stream()
                .filter(a -> !a.startsWith("ROLE_") || isPermissionName(a))
                .toList());
        return "index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    /** Los permisos ROLE_READ y ROLE_WRITE empiezan por ROLE_ pero no son roles. */
    private boolean isPermissionName(String authority) {
        return authority.equals("ROLE_READ") || authority.equals("ROLE_WRITE");
    }
}
