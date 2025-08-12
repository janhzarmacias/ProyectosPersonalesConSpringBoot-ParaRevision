
# Módulo Auth – GoTaxi

## Arquitectura General

- **Arquitectura:** Hexagonal (Ports and Adapters)
- **Tecnologías:** Spring WebFlux, R2DBC, Redis, JWT
- **Responsabilidad:** Manejo de autenticación, sesión, recuperación y seguridad

---

## Endpoints principales

### AuthController (`/api/auth`)

| Método | Ruta                  | Descripción                          | Autenticación |
|--------|-----------------------|--------------------------------------|----------------|
| POST   | `/register`           | Registro con email o teléfono        | ❌ |
| POST   | `/verify`             | Verificar código OTP                 | ❌ |
| POST   | `/login`              | Login con email/teléfono + password  | ❌ |
| POST   | `/login/google`       | Login con cuenta de Google           | ❌ |
| POST   | `/refresh`            | Refrescar tokens                     | ❌ |
| POST   | `/logout`             | Logout de sesión actual              | ✅ |
| POST   | `/logout/all`         | Logout global                        | ✅ |
| GET    | `/sessions`           | Obtener sesiones activas             | ✅ |
| DELETE | `/close/session`      | Cerrar sesión específica             | ✅ |
| POST   | `/forgot-password`    | Iniciar recuperación de contraseña   | ❌ |
| POST   | `/reset-password`     | Confirmar nueva contraseña           | ❌ |
| POST   | `/add-email`          | Añadir nuevo email                   | ✅ |
| POST   | `/add-phone-number`   | Añadir nuevo teléfono                | ✅ |
| PUT    | `/change-password`    | Cambiar contraseña autenticado       | ✅ |

---

## Estructura del módulo

```
modules/auth
├── api                    # Controladores REST
├── application            # Casos de uso
├── domain
│   ├── model              # Entidades
│   ├── port
│   │   ├── in             # Casos de uso (interfaces)
│   │   └── out            # Repos, servicios externos
├── infrastructure
│   ├── adapter            # Adaptadores (redis, jwt, etc)
│   ├── jwt                # Filtro y servicio JWT
│   ├── repository         # R2DBC
│   └── util               # Helpers redis, etc.
```

---

## Casos de uso

###  `RegisterUserUseCase`
- Registra un usuario con email o teléfono
- Guarda términos aceptados y versión
- Envía código de verificación OTP

###  `LoginUserUseCase`
- Login local por identificador y contraseña
- Verifica existencia y validez
- Devuelve tokens
- Protege con bloqueo por intentos fallidos

###  `LoginWithGoogleUseCase`
- Verifica `idToken` con Google
- Crea cuenta si no existe
- Devuelve access y refresh token

###  `RecoveryPasswordUseCase`
- Inicia recuperación con bloqueo por intentos
- Envía código OTP
- Valida OTP + token
- Cambia contraseña y devuelve nuevos tokens

###  `AddContactInfoUseCase`
- Añade email o teléfono verificado
- Envia OTP al contacto nuevo
- Al verificar:
    - Reemplaza contacto anterior como identificador principal
    - Limpia token huérfano del contacto anterior

###  `ChangePasswordUseCase`
- Cambia contraseña autenticado
- Verifica la actual
- Reemplaza la contraseña y emite nuevos tokens

###  `RefreshTokenUseCase`
- Verifica refresh token y su index en Redis
- Devuelve nuevos tokens con metadata actualizada

###  `LogoutUseCase`
- Elimina refresh token e índice actual
- `logoutAll`: Elimina todos los tokens del usuario

###  `GetActiveSessionsUseCase`
- Devuelve lista de dispositivos/sesiones activos

###  `RevokeSessionUseCase`
- Revoca sesión específica por `deviceId` y `userAgent`


## Seguridad y Redis

- Refresh tokens guardados por `identifier + deviceId + userAgent`
- TTL: 30 días
- Rate Limiting: 5 intentos por 5 min por device/identifier (forgot-password)
- OTP y recovery token expiran (por clave Redis y por DB)

Claves Redis relevantes:

- `refresh:<token>`
- `refresh:index:<identifier>:<userAgent>:<deviceId>`
- `password:attempts:<identifier>:<deviceId>`
- `password:blocked:<identifier>:<deviceId>`
- `otp:email:<email>`, `otp:phone:<phone>`

---

## Manual para devs

### Variables necesarias:
- `jwt.secret`
- `jwt.expiration`
- Redis host/port
- Google clientId para verificación de token

### Comandos útiles Redis (CLI):
```bash
# Ver claves activas
KEYS refresh:*

# Ver intentos
GET password:attempts:user@example.com:android123

# Borrar claves
DEL refresh:<token>
DEL refresh:index:<identifier>:<userAgent>:<deviceId>
DEL password:blocked:<identifier>:<deviceId>
```

---

## Métricas sugeridas

| Indicador                        | Descripción |
|----------------------------------|-------------|
| % de logins exitosos             | Seguridad |
| Tiempo medio de verificación OTP| Usabilidad |
| Cantidad de dispositivos por usuario | Seguridad |
| Tokens expirados por TTL         | Limpieza automática |

---

## Estado

✔️ Módulo revisado y funcional  
✔️ Arquitectura limpia y extensible  
✔️ Cubierto contra fuerza bruta y accesos no autorizados

---

**Autor:** GoTaxi Dev Team — *Janhzar Daniel Macias Garcia*  
**Fecha:** 2025-08-09