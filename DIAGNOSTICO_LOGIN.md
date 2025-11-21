# Diagnóstico del Problema de Login

## Situación Actual

Según los logs compartidos:

### ✅ Lo que está funcionando:
1. **API Gateway**: Recibe la petición POST a `/login/authuser` y responde con status 200
2. **Auth Service**: Está generando tokens MFA correctamente (281148, 467940, 407832)
3. **Comunicación entre servicios**: El login-service está comunicándose con el auth-service

### ❌ Problema identificado:
- **No se ven logs del login-service** en la salida compartida
- El usuario reporta que sigue recibiendo "Error al iniciar sesión"

## Posibles Causas

### 1. El Login-Service no está mostrando logs

**Verificación**:
```bash
# Verifica si el login-service está ejecutándose
jps | grep LoginServiceApplication
# O verifica el puerto
netstat -ano | findstr :8081
```

**Solución**: Si no está ejecutándose, inícialo. Los logs deberían mostrar:
- "Usuario admin creado por defecto" o "Usuario admin ya existe"
- "LoginController: Petición recibida para usuario: X"
- "Iniciando autenticación para usuario: X"

### 2. El Login-Service está devolviendo una respuesta incorrecta

**Problema**: El servicio podría estar devolviendo `success: false` incluso cuando todo funciona.

**Verificación**: Revisa los logs del login-service para ver:
- Si el usuario se encuentra en la base de datos
- Si la contraseña es válida
- Si se genera el token MFA correctamente

**Solución**: Agrega logs detallados (ya implementado en las correcciones)

### 3. El Frontend no está interpretando correctamente la respuesta

**Problema**: El frontend espera una estructura específica y podría no estar recibiendo `requiresMFA: true`.

**Verificación**: Abre las DevTools del navegador (F12) y revisa:
- La pestaña Network: ¿Qué respuesta se está recibiendo?
- La pestaña Console: ¿Hay algún error de JavaScript?

**Respuesta esperada**:
```json
{
  "success": true,
  "message": "MFA token sent. Please verify.",
  "username": "admin",
  "token": "281148",
  "requiresMFA": true
}
```

### 4. Problema de comunicación entre API Gateway y Login Service

**Problema**: El api-gateway podría no estar reenviando correctamente la petición.

**Verificación**: Prueba llamar directamente al login-service:
```bash
curl -X POST http://localhost:8081/login/authuser \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

**Compara con la llamada a través del api-gateway**:
```bash
curl -X POST http://localhost:8080/login/authuser \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

## Pasos de Diagnóstico Recomendados

### Paso 1: Verificar que el Login-Service esté ejecutándose
```bash
# En Windows
netstat -ano | findstr :8081

# O verifica los procesos Java
jps -l | findstr login
```

### Paso 2: Revisar los logs del Login-Service
Busca en los logs del login-service estos mensajes:
- `LoginController: Petición recibida para usuario: admin`
- `Iniciando autenticación para usuario: admin`
- `Usuario encontrado. Estado: ACTIVE`
- `Contraseña válida: true`
- `MFA Token para admin: XXXX`

### Paso 3: Verificar la Base de Datos
```javascript
// Conecta a MongoDB en el puerto 27018
// Usa mongosh o Mongo Compass
use login_db
db.users.find({username: "admin"})
```

Verifica:
- ¿El usuario existe?
- ¿Qué tiene en `passwordHash`? (Debería ser un hash BCrypt o "admin" en texto plano)
- ¿Cuál es el `status`? (Debe ser "ACTIVE")

### Paso 4: Probar el Endpoint Directamente
```bash
# Probar login-service directamente
curl -v -X POST http://localhost:8081/login/authuser \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"

# Probar a través del api-gateway
curl -v -X POST http://localhost:8080/login/authuser \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

Compara las respuestas. Ambas deberían ser idénticas.

### Paso 5: Verificar el Frontend
1. Abre las DevTools (F12)
2. Ve a la pestaña Network
3. Intenta hacer login
4. Busca la petición a `/login/authuser`
5. Revisa:
   - Status code (debe ser 200)
   - Response body (debe tener `success: true` y `requiresMFA: true`)
   - Headers de la respuesta

### Paso 6: Verificar Console del Navegador
En las DevTools, revisa la pestaña Console para errores de JavaScript que puedan estar interfiriendo.

## Solución Rápida - Verificación Manual

Si el problema persiste, ejecuta este test manual:

```bash
# 1. Verifica que todos los servicios estén ejecutándose
# login-service en puerto 8081
curl http://localhost:8081/actuator/health

# auth-service en puerto 8085  
curl http://localhost:8085/actuator/health

# api-gateway en puerto 8080
curl http://localhost:8080/actuator/health

# 2. Prueba el flujo completo manualmente
# Primero, autentica
curl -X POST http://localhost:8080/login/authuser \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"

# La respuesta debe ser algo como:
# {
#   "success": true,
#   "message": "MFA token sent. Please verify.",
#   "username": "admin",
#   "token": "281148",
#   "requiresMFA": true
# }

# 3. Luego valida el MFA (usa el token que recibiste)
curl -X POST http://localhost:8080/auth/mfa/validate \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"token\":\"281148\"}"
```

## Logs Esperados del Login-Service

Cuando todo funciona correctamente, deberías ver estos logs en el login-service:

```
INFO  - LoginController: Petición recibida para usuario: admin
INFO  - Iniciando autenticación para usuario: admin
DEBUG - Usuario encontrado. Estado: ACTIVE
DEBUG - Validando contraseña...
DEBUG - Contraseña válida: true
INFO  - Solicitando código MFA para usuario: admin
INFO  - Solicitando MFA token desde: http://localhost:8085/api/auth/mfa/generate
INFO  - MFA Token para admin: 281148
INFO  - Login exitoso para usuario: admin. Esperando verificación MFA.
INFO  - LoginController: Respuesta generada - Success: true, RequiresMFA: true
```

## Si el Problema Persiste

1. **Comparte los logs completos del login-service** cuando intentas hacer login
2. **Comparte la respuesta HTTP** que recibes en el navegador (DevTools > Network)
3. **Comparte cualquier error** que aparezca en la consola del navegador

Esto nos ayudará a identificar exactamente dónde está fallando el flujo.


