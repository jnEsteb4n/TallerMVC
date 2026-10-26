package com.icesi.alphabackend.security;

import com.icesi.alphabackend.permission.Permission;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Adaptador entre la entidad User y el UserDetails de Spring Security.
 * El "username" es el correo institucional. Copia los datos en vez de guardar
 * la entidad para que se pueda serializar en la sesión.
 */
public class SecurityUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String email;
    private final String fullName;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.authorities = Collections.unmodifiableSet(buildAuthorities(user));
    }

    /**
     * Cada rol se entrega como ROLE_<nombre> (lo evalúa hasRole) y cada permiso
     * con su nombre tal cual, p. ej. USER_READ (lo evalúa hasAuthority).
     */
    private static Set<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> result = new HashSet<>();
        for (Role role : user.getRoles()) {
            result.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                result.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return result;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
}
