package com.icesi.alphabackend.role;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.common.Validations;
import com.icesi.alphabackend.permission.Permission;
import com.icesi.alphabackend.permission.PermissionRepository;
import com.icesi.alphabackend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    /** Un rol nunca se crea sin permisos. */
    public Role create(String name, String description, Set<Long> permissionIds) {
        String cleanName = Validations.requireText(name, "name");
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException("Un rol debe tener al menos un permiso");
        }
        if (roleRepository.existsByName(cleanName)) {
            throw new BusinessException("Ya existe un rol con nombre " + cleanName);
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new NotFoundException("Alguno de los permisos indicados no existe");
        }
        Role role = new Role(cleanName, description);
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol", id));
    }

    public Role update(Long id, String name, String description) {
        Role role = findById(id);
        String cleanName = Validations.requireText(name, "name");
        if (!cleanName.equals(role.getName()) && roleRepository.existsByName(cleanName)) {
            throw new BusinessException("Ya existe un rol con nombre " + cleanName);
        }
        role.setName(cleanName);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    public Role addPermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("Permiso", permissionId));
        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    /** No permite dejar el rol sin permisos. */
    public Role removePermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        boolean assigned = role.getPermissions().stream().anyMatch(p -> permissionId.equals(p.getId()));
        if (!assigned) {
            throw new BusinessException("El rol no tiene asignado el permiso " + permissionId);
        }
        if (role.getPermissions().size() == 1) {
            throw new BusinessException("Un rol no puede quedar sin permisos");
        }
        role.getPermissions().removeIf(p -> permissionId.equals(p.getId()));
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        Role role = findById(id);
        if (userRepository.countByRolesId(id) > 0) {
            throw new BusinessException("El rol " + role.getName() + " está asignado a uno o más usuarios");
        }
        roleRepository.delete(role);
    }
}
