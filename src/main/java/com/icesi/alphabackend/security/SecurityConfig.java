package com.icesi.alphabackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Reglas de acceso del módulo.
 * <ul>
 *   <li>/login, /register y los estilos son públicos.</li>
 *   <li>Consultar (GET) exige el permiso *_READ del módulo y modificar (POST) el *_WRITE.</li>
 *   <li>Los controladores repiten el *_WRITE con @PreAuthorize en los formularios de edición,
 *       que son GET y el filtro deja pasar con *_READ.</li>
 *   <li>Eliminar un rol exige además el rol ADMIN (hasRole en @PreAuthorize).</li>
 *   <li>Todo lo demás exige usuario autenticado.</li>
 * </ul>
 * El AuthenticationProvider lo arma Spring con el único UserDetailsService (AuthService)
 * y el PasswordEncoder BCrypt de common.SecurityBeans.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/error").permitAll()

                .requestMatchers(HttpMethod.GET, "/users/**").hasAuthority("USER_READ")
                .requestMatchers(HttpMethod.POST, "/users/**").hasAuthority("USER_WRITE")

                .requestMatchers(HttpMethod.GET, "/roles/**").hasAuthority("ROLE_READ")
                .requestMatchers(HttpMethod.POST, "/roles/**").hasAuthority("ROLE_WRITE")

                .requestMatchers(HttpMethod.GET, "/permissions/**").hasAuthority("PERMISSION_READ")
                .requestMatchers(HttpMethod.POST, "/permissions/**").hasAuthority("PERMISSION_WRITE")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }
}
