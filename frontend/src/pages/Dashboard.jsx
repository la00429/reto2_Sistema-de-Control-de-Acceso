import React, { useCallback, useEffect, useState } from 'react'
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button
} from '@mui/material'
import {
  People as PeopleIcon,
  Security as SecurityIcon,
  Assessment as AssessmentIcon
} from '@mui/icons-material'
import api from '../services/api'

function Dashboard() {
  const [stats, setStats] = useState({
    employees: 0,
    accessRecords: 0,
    todayAccess: 0
  })
  const [lastUpdated, setLastUpdated] = useState(null)
  const [loading, setLoading] = useState(false)

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true)
      
      // Hacer las peticiones de forma independiente para que una no bloquee a la otra
      const employeesPromise = api.get('/employee/findallemployees').catch(err => {
        console.error('Error obteniendo empleados:', err)
        return { data: [] }
      })
      
      const accessPromise = api.get('/access/history').catch(err => {
        console.error('Error obteniendo historial de acceso:', err)
        return { data: [] }
      })
      
      const [employeesRes, accessRes] = await Promise.all([employeesPromise, accessPromise])

      console.log('Dashboard - Respuesta de empleados:', employeesRes.data)
      console.log('Dashboard - Tipo de respuesta:', typeof employeesRes.data)
      console.log('Dashboard - Es array?', Array.isArray(employeesRes.data))
      console.log('Dashboard - Número de empleados:', Array.isArray(employeesRes.data) ? employeesRes.data.length : 'No es un array')
      console.log('Dashboard - Respuesta de acceso:', accessRes.data)

      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const todayAccess = Array.isArray(accessRes.data) ? accessRes.data.filter(record => {
        if (!record.accessTimestamp) return false
        const recordDate = new Date(record.accessTimestamp)
        return recordDate >= today
      }) : []

      setStats({
        employees: Array.isArray(employeesRes.data) ? employeesRes.data.length : 0,
        accessRecords: Array.isArray(accessRes.data) ? accessRes.data.length : 0,
        todayAccess: todayAccess.length
      })
      setLastUpdated(new Date())
    } catch (error) {
      console.error('Error fetching stats:', error)
      console.error('Error response:', error.response)
      console.error('Error code:', error.code)
      console.error('Error message:', error.message)
      
      if (error.code === 'ERR_NETWORK') {
        console.error('ERROR DE RED: El API Gateway no está respondiendo.')
        console.error('Por favor verifica que:')
        console.error('1. El API Gateway esté corriendo en el puerto 8080')
        console.error('2. No haya problemas de CORS')
        console.error('3. El servicio esté accesible desde el navegador')
      }
      
      // Intentar obtener al menos los empleados si el historial falla
      try {
        const employeesRes = await api.get('/employee/findallemployees').catch(() => ({ data: [] }))
        setStats({
          employees: Array.isArray(employeesRes.data) ? employeesRes.data.length : 0,
          accessRecords: 0,
          todayAccess: 0
        })
        setLastUpdated(new Date())
      } catch (e) {
        console.error('Error al obtener empleados como fallback:', e)
        setStats({
          employees: 0,
          accessRecords: 0,
          todayAccess: 0
        })
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    // Prueba de conexión inicial
    const testConnection = async () => {
      try {
        console.log('Dashboard - Probando conexión con el API...')
        console.log('Base URL:', api.defaults.baseURL)
        const testRes = await api.get('/employee/findallemployees')
        console.log('Dashboard - Conexión exitosa:', testRes.status)
      } catch (error) {
        console.error('Dashboard - Error en prueba de conexión:', error)
        console.error('Dashboard - Código de error:', error.code)
        console.error('Dashboard - Mensaje:', error.message)
      }
    }
    
    testConnection()
    fetchStats()
    const interval = setInterval(fetchStats, 30000)
    
    // Escuchar eventos de actualización de empleados
    const handleEmployeeUpdate = () => {
      fetchStats()
    }
    window.addEventListener('employeeUpdated', handleEmployeeUpdate)
    
    return () => {
      clearInterval(interval)
      window.removeEventListener('employeeUpdated', handleEmployeeUpdate)
    }
  }, [fetchStats])

  const statCards = [
    {
      title: 'Empleados',
      value: stats.employees,
      icon: <PeopleIcon sx={{ fontSize: 40 }} />,
      color: '#2E75B6'
    },
    {
      title: 'Registros de Acceso',
      value: stats.accessRecords,
      icon: <SecurityIcon sx={{ fontSize: 40 }} />,
      color: '#4682B4'
    },
    {
      title: 'Accesos Hoy',
      value: stats.todayAccess,
      icon: <AssessmentIcon sx={{ fontSize: 40 }} />,
      color: '#5A9BD4'
    }
  ]

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Dashboard
        </Typography>
        <Button onClick={fetchStats} variant="outlined" disabled={loading}>
          {loading ? 'Actualizando...' : 'Actualizar'}
        </Button>
      </Box>
      {lastUpdated && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          Última actualización: {lastUpdated.toLocaleString('es-ES')}
        </Typography>
      )}
      <Grid container spacing={3} sx={{ mt: 2 }}>
        {statCards.map((card, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Paper
              elevation={3}
              sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                borderTop: `4px solid ${card.color}`
              }}
            >
              <Box sx={{ color: card.color, mb: 2 }}>
                {card.icon}
              </Box>
              <Typography variant="h3" component="div" gutterBottom>
                {card.value}
              </Typography>
              <Typography variant="h6" color="text.secondary">
                {card.title}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Container>
  )
}

export default Dashboard

