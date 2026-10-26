# AlphaBackend — Taller 3 (MVC, Thymeleaf y Spring Security)

Spring Boot 3.5 · Java 21 · H2 · Thymeleaf · Spring Security 6

| Rúbrica | Dónde está |
|---|---|
| Servicios (20 %) | `UserService`, `RoleService` y `PermissionService` del Taller 2 (CRUD + reglas de negocio). Nuevo `auth/AuthService`: implementa `UserDetailsService`, registra cuentas con el rol USER (la contraseña se guarda con BCrypt a través de `UserService.create`) y carga el usuario por correo. `security/SecurityUser.getAuthorities()` entrega roles y permisos como `GrantedAuthority`. |
| Spring Security (10 %) | `security/SecurityConfig`: `SecurityFilterChain` con `authorizeHttpRequests` + `requestMatchers` y `@PreAuthorize` en los controladores. |
| Controladores (20 %) | `auth/AuthController`, `home/HomeController`, `user/UserController`, `role/RoleController`, `permission/PermissionController`. Delegan en los servicios, usan `Model`, devuelven plantilla en los GET y `redirect:` después de cada POST. |
| Vistas (40 %) | `src/main/resources/templates/**` con `th:action` en todos los formularios (token CSRF) y estilos propios en `static/css/styles.css`. |
| Pruebas (10 %) | `auth/AuthServiceTest` (unitaria, Mockito) y `security/SecurityWebIntegrationTest` (integración, MockMvc), además de las pruebas de servicios del Taller 2. |
| Despliegue | Empaquetado WAR (`AlphaBackendApplication` extiende `SpringBootServletInitializer`). |

## Ejecutar en local

```bash
mvn spring-boot:run
```

Abrir http://localhost:8080. Usuarios semilla de `data.sql` (contraseña `Password123`):

| Correo | Rol | Qué puede hacer |
|---|---|---|
| `admin@u.icesi.edu.co` | ADMIN | Todo: usuarios, roles (incluido eliminar) y permisos |
| `ana.torres@u.icesi.edu.co` | USER | Ver la lista de usuarios; no modifica nada ni ve roles o permisos |
| `carlos.gomez@u.icesi.edu.co` | USER | Igual que Ana |

Quien se registra en `/register` recibe el rol USER. El correo debe ser `@u.icesi.edu.co` y la contraseña de mínimo 8 caracteres (reglas de `UserService`).

## Modelo de seguridad

- El inicio de sesión es con **correo** y contraseña (`usernameParameter("email")`).
- Cada rol se entrega como `ROLE_<nombre>` (lo evalúa `hasRole`) y cada permiso con su nombre (`USER_READ`, `ROLE_WRITE`…, los evalúa `hasAuthority`).
- Un usuario recibe la suma de los permisos de sus roles. Los cambios se aplican en su **siguiente inicio de sesión**.
- Los permisos `ROLE_READ` y `ROLE_WRITE` empiezan por `ROLE_` (vienen del Taller 2). Por eso se evalúan con `hasAuthority('ROLE_READ')`, nunca con `hasRole`, y no se debe crear un rol llamado `READ` ni `WRITE`.

| Ruta | Regla | Dónde |
|---|---|---|
| `/login`, `/register`, `/css/**` | `permitAll` | `SecurityFilterChain` |
| `GET /users/**` | `hasAuthority('USER_READ')` | `SecurityFilterChain` |
| `POST /users/**` y formulario de edición | `hasAuthority('USER_WRITE')` | `SecurityFilterChain` + `@PreAuthorize` |
| `GET /roles/**` | `hasAuthority('ROLE_READ')` | `SecurityFilterChain` |
| `POST /roles/**` y formularios | `hasAuthority('ROLE_WRITE')` | `SecurityFilterChain` + `@PreAuthorize` |
| `POST /roles/{id}/delete` | `hasRole('ADMIN') and hasAuthority('ROLE_WRITE')` | `@PreAuthorize` |
| `GET /permissions/**` | `hasAuthority('PERMISSION_READ')` | `SecurityFilterChain` |
| `POST /permissions/**` y formularios | `hasAuthority('PERMISSION_WRITE')` | `SecurityFilterChain` + `@PreAuthorize` |
| Todo lo demás | `authenticated()` | `SecurityFilterChain` |

## Funcionalidades y rutas

