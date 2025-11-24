# Sistema de Control de Acceso Peatonal

Sistema de microservicios para el control de acceso peatonal con arquitectura basada en Spring Boot y React.

## Arquitectura

El sistema está compuesto por:

- **Frontend**: Aplicación web React (Web Admin)
- **API Gateway**: Spring Cloud Gateway (Puerto 8080)
- **Microservicios**:
  - Login Service (Puerto 8081) - Autenticación + MFA
  - Authentication Service (Puerto 8085) - Generación JWT + MFA
  - Employee Service (Puerto 8082) - CRUD empleados
  - Access Control Service (Puerto 8083) - Control de acceso (Arquitectura Hexagonal)
  - Alert Service (Puerto 8084) - Gestión de alertas
  - SAGA Orchestrator (Puerto 8086) - Orquestación de transacciones
- **Bases de Datos**:
  - MySQL (Puerto 3307) - Datos transaccionales
  - MongoDB (Puerto 27018) - Cache y sesiones
- **Infraestructura**:
  - RabbitMQ (Puerto 5673) - Mensajería asíncrona
  - Prometheus (Puerto 9090) - Métricas
  - Grafana (Puerto 3000) - Visualización

## Documentación de APIs (Swagger)

Todos los microservicios incluyen documentación Swagger/OpenAPI para facilitar el acceso y prueba de los endpoints.

### Acceso a Swagger UI

Una vez iniciados los servicios, accede a la documentación interactiva en:

- **Login Service**: http://localhost:8081/swagger-ui.html
- **Employee Service**: http://localhost:8082/swagger-ui.html
- **Access Control Service**: http://localhost:8083/swagger-ui.html
- **Alert Service**: http://localhost:8084/swagger-ui.html
- **Auth Service**: http://localhost:8085/swagger-ui.html
- **SAGA Orchestrator**: http://localhost:8086/swagger-ui.html

### API Docs (JSON/YAML)

Los documentos OpenAPI están disponibles en:

- **Login Service**: http://localhost:8081/api-docs
- **Employee Service**: http://localhost:8082/api-docs
- **Access Control Service**: http://localhost:8083/api-docs
- **Alert Service**: http://localhost:8084/api-docs
- **Auth Service**: http://localhost:8085/api-docs
- **SAGA Orchestrator**: http://localhost:8086/api-docs

## Características Implementadas

### Funcionalidades
- Login con bloqueo (10 minutos, 3 intentos)
- Autenticación Multifactor (MFA)
- Gestión completa de empleados
- Control de acceso con validaciones
- Sistema de alertas automático
- Reportes específicos

### Arquitecturas
- Microservicios
- Event-Driven Architecture (EDA)
- SAGA Pattern
- Arquitectura Hexagonal
- Domain-Driven Design (DDD)
- Arquitectura DTO

## Requisitos

- **Docker y Docker Compose** (obligatorio para backend e infraestructura)
- **Node.js 18+** (solo para ejecutar el frontend en local)
- **Java 17+ y Maven 3.8+** (opcional, solo si necesitas compilar los servicios manualmente)

## Instalación

### 1. Iniciar todos los servicios backend e infraestructura con Docker

**Importante**: Todos los microservicios backend se ejecutan en Docker. Solo el frontend se ejecuta en local.

```bash
docker-compose up -d --build
```

Este comando iniciará:
- **Bases de Datos**:
- MySQL (Puerto 3307)
- MongoDB (Puerto 27018)
- **Infraestructura**:
- RabbitMQ (Puerto 5673, Management: 15673)
- Prometheus (Puerto 9090)
- Grafana (Puerto 3000)
- **Microservicios Backend**:
  - API Gateway (Puerto 8080)
  - Login Service (Puerto 8081)
  - Employee Service (Puerto 8082)
  - Access Control Service (Puerto 8083)
  - Alert Service (Puerto 8084)
  - Auth Service (Puerto 8085)
  - SAGA Orchestrator (Puerto 8086)

### 2. Iniciar Frontend (solo en local)

