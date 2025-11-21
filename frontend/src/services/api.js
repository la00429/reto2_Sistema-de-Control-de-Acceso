import axios from 'axios'

// Determinar la URL base del API Gateway
// Intentar localhost primero, si falla usar 127.0.0.1
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 30000, // 30 segundos de timeout
  withCredentials: false // Desactivar credentials para evitar problemas de CORS
})

// Interceptor para agregar token a todas las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor para manejar errores de autenticación
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Log detallado de errores para depuración
    if (error.code === 'ERR_NETWORK') {
      console.error('Error de red - El servidor no responde o hay un problema de CORS')
      console.error('URL solicitada:', error.config?.url)
      console.error('Base URL:', error.config?.baseURL)
      console.error('Verifica que el API Gateway esté corriendo en el puerto 8080')
    }
    
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api



