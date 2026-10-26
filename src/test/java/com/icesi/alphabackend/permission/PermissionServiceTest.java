package com.icesi.alphabackend.permission;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private PermissionService service;

    private Permission permission(Long id, String name) {
        Permission p = new Permission(name, "desc");
        p.setId(id);
        return p;
    }

    @Test
    void createSavesNewPermission() {
        when(permissionRepository.existsByName("USER_READ")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(i -> i.getArgument(0));

        Permission created = service.create("  USER_READ ", "Consultar");

        assertEquals("USER_READ", created.getName());
        assertEquals("Consultar", created.getDescription());
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(BusinessException.class, () -> service.create(" ", "x"));
        assertThrows(BusinessException.class, () -> service.create(null, "x"));
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void createRejectsDuplicateName() {
        when(permissionRepository.existsByName("USER_READ")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create("USER_READ", "x"));
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void findAllReturnsRepositoryContent() {
        when(permissionRepository.findAll()).thenReturn(List.of(permission(1L, "A"), permission(2L, "B")));

        assertEquals(2, service.findAll().size());
    }

    @Test
    void findByIdReturnsPermission() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission(1L, "A")));

        assertEquals("A", service.findById(1L).getName());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(permissionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void updateKeepingSameNameDoesNotCheckUniqueness() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission(1L, "A")));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(i -> i.getArgument(0));

        Permission updated = service.update(1L, "A", "nueva descripción");

        assertEquals("nueva descripción", updated.getDescription());
        verify(permissionRepository, never()).existsByName(any());
    }

    @Test
    void updateRenamesWhenNameIsFree() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission(1L, "A")));
        when(permissionRepository.existsByName("B")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("B", service.update(1L, "B", "d").getName());
    }

    @Test
    void updateRejectsNameOfAnotherPermission() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission(1L, "A")));
        when(permissionRepository.existsByName("B")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, "B", "d"));
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void deleteRemovesUnusedPermission() {
        Permission p = permission(1L, "A");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(p));
        when(roleRepository.existsByPermissionsId(1L)).thenReturn(false);

        service.delete(1L);

        verify(permissionRepository).delete(p);
    }

    @Test
    void deleteRejectsPermissionAssignedToRole() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission(1L, "A")));
        when(roleRepository.existsByPermissionsId(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1L));
        verify(permissionRepository, never()).delete(any());
    }
}
