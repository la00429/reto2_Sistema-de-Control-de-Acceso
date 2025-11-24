# Correcciones Implementadas en Login Service

## Resumen de Correcciones

Se han realizado las siguientes correcciones al servicio de login:

### 1. **Seguridad - Implementación de BCrypt** ✅
- Se agregó la dependencia `spring-boot-starter-security` al `pom.xml`
- Se creó `PasswordEncoderConfig.java` con configuración de BCrypt
- Se actualizó `DataInitializer` para hashear contraseñas con BCrypt
- Se actualizó `LoginService` para validar contraseñas con BCrypt
- **Migración automática**: Si un usuario tiene contraseña en texto plano, se migra automáticamente a BCrypt al hacer login

### 2. **Calidad de Código - Logger** ✅
- Reemplazado todos los `System.out.println` por `Logger` (SLF4J)
- Agregado logging adecuado en todos los métodos importantes
- Niveles de log apropiados: INFO, WARN, ERROR, DEBUG

### 3. **Refactorización - Inyección de Dependencias** ✅
- `DataInitializer` ahora usa inyección por constructor en lugar de `@Autowired`
- Se eliminaron imports no utilizados
- Se eliminó código no utilizado (`requestTokenFromAuthService`)

### 4. **Mejoras de Configuración** ✅
- Configuración de `SecurityConfig` para permitir todas las peticiones (el login service maneja su propia autenticación)
- Mejoras en `RestTemplateConfig` con timeouts de conexión (5s) y lectura (10s)
- Configuración mejorada de `RabbitMQConfig`

### 5. **Manejo de Errores Mejorado** ✅
- Mejor manejo de excepciones en la comunicación con `auth-service`
- Mensajes de error más descriptivos
- Logging detallado de errores para facilitar diagnóstico

## Diagnóstico del Problema de Login

Si el login sigue fallando, verifica lo siguiente:

### 1. **Verificar que los servicios estén ejecutándose**

```bash
# Verificar que MongoDB esté corriendo en el puerto 27018
docker ps | grep mongodb

# Verificar que RabbitMQ esté corriendo en el puerto 5673
docker ps | grep rabbitmq

# Verificar que auth-service esté corriendo en el puerto 8085
# Ejecuta: curl http://localhost:8085/api/auth/mfa/generate -X POST -H "Content-Type: application/json" -d '{"username":"test"}'
```

### 2. **Verificar la Base de Datos**

Si el usuario `admin` ya existe en la base de datos con contraseña en texto plano:
- El sistema ahora migrará automáticamente la contraseña a BCrypt en el primer login exitoso
- Si la contraseña era "admin" en texto plano, sigue funcionando
- **IMPORTANTE**: Si el usuario fue creado ANTES de estos cambios, puede tener la contraseña en texto plano. El primer login migrará automáticamente.

### 3. **Verificar Logs del Login Service**

Revisa los logs para identificar el problema:

```
# Busca estos mensajes en los logs:
- "Iniciando autenticación para usuario: X" → Indica que la petición llegó
- "Usuario encontrado. Estado: X" → Indica que el usuario existe
- "Contraseña en texto plano detectada" → Migración automática funcionando
- "Contraseña migrada a BCrypt exitosamente" → Migración completada
- "Solicitando MFA token desde: http://localhost:8085/api/auth/mfa/generate" → Llamada al auth-service
- "Error de conexión al Auth Service" → El auth-service no está disponible
- "Error al solicitar MFA token" → Problema con la respuesta del auth-service
```

### 4. **Problemas Comunes y Soluciones**

#### Problema: "Error generando código MFA. Por favor, verifica que el servicio de autenticación esté disponible"
**Solución**: 
- Verifica que `auth-service` esté ejecutándose en el puerto 8085
- Verifica la URL configurada en `application.yml`: `auth.service.url: http://localhost:8085`
- Ejecuta: `curl http://localhost:8085/actuator/health`

#### Problema: "Invalid credentials"
**Causas posibles**:
- Usuario no existe
- Contraseña incorrecta
- Usuario bloqueado (más de 3 intentos fallidos)

**Solución**:
- Verifica que el usuario exista en MongoDB
- Si el usuario tiene contraseña antigua en texto plano, debe ser "admin" exactamente
- Si el usuario está bloqueado, espera 10 minutos o resetea el estado en la BD

#### Problema: Contraseña no funciona después de actualizar código
**Solución**:
- Si el usuario fue creado ANTES de implementar BCrypt, la contraseña está en texto plano
- El sistema migrará automáticamente en el primer login exitoso
- Si migraste antes y ahora no funciona, verifica que el hash BCrypt en la BD sea válido

### 5. **Recrear Usuario Admin**

Si necesitas recrear el usuario admin con BCrypt:

1. Elimina el usuario de MongoDB:
```javascript
// En MongoDB
use login_db
db.users.deleteOne({username: "admin"})
```

2. Reinicia el `login-service` - el `DataInitializer` creará el usuario con BCrypt automáticamente

3. Usa las credenciales:
- Usuario: `admin`
- Contraseña: `admin`

### 6. **Verificar Comunicación entre Servicios**

```bash
# Verificar login-service → auth-service
curl -X POST http://localhost:8081/login/authuser \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Verificar auth-service directamente
curl -X POST http://localhost:8085/api/auth/mfa/generate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin"}'
```

## Cambios en los Archivos

1. `pom.xml` - Agregada dependencia de Spring Security
2. `PasswordEncoderConfig.java` - NUEVO archivo de configuración
3. `SecurityConfig.java` - NUEVO archivo de configuración
4. `DataInitializer.java` - Refactorizado con BCrypt y Logger
5. `LoginService.java` - Actualizado con BCrypt y mejor manejo de errores
6. `LoginController.java` - Actualizado con Logger
7. `RestTemplateConfig.java` - Mejorado con timeouts
8. `RabbitMQConfig.java` - Limpiado imports no utilizados

## Próximos Pasos

1. **Recompilar el proyecto**:
```bash
cd login-service
mvn clean install
```

2. **Reiniciar el servicio**:
```bash
# Si usas Docker
docker-compose restart login-service

# O ejecuta directamente
java -jar target/login-service-1.0.0.jar
```

3. **Verificar los logs** durante el inicio para ver si el usuario admin se crea correctamente

4. **Intentar login** con usuario `admin` y contraseña `admin`

## Notas Importantes

- Los usuarios existentes con contraseña en texto plano se migrarán automáticamente a BCrypt en el primer login exitoso
- Todos los nuevos usuarios se crearán con contraseñas hasheadas con BCrypt
- El logging mejorado facilitará el diagnóstico de problemas
- Los timeouts en RestTemplate evitarán que el servicio se cuelgue esperando respuestas