Una vez que todos los servicios de Docker estén corriendo, inicia el frontend en local:

```bash
cd frontend
npm install
npm run dev
```

El frontend estará disponible en: `http://localhost:3001` (puerto 3000 está ocupado por Grafana)

### 3. Verificar que todo esté corriendo

Puedes verificar el estado de los contenedores con:

```bash
docker-compose ps
```

Para ver los logs de un servicio específico:

```bash
docker-compose logs -f [nombre-del-servicio]
# Ejemplo: docker-compose logs -f api-gateway
```

## Acceso a la Aplicación

### Paso 1: Iniciar servicios backend
```bash
docker-compose up -d --build
```

Espera a que todos los contenedores estén corriendo (puedes verificar con `docker-compose ps`).

### Paso 2: Iniciar frontend
```bash
cd frontend
npm install
npm run dev
```

### Paso 3: Acceder a la aplicación

Una vez iniciado el frontend, accede a:
- **URL**: http://localhost:3001
- **Página de Login**: http://localhost:3001/login

### Credenciales de Prueba

Para acceder al sistema, primero debes crear un usuario usando el endpoint de registro:

**Crear usuario mediante API:**
```bash
POST http://localhost:8080/login/createuser
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "email": "admin@example.com"
}
```

Luego inicia sesión con:
- **Usuario**: admin
- **Contraseña**: admin123

### Flujo de Autenticación
1. Accede a http://localhost:3001/login
2. Ingresa tus credenciales
3. El sistema generará un código MFA de 6 dígitos
4. Ingresa el código MFA para completar la autenticación
5. Serás redirigido al Dashboard

### Páginas Disponibles
- **Login**: http://localhost:3001/login
- **Dashboard**: http://localhost:3001/dashboard
- **Empleados**: http://localhost:3001/employees
- **Control de Acceso**: http://localhost:3001/access-control
- **Reportes**: http://localhost:3001/reports

## Estructura del Proyecto

```
.
├── frontend/                          # Aplicación React
├── api-gateway/                       # Spring Cloud Gateway
├── login-service/                     # Login + MFA
├── auth-service/                      # JWT + MFA
├── employee-service/                  # CRUD Empleados
├── access-control-service/            # Control de Acceso (Hexagonal)
│   ├── domain/                        # Capa de dominio
│   ├── application/                   # Casos de uso
│   └── infrastructure/                 # Adaptadores
├── alert-service/                      # Gestión de Alertas
├── saga-service/                       # SAGA Orchestrator
├── database/                           # Scripts SQL
├── monitoring/                          # Prometheus + Grafana
├── diagrams/                           # Diagramas PlantUML
├── docker-compose.yml                 # Orquestación
└── README.md
```

## Autenticación

### Flujo con MFA

1. **Login Inicial**
   ```
   POST /login/authuser
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
   Respuesta: Código MFA de 6 dígitos

2. **Verificar MFA**
   ```
   POST /login/verify-mfa
   {
     "username": "admin",
     "mfaToken": "123456"
   }
   ```
   Respuesta: JWT token

## Endpoints Principales

### Login
- `POST /login/authuser` - Autenticación
- `POST /login/createuser` - Registro de usuario
- `POST /login/verify-mfa` - Verificar MFA

### Empleados
- `GET /employee/findallemployees` - Listar todos
- `POST /employee/createemployee` - Crear
- `PUT /employee/updateemployee/{id}` - Actualizar
- `PATCH /employee/disableemployee/{id}` - Inactivar

### Control de Acceso
- `POST /access/usercheckin` - Registrar ingreso
- `POST /access/usercheckout` - Registrar salida
- `GET /access/allemployeesbydate` - Reporte por fecha
- `GET /access/employeebydates` - Reporte por empleado

### Alertas
- `POST /alert/usrnotregistattempt` - Usuario no registrado
- `POST /alert/usrexceedattempts` - Intentos excedidos
- `POST /alert/employeealreadyentered` - Doble ingreso
- `POST /alert/employeealreadyleft` - Doble salida

### SAGA
- `POST /saga/access-registration` - Orquestar registro de acceso

## Configuración

### Variables de Entorno para Producción

Para producción, crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```bash
# IP del VPS
VPS_IP=142.93.248.100

