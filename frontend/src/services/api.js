import axios from 'axios'

// Configuración del API Gateway
// En desarrollo y producción, usar rutas relativas que serán manejadas por el proxy
const getBaseURL = () => {
  // Usar siempre rutas relativas que serán manejadas por Nginx en producción
  // o por el proxy de Vite en desarrollo
  return '/api'
}

const api = axios.create({
  baseURL: getBaseURL(),
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 60000, // 60 segundos de timeout
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
    console.error('Error en interceptor de request:', error)
    return Promise.reject(error)
  }
)

// Interceptor para manejar errores de autenticación
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
      console.error('Error de red detectado')
      console.error('URL solicitada:', error.config?.url)
      console.error('Base URL:', error.config?.baseURL)
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
