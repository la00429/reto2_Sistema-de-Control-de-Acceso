import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  CircularProgress,
  InputAdornment
} from '@mui/material'
import { Person as PersonIcon, Lock as LockIcon } from '@mui/icons-material'
import { useAuth } from '../context/AuthContext'
import HelpIcon from '../components/HelpIcon'
import LoadingButton from '../components/LoadingButton'

function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [mfaToken, setMfaToken] = useState('')
  const [requiresMFA, setRequiresMFA] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login, verifyMFA } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    if (requiresMFA) {
      // Verificar código MFA
      const result = await verifyMFA(username, mfaToken)
      
      if (result.success) {
        navigate('/')
      } else {
        setError(result.message || 'Código MFA inválido')
      }
    } else {
      // Login inicial
      const result = await login(username, password)
      
      if (result.success && result.requiresMFA) {
        setRequiresMFA(true)
        setError('')
        // El código MFA se muestra en la consola del servidor
        // En producción se enviaría por email/SMS
      } else if (result.success) {
        navigate('/')
      } else {
        setError(result.message || 'Error al iniciar sesión')
      }
    }
    
    setLoading(false)
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        <Paper 
          elevation={3} 
          sx={{ 
            p: { xs: 3, sm: 4 }, 
            width: '100%',
            maxWidth: 480,
            background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
            border: '1px solid rgba(0, 0, 0, 0.08)',
          }}
        >
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography 
              variant="h4" 
              component="h1" 
              gutterBottom 
              sx={{
                fontWeight: 700,
                background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                mb: 1,
              }}
            >
              Sistema de Control de Acceso
            </Typography>
            <Typography 
              variant="subtitle1" 
              color="text.secondary" 
              sx={{ 
                fontWeight: 500,
                letterSpacing: '0.02em',
              }}
            >
              Iniciar Sesión
            </Typography>
          </Box>

          {error && (
            <Alert 
              severity="error" 
              sx={{ 
                mb: 3,
                borderRadius: 2,
                '& .MuiAlert-icon': {
                  alignItems: 'center',
                },
              }}
              onClose={() => setError('')}
            >
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            {!requiresMFA ? (
              <>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1, mt: 1 }}>
                  <TextField
                    fullWidth
                    label="Usuario"
                    variant="outlined"
                    margin="normal"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    autoFocus
                    disabled={loading}
                    helperText={username ? "Ingresa tu nombre de usuario del sistema" : ""}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <PersonIcon fontSize="small" color="action" />
                        </InputAdornment>
                      ),
                    }}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        transition: 'all 0.2s ease-in-out',
                        '&:hover': {
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'primary.main',
                          },
                        },
                      },
                    }}
                  />
                  <Box sx={{ mt: 3 }}>
                    <HelpIcon 
                      title="Ingresa el nombre de usuario que te fue asignado. Si no tienes uno, contacta al administrador del sistema."
                      placement="right"
                    />
                  </Box>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                  <TextField
                    fullWidth
                    label="Contraseña"
                    type="password"
                    variant="outlined"
                    margin="normal"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={loading}
                    helperText={password ? "Ingresa tu contraseña de acceso" : ""}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <LockIcon fontSize="small" color="action" />
                        </InputAdornment>
                      ),
                    }}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        transition: 'all 0.2s ease-in-out',
                        '&:hover': {
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'primary.main',
                          },
                        },
                      },
                    }}
                  />
                  <Box sx={{ mt: 3 }}>
                    <HelpIcon 
                      title="Ingresa tu contraseña de acceso. Si la has olvidado, contacta al administrador para restablecerla."
                      placement="right"
                    />
                  </Box>
                </Box>
                <LoadingButton
                  type="submit"
                  fullWidth
                  variant="contained"
                  size="large"
                  loading={loading}
                  sx={{ 
                    mt: 3, 
                    mb: 2,
                    py: 1.5,
                    fontSize: '1rem',
                    fontWeight: 600,
                  }}
                >
                  Iniciar Sesión
                </LoadingButton>
              </>
            ) : (
              <>
                <Alert 
                  severity="info" 
                  sx={{ 
                    mb: 3,
                    borderRadius: 2,
                  }}
                >
                  <Typography variant="body2" component="div">
                    Se ha generado un código MFA. Revisa la consola del servidor (Auth Service) para ver el código.
                    <br />
                    <strong>Nota:</strong> En producción, el código se enviaría por email/SMS.
                  </Typography>
                </Alert>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                  <TextField
                    fullWidth
                    label="Código MFA (6 dígitos)"
                    variant="outlined"
                    margin="normal"
                    value={mfaToken}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '')
                      setMfaToken(value)
                    }}
                    required
                    autoFocus
                    disabled={loading}
                    inputProps={{ 
                      maxLength: 6,
                      inputMode: 'numeric',
                      pattern: '[0-9]*',
                    }}
                    helperText={
                      mfaToken.length === 6 
                        ? "✓ Código completo" 
                        : `Ingresa ${6 - mfaToken.length} dígito${6 - mfaToken.length !== 1 ? 's' : ''} más`
                    }
                    error={mfaToken.length > 0 && mfaToken.length < 6}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        fontSize: '1.25rem',
                        letterSpacing: '0.5em',
                        textAlign: 'center',
                        fontWeight: 600,
                      },
                    }}
                  />
                  <Box sx={{ mt: 3 }}>
                    <HelpIcon 
                      title="El código MFA (Autenticación Multifactor) es un código de 6 dígitos que se genera automáticamente para mayor seguridad. En producción, este código se enviaría por email o SMS. Actualmente, puedes verlo en la consola del servidor."
                      placement="right"
                    />
                  </Box>
                </Box>
                <LoadingButton
                  type="submit"
                  fullWidth
                  variant="contained"
                  size="large"
                  loading={loading}
                  disabled={mfaToken.length !== 6}
                  sx={{ 
                    mt: 3, 
                    mb: 2,
                    py: 1.5,
                    fontSize: '1rem',
                    fontWeight: 600,
                  }}
                >
                  Verificar Código MFA
                </LoadingButton>
                <Button
                  fullWidth
                  variant="outlined"
                  size="large"
                  sx={{ mt: 1 }}
                  onClick={() => {
                    setRequiresMFA(false)
                    setMfaToken('')
                    setError('')
                  }}
                  disabled={loading}
                >
                  Volver
                </Button>
              </>
            )}
          </form>
        </Paper>
      </Box>
    </Container>
  )
}

export default Login

