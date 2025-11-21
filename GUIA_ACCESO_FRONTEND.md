# Guía de Acceso al Frontend

## Inicio Rápido

### 1. Verificar que los Servicios Estén Corriendo

Antes de acceder al frontend, asegúrate de que todos los servicios estén corriendo:

- **API Gateway**: http://localhost:8080
- **Login Service**: http://localhost:8081
- **Employee Service**: http://localhost:8082
- **Access Control Service**: http://localhost:8083
- **Alert Service**: http://localhost:8084
- **Auth Service**: http://localhost:8085
- **SAGA Orchestrator**: http://localhost:8086

### 2. Iniciar el Frontend

```bash
cd frontend
npm install  # Solo la primera vez
npm run dev
```

El frontend se iniciará en: **http://localhost:3001** (puerto 3000 está ocupado por Grafana)

## Acceso a la Aplicación

### URL Principal
- **Frontend**: http://localhost:3001
- **Login**: http://localhost:3001/login
- **Grafana**: http://localhost:3000 (monitoreo)

### Configuración de Conexión

El frontend está configurado para conectarse al API Gateway en:
- **Base URL**: http://localhost:8080
- **Archivo de configuración**: `frontend/src/services/api.js`

## Crear Usuario de Prueba

### Opción 1: Mediante API (Recomendado)

```bash
curl -X POST http://localhost:8080/login/createuser \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com"
  }'
```

O usando Swagger:
1. Accede a http://localhost:8081/swagger-ui.html
2. Usa el endpoint `POST /login/createuser`
3. Ingresa los datos del usuario

### Opción 2: Directamente en MongoDB

```javascript
// Conectarse a MongoDB (puerto 27018)
use login_db
db.Login.insertOne({
  userID: NumberLong(1),
  password: "admin123",
  status: "ACTIVE",
  failedLoginAttempts: 0,
  createdAt: new Date(),
  updatedAt: new Date()
})
```

## Flujo de Autenticación

### Paso 1: Login Inicial
1. Accede a http://localhost:3001/login
2. Ingresa:
   - **Usuario**: admin
   - **Contraseña**: admin123
3. Haz clic en "Iniciar Sesión"

### Paso 2: Verificación MFA
1. El sistema generará un código MFA de 6 dígitos
2. Revisa la consola del servidor (Login Service) para ver el código
3. Ingresa el código MFA en el formulario
4. Haz clic en "Verificar"

### Paso 3: Acceso al Sistema
Una vez verificado el MFA, serás redirigido al Dashboard.

## Páginas Disponibles

### Dashboard
- **URL**: http://localhost:3001/dashboard
- **Descripción**: Vista general del sistema con estadísticas

### Gestión de Empleados
- **URL**: http://localhost:3001/employees
- **Funcionalidades**:
  - Ver lista de empleados
  - Crear nuevo empleado
  - Editar empleado
  - Inactivar empleado

### Control de Acceso
- **URL**: http://localhost:3001/access-control
- **Funcionalidades**:
  - Registrar ingreso de empleado
  - Registrar salida de empleado
  - Ver historial de accesos

### Reportes
- **URL**: http://localhost:3001/reports
- **Funcionalidades**:
  - Reporte de empleados por fecha
  - Reporte de empleado por rango de fechas

## Solución de Problemas

### Error: "Network Error" o "Connection Refused"
- Verifica que el API Gateway esté corriendo en el puerto 8080
- Verifica que todos los microservicios estén corriendo
- Revisa la consola del navegador para ver errores específicos

### Error: "401 Unauthorized"
- Verifica que hayas completado el flujo de autenticación MFA
- Verifica que el token JWT esté almacenado en localStorage
- Intenta cerrar sesión y volver a iniciar sesión

### El código MFA no aparece
- Revisa la consola del servidor (Login Service)
- El código se imprime en la consola: `MFA Token para {username}: {código}`
- En producción, el código se enviaría por email/SMS

### No puedo acceder a las páginas protegidas
- Asegúrate de haber completado el login y verificación MFA
- Verifica que el token esté en localStorage (F12 > Application > Local Storage)
- Si el token expiró, inicia sesión nuevamente

## Configuración Avanzada

### Cambiar el Puerto del Frontend
Edita `frontend/vite.config.js`:
```javascript
server: {
  port: 3001  // Puerto actual (3000 está ocupado por Grafana)
}
```

### Cambiar la URL del API Gateway
Edita `frontend/src/services/api.js`:
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080',  // Cambiar aquí
  // ...
})
```

### Habilitar CORS (si es necesario)
Si tienes problemas de CORS, verifica que los servicios tengan `@CrossOrigin(origins = "*")` en los controladores.

## Notas Importantes

1. **MFA en Desarrollo**: En desarrollo, el código MFA se imprime en la consola del servidor. En producción, se enviaría por email/SMS.

2. **Tokens JWT**: Los tokens JWT tienen una expiración configurada. Si expiran, necesitarás iniciar sesión nuevamente.

3. **Base de Datos**: Asegúrate de que las bases de datos (MySQL y MongoDB) estén corriendo antes de iniciar los servicios.

4. **Puertos**: Si algún puerto está ocupado, verifica el archivo `PUERTOS_CONFIGURADOS.md` para ver los puertos alternativos configurados.