# Dominio principal
DOMAIN=pnaltsw.site

# Base de datos MySQL
MYSQL_ROOT_PASSWORD=SuperSecureRootPassword2024!
MYSQL_USER=appuser
MYSQL_PASSWORD=AppUserSecurePassword2024!

# Base de datos MongoDB
MONGO_ROOT_USER=admin
MONGO_ROOT_PASSWORD=MongoSecurePassword2024!

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=RabbitMQSecurePassword2024!

# Grafana
GRAFANA_USER=admin
GRAFANA_PASSWORD=GrafanaSecurePassword2024!

# Let's Encrypt (cambia por tu email real)
LETSENCRYPT_EMAIL=admin@pnaltsw.site

# Traefik Dashboard (formato: usuario:hash_de_contraseña)
# Genera con: ./generate-traefik-password.sh
TRAEFIK_DASHBOARD_AUTH=admin:$$2y$$05$$Pv0ZEpv6vMBauSXfUbzRuu1W4.VlEjml42udd3s0J669JhwyQ4dFq
```

### Credenciales por Defecto (Desarrollo Local)

**MySQL:**
- Puerto: `3307`
- Usuario: `appuser`
- Contraseña: `apppassword`
- Base de datos: `access_control_db`

**MongoDB:**
- Puerto: `27018`
- Usuario: `admin`
- Contraseña: `adminpassword`
- Base de datos: `login_db`

**RabbitMQ:**
- Puerto: `5673`
- Management UI: `http://localhost:15673`
- Usuario: `admin`
- Contraseña: `adminpassword`

**Grafana:**
- Usuario: `admin`
- Contraseña: `adminpassword`

## Testing

### Ejecutar tests de los servicios

Si necesitas ejecutar los tests de los microservicios, puedes hacerlo de dos formas:

**Opción 1: Ejecutar tests dentro del contenedor Docker**
```bash
docker-compose exec [nombre-servicio] mvn test
# Ejemplo: docker-compose exec login-service mvn test
```

**Opción 2: Ejecutar tests localmente (requiere Java y Maven instalados)**
```bash
cd login-service && mvn test
cd ../employee-service && mvn test
# ... etc
```

## Monitoreo

### RabbitMQ
**Para qué sirve**: Sistema de mensajería asíncrona entre microservicios. Cuando un servicio necesita comunicarse con otro sin esperar respuesta inmediata, envía mensajes a través de RabbitMQ.

**Qué ver**: 
- **URL**: http://localhost:15673 (usuario: `admin`, contraseña: `adminpassword`)
- **Verás**: Colas de mensajes, mensajes en cola, mensajes procesados, conexiones activas
- **Útil para**: Detectar si hay mensajes acumulados o si algún servicio no está procesando mensajes

### Prometheus
**Para qué sirve**: Recolecta y almacena métricas de todos los servicios (cada 15 segundos).

**Cómo ver datos:**
1. Abre http://localhost:9090
2. En el campo grande de consulta, escribe: `up`
3. Haz clic en **Execute** (botón azul)
4. Deberías ver una tabla con todos los servicios y su estado (up=1 significa funcionando)

**Verificar que está scrapeando correctamente:**
- Ve a **Status** → **Targets** (menú superior)
- Todos los servicios deben estar en estado **"UP"** (verde)
- Si alguno está "DOWN", revisa los logs: `docker-compose logs [nombre-servicio]`

### Grafana
**Para qué sirve**: Visualiza las métricas de Prometheus en gráficos y dashboards.

**Cómo ver métricas (paso a paso):**
1. Abre http://localhost:3000 (usuario: `admin`, contraseña: `adminpassword`)
2. Haz clic en **Dashboards** → **Create dashboard** (botón azul grande)
3. Haz clic en **Add visualization**
4. Verifica que el datasource sea **Prometheus** (debe decir "Prome" o "Prometheus" arriba)
5. En el campo de consulta (debajo de "Kick start your query"), escribe: `up`
6. Haz clic en el botón azul **Run queries** (abajo a la derecha)
7. Deberías ver un gráfico con líneas en el valor 1.0 (cada servicio es una línea de color diferente)

