package com.icesi.alphabackend.user;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.common.Validations;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    static final String INSTITUTIONAL_DOMAIN = "@u.icesi.edu.co";
    static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Un usuario nunca se crea sin rol. */
    public User create(String fullName, String email, String rawPassword,
                       String academicProgram, Set<Long> roleIds) {
        String cleanName = Validations.requireText(fullName, "fullName");
        String cleanEmail = Validations.requireText(email, "email").toLowerCase();
        Validations.requireText(rawPassword, "password");
        if (!cleanEmail.endsWith(INSTITUTIONAL_DOMAIN)) {
            throw new BusinessException("El correo debe pertenecer al dominio " + INSTITUTIONAL_DOMAIN);
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("Un usuario debe tener al menos un rol");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BusinessException("Ya existe un usuario con el correo " + cleanEmail);
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new NotFoundException("Alguno de los roles indicados no existe");
        }
        User user = new User();
        user.setFullName(cleanName);
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setAcademicProgram(academicProgram);
        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el correo " + email));
    }

    public User update(Long id, String fullName, String academicProgram, String photoUrl) {
        User user = findById(id);
        user.setFullName(Validations.requireText(fullName, "fullName"));
        user.setAcademicProgram(academicProgram);
        user.setPhotoUrl(photoUrl);
        return userRepository.save(user);
    }

    public User setEnabled(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    public User addRole(Long userId, Long roleId) {
        User user = findById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Rol", roleId));
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    /** No permite dejar al usuario sin roles. */
    public User removeRole(Long userId, Long roleId) {
        User user = findById(userId);
        boolean assigned = user.getRoles().stream().anyMatch(r -> roleId.equals(r.getId()));
        if (!assigned) {
            throw new BusinessException("El usuario no tiene asignado el rol " + roleId);
        }
        if (user.getRoles().size() == 1) {
            throw new BusinessException("Un usuario no puede quedar sin roles");
        }
        user.getRoles().removeIf(r -> roleId.equals(r.getId()));
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.delete(findById(id));
    }
}
