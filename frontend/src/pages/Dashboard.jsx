import React, { useCallback, useEffect, useState } from 'react'
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  IconButton,
  Tooltip
} from '@mui/material'
import {
  People as PeopleIcon,
  Security as SecurityIcon,
  Assessment as AssessmentIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material'
import api from '../services/api'
import HelpIcon from '../components/HelpIcon'
import SkeletonCard from '../components/SkeletonCard'

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
      color: '#1976d2',
      gradient: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
      helpText: 'Total de empleados registrados en el sistema, incluyendo activos e inactivos.'
    },
    {
      title: 'Registros de Acceso',
      value: stats.accessRecords,
      icon: <SecurityIcon sx={{ fontSize: 40 }} />,
      color: '#9c27b0',
      gradient: 'linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%)',
      helpText: 'Total de registros de acceso (entradas y salidas) almacenados en el historial del sistema.'
    },
    {
      title: 'Accesos Hoy',
      value: stats.todayAccess,
      icon: <AssessmentIcon sx={{ fontSize: 40 }} />,
      color: '#10b981',
      gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)',
      helpText: 'Número de accesos registrados desde el inicio del día actual (00:00:00 hasta ahora).'
    }
  ]

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            Dashboard
          </Typography>
          <HelpIcon 
            title="El Dashboard muestra un resumen de las estadísticas principales del sistema: número de empleados registrados, total de registros de acceso y accesos del día actual. Los datos se actualizan automáticamente cada 30 segundos."
            placement="right"
          />
        </Box>
        <Tooltip title="Actualizar estadísticas" arrow>
          <IconButton
            onClick={fetchStats}
            disabled={loading}
            sx={{
              border: '1px solid',
              borderColor: 'divider',
              '&:hover': {
                backgroundColor: 'action.hover',
                transform: 'rotate(180deg)',
              },
              transition: 'transform 0.5s ease-in-out',
            }}
          >
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>
      {lastUpdated && (
        <Typography 
          variant="body2" 
          color="text.secondary" 
          sx={{ 
            mb: 3,
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
          }}
        >
          <Box
            component="span"
            sx={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              backgroundColor: 'success.main',
              display: 'inline-block',
              animation: 'pulse 2s infinite',
              '@keyframes pulse': {
                '0%, 100%': {
                  opacity: 1,
                },
                '50%': {
                  opacity: 0.5,
                },
              },
            }}
          />
          Última actualización: {lastUpdated.toLocaleString('es-ES')}
        </Typography>
      )}
      <Grid container spacing={{ xs: 2, sm: 3 }}>
        {loading && statCards.length === 0 ? (
          <>
            {[1, 2, 3].map((i) => (
              <Grid item xs={12} sm={6} md={4} key={i}>
                <SkeletonCard />
              </Grid>
            ))}
          </>
        ) : (
          statCards.map((card, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Paper
              elevation={3}
              sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                background: `linear-gradient(135deg, ${card.color}15 0%, ${card.color}08 100%)`,
                border: `1px solid ${card.color}30`,
                position: 'relative',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                overflow: 'hidden',
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  height: '4px',
                  background: card.gradient,
                },
                '&:hover': {
                  transform: 'translateY(-6px) scale(1.02)',
                  boxShadow: `0 12px 24px ${card.color}25`,
                  borderColor: `${card.color}50`,
                }
              }}
            >
              <Box sx={{ position: 'absolute', top: 8, right: 8, zIndex: 1 }}>
                <HelpIcon 
                  title={card.helpText}
                  placement="left"
                />
              </Box>
              <Box 
                sx={{ 
                  color: card.color, 
                  mb: 2,
                  p: 1.5,
                  borderRadius: 2,
                  background: `${card.color}10`,
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    background: `${card.color}20`,
                    transform: 'scale(1.1)',
                  }
                }}
              >
                {card.icon}
              </Box>
              <Typography 
                variant="h3" 
                component="div" 
                gutterBottom
                sx={{
                  fontWeight: 700,
                  background: card.gradient,
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  mb: 1,
                }}
              >
                {card.value}
              </Typography>
              <Typography 
                variant="h6" 
                color="text.secondary"
                sx={{ fontWeight: 500 }}
              >
                {card.title}
              </Typography>
            </Paper>
          </Grid>
        ))
        )}
      </Grid>
    </Container>
  )
}

export default Dashboard

