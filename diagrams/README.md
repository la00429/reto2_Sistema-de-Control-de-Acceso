# Diagramas del Sistema de Control de Acceso Peatonal

Esta carpeta contiene todos los diagramas PlantUML del proyecto.

## Diagramas Principales

### 1. Diagrama de Componentes
- **Archivo:** `diagrama_componentes_mejorado.puml`
- **Descripción:** Muestra la arquitectura de microservicios, componentes y sus relaciones
- **Capas:** Presentación, API Gateway, Microservicios, Bases de Datos, Monitoreo

### 2. Diagrama de Despliegue
- **Archivo:** `diagrama_despliegue.puml`
- **Descripción:** Muestra la infraestructura y distribución de componentes en Kubernetes
- **Capas:** Cliente Web, Servidor de Aplicaciones, ORM, Mensajería, Bases de Datos, Monitoreo

### 3. Diagrama de Casos de Uso
- **Archivo:** `diagrama_casos_uso_mejorado.puml`
- **Descripción:** Muestra los casos de uso principales del sistema
- **Actores:** Administrador, Empleado, Sistema de Alertas
- **Paquetes:** Autenticación, Gestión de Empleados, Control de Acceso, Reportes, Alertas

## Diagramas de Casos de Uso Detallados

### Casos de Uso por Módulo
- `casos_uso_autenticacion.puml` - Casos de uso de autenticación
- `casos_uso_empleados.puml` - Casos de uso de gestión de empleados
- `casos_uso_acceso.puml` - Casos de uso de control de acceso
- `casos_uso_reportes.puml` - Casos de uso de reportes
- `casos_uso_alertas.puml` - Casos de uso de alertas
- `casos_uso_completo.puml` - Vista completa de todos los casos de uso

## Diagramas de Actividad

### Diagrama de Actividad de Login
- **Archivo:** `diagrama_actividad_login_mejorado.puml`
- **Descripción:** Flujo de proceso de autenticación con MFA

## Cómo Visualizar los Diagramas

### Opción 1: PlantUML Online
1. Visitar http://www.plantuml.com/plantuml/uml/
2. Copiar el contenido del archivo .puml
3. Pegar en el editor
4. El diagrama se generará automáticamente

### Opción 2: Plugin de VS Code
1. Instalar la extensión "PlantUML" en VS Code
2. Abrir el archivo .puml
3. Presionar Alt+D para previsualizar

### Opción 3: PlantUML Local
1. Instalar Java
2. Descargar plantuml.jar desde http://plantuml.com/download
3. Ejecutar: `java -jar plantuml.jar diagrama.puml`

## Estructura de los Diagramas

Todos los diagramas siguen las convenciones UML estándar:
- **Componentes:** Representan servicios y módulos
- **Interfaces (Colombinas):** Definen contratos
- **Dependencias:** Muestran relaciones entre componentes
- **Notas:** Proporcionan información adicional

## Notas

- Los diagramas están en formato PlantUML (.puml)
- Pueden editarse con cualquier editor de texto
- Son versionados junto con el código del proyecto
- Se actualizan cuando hay cambios arquitectónicos significativos




