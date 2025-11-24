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
    console.log('→ Petición enviada:', config.method?.toUpperCase(), config.url)
    console.log('→ Base URL:', config.baseURL)
    console.log('→ URL completa:', `${config.baseURL}${config.url}`)
    console.log('→ Headers:', config.headers)
    console.log('→ Data:', config.data)
    return config
  },
  (error) => {
    console.error('✗ Error en interceptor de request:', error)
    return Promise.reject(error)
  }
)

// Interceptor para manejar errores de autenticación
api.interceptors.response.use(
  (response) => {
    console.log('✓ Respuesta recibida:', response.status, response.config?.url)
    return response
  },
  (error) => {
    // Log detallado de errores para depuración
    console.error('✗ Error en petición:', error.config?.method?.toUpperCase(), error.config?.url)
    
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
      console.error('❌ Error de red detectado')
      console.error('URL solicitada:', error.config?.url)
      console.error('Base URL:', error.config?.baseURL)
      console.error('URL completa:', `${error.config?.baseURL}${error.config?.url}`)
      console.error('Método:', error.config?.method)
      console.error('Headers:', error.config?.headers)
      console.error('Data enviada:', error.config?.data)
      console.error('Verifica:')
      console.error('1. El API Gateway esté corriendo en http://localhost:8080')
      console.error('2. El servicio de destino esté corriendo')
      console.error('3. No haya problemas de CORS (revisa la consola de Network)')
    }
    
    if (error.response) {
      console.log('✓ El servidor respondió (aunque con error):', error.response.status)
      console.log('Respuesta completa:', error.response)
    } else {
      console.error('✗ El servidor NO respondió (error.response es undefined)')
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