**Qué deberías ver:**
- Un gráfico con líneas horizontales en el valor 1.0
- Una leyenda abajo con cada servicio (api-gateway, login-service, etc.)
- El gráfico se actualiza cada 15 segundos automáticamente
- Puedes cambiar el rango de tiempo arriba (última hora, último día, etc.)

**Si no ves datos:**
- Primero verifica Prometheus: http://localhost:9090 → **Status** → **Targets** (todos deben estar "UP" en verde)
- Si algún servicio está DOWN, revisa los logs: `docker-compose logs [nombre-servicio]`

**Consultas útiles:**
- `up` - Estado de todos los servicios (1=UP, 0=DOWN)
- `rate(http_server_requests_seconds_count[5m])` - Solicitudes HTTP por segundo
- `jvm_memory_used_bytes{area="heap"}` - Memoria JVM usada

## Acceso Rápido

### URLs Importantes
- **Frontend**: http://localhost:3001
- **API Gateway**: http://localhost:8080
- **Grafana**: http://localhost:3000
- **Swagger UI**:
  - Login Service: http://localhost:8081/swagger-ui.html
  - Employee Service: http://localhost:8082/swagger-ui.html
  - Access Control Service: http://localhost:8083/swagger-ui.html
  - Alert Service: http://localhost:8084/swagger-ui.html
  - Auth Service: http://localhost:8085/swagger-ui.html
  - SAGA Orchestrator: http://localhost:8086/swagger-ui.html

### Crear Usuario de Prueba
```bash
POST http://localhost:8080/login/createuser
{
  "username": "admin",
  "password": "admin123",
  "email": "admin@example.com"
}
```

Luego inicia sesión en http://localhost:3001/login con:
- Usuario: `admin`
- Contraseña: `admin123`
- Código MFA: (se muestra en la consola del Login Service)

## Troubleshooting

### Problemas Comunes

1. **Puertos ocupados**: Verificar que los puertos estén libres antes de ejecutar `docker-compose up`
   ```bash
   # Verificar si un puerto está en uso (ejemplo para puerto 8080)
   netstat -ano | findstr :8080
   ```

2. **Servicios no inician**: Verificar los logs de Docker
   ```bash
   docker-compose logs [nombre-servicio]
   # Ver todos los logs: docker-compose logs
   ```

3. **Bases de datos no conectan**: Verificar que los contenedores estén corriendo
   ```bash
   docker-compose ps
   ```

4. **Reiniciar un servicio específico**:
   ```bash
   docker-compose restart [nombre-servicio]
   ```

5. **Reconstruir todos los servicios**:
   ```bash
   docker-compose down
   docker-compose up -d --build
   ```

6. **Error 403 (Forbidden) en producción**:
   - Verifica que CORS esté configurado con el dominio de producción en `CorsConfig.java`
   - Verifica que Traefik tenga las rutas correctas configuradas
   - Revisa los logs de Traefik: `docker-compose -f docker-compose.prod.yml logs traefik`
   - Revisa los logs del API Gateway: `docker-compose -f docker-compose.prod.yml logs api-gateway`

7. **Frontend no se conecta al backend**: 
   - Verificar que el API Gateway esté corriendo: `docker-compose ps api-gateway`
   - Verificar que el puerto 8080 esté accesible: `http://localhost:8080/actuator/health`

8. **Error MFA**: El código MFA se muestra en los logs del Login Service
   ```bash
   docker-compose logs -f login-service
   ```

9. **Limpiar todo y empezar de nuevo**:
   ```bash
   docker-compose down -v  # Elimina también los volúmenes
   docker-compose up -d --build
   ```

## Licencia

Este proyecto es parte de un trabajo académico.

## Autores

Sistema desarrollado según especificaciones del proyecto.
