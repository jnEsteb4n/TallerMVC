package com.icesi.alphabackend.user;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "ana@u.icesi.edu.co";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService service;

    private Role role(Long id) {
        Role r = new Role("R" + id, "d");
        r.setId(id);
        return r;
    }

    private User userWith(Long id, Role... roles) {
        User u = new User();
        u.setId(id);
        u.setFullName("Ana");
        u.setEmail(EMAIL);
        u.setRoles(new HashSet<>(List.of(roles)));
        return u;
    }

    @Test
    void createSavesUserWithRolesAndHashedPassword() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findAllById(Set.of(1L))).thenReturn(List.of(role(1L)));
        when(passwordEncoder.encode("Password123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User created = service.create(" Ana ", " ANA@u.icesi.edu.co ", "Password123", "Sistemas", Set.of(1L));

        assertEquals("Ana", created.getFullName());
        assertEquals(EMAIL, created.getEmail());
        assertEquals("HASH", created.getPasswordHash());
        assertEquals("Sistemas", created.getAcademicProgram());
        assertEquals(1, created.getRoles().size());
    }

    @Test
    void createRejectsBlankFields() {
        assertThrows(BusinessException.class, () -> service.create("", EMAIL, "Password123", null, Set.of(1L)));
        assertThrows(BusinessException.class, () -> service.create("Ana", "", "Password123", null, Set.of(1L)));
        assertThrows(BusinessException.class, () -> service.create("Ana", EMAIL, "", null, Set.of(1L)));
    }

    @Test
    void createRejectsNonInstitutionalEmail() {
        assertThrows(BusinessException.class,
                () -> service.create("Ana", "ana@gmail.com", "Password123", null, Set.of(1L)));
    }

    @Test
    void createRejectsShortPassword() {
        assertThrows(BusinessException.class, () -> service.create("Ana", EMAIL, "corta", null, Set.of(1L)));
    }

    @Test
    void createRejectsUserWithoutRoles() {
        assertThrows(BusinessException.class, () -> service.create("Ana", EMAIL, "Password123", null, null));
        assertThrows(BusinessException.class, () -> service.create("Ana", EMAIL, "Password123", null, Set.of()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create("Ana", EMAIL, "Password123", null, Set.of(1L)));
    }

    @Test
    void createRejectsUnknownRole() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(role(1L)));

        assertThrows(NotFoundException.class,
                () -> service.create("Ana", EMAIL, "Password123", null, Set.of(1L, 2L)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findAllReturnsRepositoryContent() {
        when(userRepository.findAll()).thenReturn(List.of(userWith(1L, role(1L))));

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByIdReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));

        assertEquals(EMAIL, service.findById(1L).getEmail());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(9L));
    }

    @Test
    void findByEmailReturnsUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWith(1L, role(1L))));

        assertEquals(1L, service.findByEmail(EMAIL).getId());
    }

    @Test
    void findByEmailThrowsWhenMissing() {
        when(userRepository.findByEmail("x@u.icesi.edu.co")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findByEmail("x@u.icesi.edu.co"));
    }

    @Test
    void updateChangesProfileFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = service.update(1L, " Ana María ", "Economía", "http://foto");

        assertEquals("Ana María", updated.getFullName());
        assertEquals("Economía", updated.getAcademicProgram());
        assertEquals("http://foto", updated.getPhotoUrl());
    }

    @Test
    void updateRejectsBlankName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));

        assertThrows(BusinessException.class, () -> service.update(1L, " ", null, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEnabledTogglesAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertFalse(service.setEnabled(1L, false).isEnabled());
    }

    @Test
    void addRoleAttachesIt() {
        User user = userWith(1L, role(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role(2L)));
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(2, service.addRole(1L, 2L).getRoles().size());
    }

    @Test
    void addRoleRejectsUnknownRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addRole(1L, 2L));
    }

    @Test
    void removeRoleDetachesIt() {
        User user = userWith(1L, role(1L), role(2L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = service.removeRole(1L, 2L);

        assertEquals(1, result.getRoles().size());
        assertEquals(1L, result.getRoles().iterator().next().getId());
    }

    @Test
    void removeRoleRejectsNotAssignedRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, role(1L))));

        assertThrows(BusinessException.class, () -> service.removeRole(1L, 7L));
    }

    @Test
    void removeRoleRejectsLeavingUserWithoutRoles() {
        User user = userWith(1L, role(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> service.removeRole(1L, 1L));
        assertEquals(1, user.getRoles().size());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteRemovesUser() {
        User user = userWith(1L, role(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        verify(userRepository).delete(user);
    }
}
