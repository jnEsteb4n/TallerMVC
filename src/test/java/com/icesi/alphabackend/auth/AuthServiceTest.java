package com.icesi.alphabackend.auth;

import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.common.NotFoundException;
import com.icesi.alphabackend.permission.Permission;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleRepository;
import com.icesi.alphabackend.user.User;
import com.icesi.alphabackend.user.UserRepository;
import com.icesi.alphabackend.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "ana@u.icesi.edu.co";

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AuthService service;

    private Role role(Long id, String name, String... permissions) {
        Role r = new Role(name, "d");
        r.setId(id);
        for (String p : permissions) {
            r.getPermissions().add(new Permission(p, "d"));
        }
        return r;
    }

    private User user(boolean enabled, Role... roles) {
        User u = new User();
        u.setId(1L);
        u.setFullName("Ana");
        u.setEmail(EMAIL);
        u.setPasswordHash("HASH");
        u.setEnabled(enabled);
        u.setRoles(new HashSet<>(List.of(roles)));
        return u;
    }

    @Test
    void registerCreatesUserWithDefaultRole() {
        User created = user(true, role(2L, "USER"));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role(2L, "USER")));
        when(userService.create("Ana", EMAIL, "Password123", "Sistemas", Set.of(2L))).thenReturn(created);

        User result = service.register("Ana", EMAIL, "Password123", "Password123", "Sistemas");

        assertSame(created, result);
        verify(userService).create("Ana", EMAIL, "Password123", "Sistemas", Set.of(2L));
    }

    @Test
    void registerRejectsPasswordsThatDoNotMatch() {
        assertThrows(BusinessException.class,
                () -> service.register("Ana", EMAIL, "Password123", "Otra12345", null));
        verifyNoInteractions(userService, roleRepository);
    }

    @Test
    void registerFailsWhenDefaultRoleDoesNotExist() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.register("Ana", EMAIL, "Password123", "Password123", null));
        verifyNoInteractions(userService);
    }

    @Test
    void loadUserByUsernameReturnsRolesAndPermissionsAsAuthorities() {
        Role admin = role(1L, "ADMIN", "USER_READ", "ROLE_WRITE");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true, admin)));

        UserDetails details = service.loadUserByUsername("  ANA@u.icesi.edu.co ");

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_ADMIN", "USER_READ", "ROLE_WRITE"), authorities);
        assertEquals(EMAIL, details.getUsername());
        assertEquals("HASH", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsernameReportsDisabledUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(false, role(2L, "USER"))));

        assertFalse(service.loadUserByUsername(EMAIL).isEnabled());
    }

    @Test
    void loadUserByUsernameThrowsWhenEmailIsUnknown() {
        when(userRepository.findByEmail("nadie@u.icesi.edu.co")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("nadie@u.icesi.edu.co"));
    }
}
