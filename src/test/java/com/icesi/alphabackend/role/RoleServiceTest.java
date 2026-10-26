package com.icesi.alphabackend.role;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.permission.Permission;
import com.icesi.alphabackend.permission.PermissionRepository;
import com.icesi.alphabackend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RoleService service;

    private Permission permission(Long id) {
        Permission p = new Permission("P" + id, "d");
        p.setId(id);
        return p;
    }

    private Role roleWith(Long id, String name, Permission... permissions) {
        Role r = new Role(name, "d");
        r.setId(id);
        r.setPermissions(new HashSet<>(List.of(permissions)));
        return r;
    }

    @Test
    void createSavesRoleWithPermissions() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(permissionRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(permission(1L), permission(2L)));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        Role created = service.create(" ADMIN ", "Administrador", Set.of(1L, 2L));

        assertEquals("ADMIN", created.getName());
        assertEquals(2, created.getPermissions().size());
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(BusinessException.class, () -> service.create("", "d", Set.of(1L)));
    }

    @Test
    void createRejectsMissingPermissions() {
        assertThrows(BusinessException.class, () -> service.create("R", "d", null));
        assertThrows(BusinessException.class, () -> service.create("R", "d", Set.of()));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateName() {
        when(roleRepository.existsByName("R")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create("R", "d", Set.of(1L)));
    }

    @Test
    void createRejectsUnknownPermission() {
        when(roleRepository.existsByName("R")).thenReturn(false);
        when(permissionRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(permission(1L)));

        assertThrows(NotFoundException.class, () -> service.create("R", "d", Set.of(1L, 2L)));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void findAllReturnsRepositoryContent() {
        when(roleRepository.findAll()).thenReturn(List.of(roleWith(1L, "A", permission(1L))));

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByIdReturnsRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));

        assertEquals("A", service.findById(1L).getName());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(roleRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void updateKeepingSameNameDoesNotCheckUniqueness() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("nueva", service.update(1L, "A", "nueva").getDescription());
        verify(roleRepository, never()).existsByName(any());
    }

    @Test
    void updateRenamesWhenNameIsFree() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));
        when(roleRepository.existsByName("B")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("B", service.update(1L, "B", "d").getName());
    }

    @Test
    void updateRejectsNameOfAnotherRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));
        when(roleRepository.existsByName("B")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, "B", "d"));
    }

    @Test
    void addPermissionAttachesIt() {
        Role role = roleWith(1L, "A", permission(1L));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(permission(2L)));
        when(roleRepository.save(role)).thenReturn(role);

        assertEquals(2, service.addPermission(1L, 2L).getPermissions().size());
    }

    @Test
    void addPermissionRejectsUnknownPermission() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));
        when(permissionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addPermission(1L, 2L));
    }

    @Test
    void removePermissionDetachesIt() {
        Role role = roleWith(1L, "A", permission(1L), permission(2L));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);

        Role result = service.removePermission(1L, 2L);

        assertEquals(1, result.getPermissions().size());
        assertEquals(1L, result.getPermissions().iterator().next().getId());
    }

    @Test
    void removePermissionRejectsNotAssignedPermission() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));

        assertThrows(BusinessException.class, () -> service.removePermission(1L, 5L));
    }

    @Test
    void removePermissionRejectsLeavingRoleWithoutPermissions() {
        Role role = roleWith(1L, "A", permission(1L));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThrows(BusinessException.class, () -> service.removePermission(1L, 1L));
        assertEquals(1, role.getPermissions().size());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void deleteRemovesRoleWithoutUsers() {
        Role role = roleWith(1L, "A", permission(1L));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.countByRolesId(1L)).thenReturn(0L);

        service.delete(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRejectsRoleAssignedToUsers() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleWith(1L, "A", permission(1L))));
        when(userRepository.countByRolesId(1L)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> service.delete(1L));
        verify(roleRepository, never()).delete(any());
    }
}
