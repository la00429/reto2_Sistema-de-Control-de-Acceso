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

### Variables de Entorno

Los servicios usan configuración por defecto. Para producción, crear archivos `application-prod.yml` en cada servicio.

### Credenciales por Defecto

**MySQL:**
- Puerto: `3307` (cambiado de 3306 para evitar conflictos)
- Usuario: `appuser`
- Contraseña: `apppassword`
- Base de datos: `access_control_db`

**MongoDB:**
- Puerto: `27018` (cambiado de 27017 para evitar conflictos)
- Usuario: `admin`
- Contraseña: `adminpassword`
- Base de datos: `login_db`

**RabbitMQ:**
- Puerto: `5673` (mapeado desde 5672 interno)
- Management UI: `http://localhost:15673` (mapeado desde 15672 interno)
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

El sistema incluye monitoreo completo con Prometheus y Grafana para visualizar métricas en tiempo real, detectar problemas y analizar el rendimiento del sistema.

### Herramientas de Monitoreo

- **Prometheus**: `http://localhost:9090` - Base de datos de métricas
- **Grafana**: `http://localhost:3000` - Visualización de métricas (usuario: `admin`, contraseña: `adminpassword`)
- **RabbitMQ Management**: `http://localhost:15673` - Gestión de colas de mensajes (usuario: `admin`, contraseña: `adminpassword`)

### Dashboards Disponibles en Grafana

1. **Services Health Dashboard**: Estado de salud, latencia HTTP y uso de memoria de todos los servicios
2. **SAGA Orchestrator Dashboard**: Monitoreo de transacciones distribuidas (iniciadas, completadas, compensadas, fallidas)
3. **RabbitMQ Dashboard**: Estado de colas, mensajes procesados y Dead Letter Queue

### Métricas Monitoreadas

- **Salud de servicios**: Estado UP/DOWN de cada microservicio
- **Rendimiento HTTP**: Latencia, tasa de solicitudes, errores
- **Métricas de negocio**: Registros de acceso creados, validaciones fallidas, alertas enviadas
- **Transacciones SAGA**: Sagas iniciadas, completadas, compensadas, tiempos de ejecución
- **RabbitMQ**: Mensajes en cola, tasa de procesamiento, mensajes fallidos
- **Recursos del sistema**: Memoria JVM, CPU, threads

Para más información sobre cómo usar Grafana, consulta el archivo `MONITOREO_GRAFANA.md`.

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

6. **Frontend no se conecta al backend**: 
   - Verificar que el API Gateway esté corriendo: `docker-compose ps api-gateway`
   - Verificar que el puerto 8080 esté accesible: `http://localhost:8080/actuator/health`

7. **Error MFA**: El código MFA se muestra en los logs del Login Service
   ```bash
   docker-compose logs -f login-service
   ```

8. **Limpiar todo y empezar de nuevo**:
   ```bash
   docker-compose down -v  # Elimina también los volúmenes
   docker-compose up -d --build
   ```

## Licencia

Este proyecto es parte de un trabajo académico.

## Autores

Sistema desarrollado según especificaciones del proyecto.
