import React, { createContext, useState, useContext, useEffect } from 'react'
import api from '../services/api'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    
    if (token && username) {
      setIsAuthenticated(true)
      setUser({ username })
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    }
    setLoading(false)
  }, [])

  const login = async (username, password) => {
    try {
      const response = await api.post('/login/authuser', {
        username,
        password
      })
      
      // Log para debugging
      console.log('Login response recibida:', response.data)
      console.log('Success:', response.data.success)
      console.log('RequiresMFA:', response.data.requiresMFA)
      console.log('Token presente:', !!response.data.token)
      
      // Si requiere MFA, devolver el código MFA temporal
      if (response.data.success === true && response.data.requiresMFA === true) {
        console.log('Login exitoso con MFA requerido')
        return { 
          success: true, 
          requiresMFA: true, 
          mfaToken: response.data.token, // Código MFA temporal
          username: response.data.username || username,
          message: response.data.message || 'Ingresa el código MFA'
        }
      }
      
      // Si ya tiene token JWT (sin MFA o después de verificar)
      if (response.data.success === true && response.data.token && response.data.requiresMFA !== true) {
        const token = response.data.token
        localStorage.setItem('token', token)
        localStorage.setItem('username', username)
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`
        setIsAuthenticated(true)
        setUser({ username })
        return { success: true }
      }
      
      console.warn('Login falló. Respuesta recibida:', response.data)
      return { success: false, message: response.data.message || 'Error al iniciar sesión' }
    } catch (error) {
      console.error('Error en login:', error)
      console.error('Error response:', error.response?.data)
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Error al iniciar sesión'
      }
    }
  }

  const verifyMFA = async (username, mfaToken) => {
    try {
      console.log('Verificando MFA para usuario:', username, 'con token:', mfaToken)
      
      // Llamar al Auth Service a través del API Gateway para validar MFA y obtener JWT
      const response = await api.post('/auth/mfa/validate', {
        username,
        token: mfaToken
      })
      
      console.log('Respuesta completa de verificación MFA:', response)
      console.log('Respuesta data:', response.data)
      console.log('Valid:', response.data?.valid)
      console.log('AccessToken presente:', !!response.data?.accessToken)
      
      // Verificar si la respuesta tiene la estructura esperada
      if (response.data && response.data.valid === true) {
        if (response.data.accessToken) {
          const token = response.data.accessToken
          localStorage.setItem('token', token)
          localStorage.setItem('username', username)
          api.defaults.headers.common['Authorization'] = `Bearer ${token}`
          setIsAuthenticated(true)
          setUser({ username })
          console.log('MFA verificado exitosamente. Token guardado.')
          return { success: true }
        } else {
          console.error('MFA válido pero no se recibió accessToken. Respuesta:', response.data)
          return { success: false, message: 'Error: No se recibió el token de acceso' }
        }
      }
      
      console.warn('MFA inválido o respuesta inesperada. Respuesta completa:', response.data)
      return { success: false, message: response.data?.message || 'Código MFA inválido' }
    } catch (error) {
      console.error('Error al verificar MFA:', error)
      console.error('Error response:', error.response?.data)
      console.error('Error status:', error.response?.status)
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Error al verificar código MFA'
      }
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    delete api.defaults.headers.common['Authorization']
    setIsAuthenticated(false)
    setUser(null)
  }

  const value = {
    isAuthenticated,
    user,
    login,
    verifyMFA,
    logout,
    loading
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

