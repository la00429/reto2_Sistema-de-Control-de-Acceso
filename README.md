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
  - RabbitMQ (Puerto 5672) - Mensajería asíncrona
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

- Java 17+
- Node.js 18+
- Docker y Docker Compose
- Maven 3.8+

## Instalación

### 1. Iniciar Infraestructura
```bash
docker-compose up -d
```

Esto iniciará:
- MySQL (Puerto 3307)
- MongoDB (Puerto 27018)
- RabbitMQ (Puerto 5673, Management: 15673)
- Prometheus (Puerto 9090)
- Grafana (Puerto 3000)

### 2. Compilar Microservicios
```bash
# Compilar cada servicio
cd api-gateway && mvn clean install
cd ../login-service && mvn clean install
cd ../auth-service && mvn clean install
cd ../employee-service && mvn clean install
cd ../access-control-service && mvn clean install
cd ../alert-service && mvn clean install
cd ../saga-service && mvn clean install
```

### 3. Iniciar Servicios (en orden)

**Opción A: Desde IDE**
1. Alert Service
2. Auth Service
3. Employee Service
4. Login Service
5. Access Control Service
6. SAGA Orchestrator
7. API Gateway

**Opción B: Desde línea de comandos**
```bash
# En terminales separadas
cd alert-service && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd employee-service && mvn spring-boot:run
cd login-service && mvn spring-boot:run
cd access-control-service && mvn spring-boot:run
cd saga-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 4. Iniciar Frontend
```bash
cd frontend
npm install
npm run dev
```

El frontend estará disponible en: `http://localhost:3001` (puerto 3000 está ocupado por Grafana)

## Acceso a la Aplicación

### Frontend Web
Una vez iniciado el frontend, accede a:
- **URL**: http://localhost:3001
- **Página de Login**: http://localhost:3001/login

### Credenciales de Prueba
Para acceder al sistema, primero debes crear un usuario usando el endpoint de registro o directamente en la base de datos.

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
1. Accede a http://localhost:3000/login
2. Ingresa tus credenciales
3. El sistema generará un código MFA de 6 dígitos
4. Ingresa el código MFA para completar la autenticación
5. Serás redirigido al Dashboard

### Páginas Disponibles
- **Login**: http://localhost:3000/login
- **Dashboard**: http://localhost:3000/dashboard
- **Empleados**: http://localhost:3000/employees
- **Control de Acceso**: http://localhost:3000/access-control
- **Reportes**: http://localhost:3000/reports

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
- Puerto: `5673` (cambiado de 5672 para evitar conflictos)
- Management UI: `http://localhost:15673` (cambiado de 15672)
- Usuario: `admin`
- Contraseña: `adminpassword`

**Grafana:**
- Usuario: `admin`
- Contraseña: `adminpassword`

## Testing

```bash
# Ejecutar tests de cada servicio
cd login-service && mvn test
cd ../employee-service && mvn test
# ... etc
```

## Monitoreo

- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`
- **RabbitMQ Management**: `http://localhost:15673`

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

1. **Puertos ocupados**: Verificar que los puertos estén libres
2. **Bases de datos no conectan**: Verificar que Docker esté corriendo
3. **RabbitMQ no responde**: Verificar credenciales y puerto
4. **Frontend no se conecta**: Verificar que el API Gateway esté corriendo en puerto 8080
5. **Error MFA**: El código MFA se muestra en la consola del Login Service (no en el navegador)

## Licencia

Este proyecto es parte de un trabajo académico.

## Autores

Sistema desarrollado según especificaciones del proyecto.
