package com.icesi.alphabackend.auth;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleRepository;
import com.icesi.alphabackend.security.SecurityUser;
import com.icesi.alphabackend.user.User;
import com.icesi.alphabackend.user.UserRepository;
import com.icesi.alphabackend.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio de autenticación:
 * <ul>
 *   <li>register: crea la cuenta con el rol por defecto USER. Reutiliza UserService.create,
 *       que valida el correo institucional, la longitud de la contraseña y la guarda con BCrypt.</li>
 *   <li>loadUserByUsername: carga el usuario por correo para que Spring Security lo autentique.</li>
 * </ul>
 */
@Service
@Transactional
public class AuthService implements UserDetailsService {

    static final String DEFAULT_ROLE = "USER";

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User register(String fullName, String email, String rawPassword,
                         String confirmPassword, String academicProgram) {
        if (!Objects.equals(rawPassword, confirmPassword)) {
            throw new BusinessException("Las contraseñas no coinciden");
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new NotFoundException("No existe el rol por defecto " + DEFAULT_ROLE));
        return userService.create(fullName, email, rawPassword, academicProgram, Set.of(defaultRole.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el correo " + cleanEmail));
        return new SecurityUser(user);
    }
}
