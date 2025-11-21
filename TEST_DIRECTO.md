# Prueba Directa del Login Service

Para verificar si el problema está en el api-gateway o en el login-service, prueba hacer una petición directa al login-service:

## Prueba 1: Login Service Directo

```bash
curl -v -X POST http://localhost:8081/login/authuser \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3001" \
  -d '{"username":"admin","password":"admin"}'
```

Si esta petición funciona y devuelve la respuesta JSON correcta, entonces el problema está en el api-gateway.

## Prueba 2: A través del API Gateway

```bash
curl -v -X POST http://localhost:8080/login/authuser \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3001" \
  -d '{"username":"admin","password":"admin"}'
```

Compara ambas respuestas. Si la primera funciona pero la segunda no, el problema está en el gateway.

## Solución Temporal

Si el problema está en el gateway, puedes temporalmente configurar el frontend para que apunte directamente al login-service:

En `frontend/src/services/api.js`, cambia:
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8081',  // Directo al login-service
  headers: {
    'Content-Type': 'application/json'
  }
})
```

Pero esto solo funciona para el login-service. Para otros servicios, necesitarás pasar por el gateway.


