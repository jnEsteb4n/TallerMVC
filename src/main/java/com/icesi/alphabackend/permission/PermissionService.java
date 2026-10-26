package com.icesi.alphabackend.permission;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.common.Validations;
import com.icesi.alphabackend.role.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    public Permission create(String name, String description) {
        String cleanName = Validations.requireText(name, "name");
        if (permissionRepository.existsByName(cleanName)) {
            throw new BusinessException("Ya existe un permiso con nombre " + cleanName);
        }
        return permissionRepository.save(new Permission(cleanName, description));
    }

    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permiso", id));
    }

    public Permission update(Long id, String name, String description) {
        Permission permission = findById(id);
        String cleanName = Validations.requireText(name, "name");
        if (!cleanName.equals(permission.getName()) && permissionRepository.existsByName(cleanName)) {
            throw new BusinessException("Ya existe un permiso con nombre " + cleanName);
        }
        permission.setName(cleanName);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    public void delete(Long id) {
        Permission permission = findById(id);
        if (roleRepository.existsByPermissionsId(id)) {
            throw new BusinessException("El permiso " + permission.getName() + " está asignado a uno o más roles");
        }
        permissionRepository.delete(permission);
    }
}