| Funcionalidad | GET (vista) | POST (acción → redirect) |
|---|---|---|
| Iniciar sesión | `/login` → `auth/login` | `/login` (Spring Security) → `/` |
| Registrar usuario | `/register` → `auth/register` | `/register` → `/login` |
| Listar usuarios | `/users` → `users/list` | — |
| Editar / desactivar usuario | `/users/{id}/edit` → `users/form` | `/users/{id}` → `/users` |
| Eliminar usuario | — | `/users/{id}/delete` → `/users` |
| Asociar roles a un usuario | `/users/{id}/roles` → `users/roles` | `/users/{id}/roles` |
| Quitar roles a un usuario | `/users/{id}/roles` → `users/roles` | `/users/{id}/roles/{roleId}/remove` |
| Crear roles | `/roles/new` → `roles/form` | `/roles` → `/roles/{id}/permissions` |
| Editar rol | `/roles/{id}/edit` → `roles/form` | `/roles/{id}` → `/roles` |
| Asociar / quitar permisos de un rol | `/roles/{id}/permissions` → `roles/permissions` | `/roles/{id}/permissions`, `/roles/{id}/permissions/{permissionId}/remove` |
| Eliminar roles | — | `/roles/{id}/delete` → `/roles` |
| CRUD de permisos | `/permissions`, `/permissions/new`, `/permissions/{id}/edit` | `/permissions`, `/permissions/{id}`, `/permissions/{id}/delete` |

Las reglas de negocio del Taller 2 se respetan en la interfaz y se muestran como mensajes: un usuario no queda sin roles, un rol no queda sin permisos, no se elimina un rol asignado a usuarios ni un permiso asignado a roles, y un rol se crea con al menos un permiso. Un usuario con información financiera (cuentas, movimientos) no se puede eliminar por las llaves foráneas; en ese caso se desactiva. Nadie puede desactivarse ni eliminarse a sí mismo.

## Cómo probar a mano

1. Entrar con `admin@u.icesi.edu.co`. En Inicio se ven sus roles y permisos.
2. **Roles → Crear rol** `TESORERO` marcando `ROLE_READ` y `FINANCE_OWN`; queda en la vista de permisos del rol. Asociar `PERMISSION_READ` y quitar `FINANCE_OWN`.
3. **Usuarios → Roles** de Ana → asociar `TESORERO`.
4. Cerrar sesión y entrar con `ana.torres@u.icesi.edu.co`: ahora ve *Roles* y *Permisos* en el menú, pero en solo lectura. Abrir `/roles/new` muestra la página 403.
5. Con `admin`, intentar eliminar `USER` (está asignado: muestra el error), quitar `TESORERO` a Ana y luego eliminarlo.
6. Registrar una cuenta nueva en `/register` e iniciar sesión con ella.

> `data.sql` se ejecuta en cada arranque con `MERGE`. Si se modifican datos semilla (por ejemplo, quitarle un rol a Ana), se restauran al reiniciar. Los datos nuevos se conservan.

## Pruebas y cobertura

```bash
mvn verify
```

Ejecuta todas las pruebas, genera el reporte JaCoCo en `target/site/jacoco/index.html` y exige 100 % de líneas y ramas en las clases `*Service`, que ahora incluyen `AuthService`.

- Unitarias (JUnit 5 + Mockito): `UserServiceTest`, `RoleServiceTest`, `PermissionServiceTest`, `AuthServiceTest`.
- Integración: `PersistenceIntegrationTest` (JPA) y `SecurityWebIntegrationTest` (páginas públicas, redirección al login, login con correo, registro, 403 por permisos y por `@PreAuthorize`, CSRF y errores de negocio en la interfaz).

## Despliegue en el Tomcat de IAsLab

1. `mvn clean package` genera `target/alphabackend.war`.
2. Subirlo desde el **Tomcat Manager** (*WAR file to deploy*) o copiarlo a `webapps/`.
3. La aplicación queda en `http://<servidor>:<puerto>/alphabackend/`.
4. Requisitos del servidor: Tomcat 10.1 u 11 y **Java 21** (`java.version` del `pom.xml`). Si el servidor tiene Java 17, cambiar `<java.version>` a 17: el código no usa nada exclusivo de 21.
5. En Tomcat se activa el perfil `war` (`application-war.yml`), que guarda la base H2 en `$CATALINA_BASE/temp/alphabackend/` porque la carpeta relativa `./data` puede no tener permisos de escritura.




