import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert
} from '@mui/material'
import { useAuth } from '../context/AuthContext'

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
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" gutterBottom align="center">
            Sistema de Control de Acceso
          </Typography>
          <Typography variant="subtitle1" align="center" color="text.secondary" sx={{ mb: 3 }}>
            Iniciar Sesión
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            {!requiresMFA ? (
              <>
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
                />
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
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 3, mb: 2 }}
                  disabled={loading}
                >
                  {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
                </Button>
              </>
            ) : (
              <>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Se ha generado un código MFA. Revisa la consola del servidor (Auth Service) para ver el código.
                  <br />
                  <strong>Nota:</strong> En producción, el código se enviaría por email/SMS.
                </Alert>
                <TextField
                  fullWidth
                  label="Código MFA (6 dígitos)"
                  variant="outlined"
                  margin="normal"
                  value={mfaToken}
                  onChange={(e) => setMfaToken(e.target.value)}
                  required
                  autoFocus
                  disabled={loading}
                  inputProps={{ maxLength: 6 }}
                  helperText="Ingresa el código de 6 dígitos generado"
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 3, mb: 2 }}
                  disabled={loading || mfaToken.length !== 6}
                >
                  {loading ? 'Verificando...' : 'Verificar Código MFA'}
                </Button>
                <Button
                  fullWidth
                  variant="outlined"
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

