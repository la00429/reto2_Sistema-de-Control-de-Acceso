import React, { useEffect, useState } from 'react'
import {
  Container,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Box,
  MenuItem,
  Alert,
  Divider,
  FormControl,
  InputLabel,
  Select,
  FormHelperText,
  Autocomplete
} from '@mui/material'
import { 
  Add as AddIcon, 
  Info as InfoIcon, 
  CheckCircle as CheckCircleIcon, 
  Warning as WarningIcon, 
  History as HistoryIcon
} from '@mui/icons-material'
import api from '../services/api'
import HelpIcon from '../components/HelpIcon'
import EmptyState from '../components/EmptyState'
import LoadingButton from '../components/LoadingButton'

function AccessControl() {
  const [accessRecords, setAccessRecords] = useState([])
  const [employees, setEmployees] = useState([])
  const [open, setOpen] = useState(false)
  const [availableLocations, setAvailableLocations] = useState([])
  const [availableDevices, setAvailableDevices] = useState([])
  const [formData, setFormData] = useState({
    employeeId: '',
    employeeCode: '',
    employeeID: '', // documento, requerido por AccessRecordDTO
    accessType: 'ENTRY',
    location: '',
    deviceId: ''
  })

  useEffect(() => {
    fetchAccessRecords()
    fetchEmployees()
  }, [])

  const fetchAccessRecords = async () => {
    try {
      const response = await api.get('/access/history')
      const records = Array.isArray(response.data) ? response.data : []
      setAccessRecords(records)
      
      // Extraer ubicaciones y dispositivos únicos del historial
      const locations = [...new Set(records
        .map(r => r.location)
        .filter(loc => loc && loc.trim() !== '')
      )].sort()
      
      const devices = [...new Set(records
        .map(r => r.deviceId)
        .filter(dev => dev && dev.trim() !== '')
      )].sort()
      
      setAvailableLocations(locations)
      setAvailableDevices(devices)
    } catch (error) {
      console.error('Error fetching access records:', error)
      setAccessRecords([])
      setAvailableLocations([])
      setAvailableDevices([])
    }
  }

  const fetchEmployees = async () => {
    try {
      const response = await api.get('/employee/findallemployees')
      setEmployees(response.data)
    } catch (error) {
      console.error('Error fetching employees:', error)
    }
  }

  const handleOpen = () => {
    setFormData({
      employeeId: '',
      employeeCode: '',
      employeeID: '',
      accessType: 'ENTRY',
      location: '',
      deviceId: ''
    })
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
  }

  const handleEmployeeChange = (employeeId) => {
    const employee = employees.find(emp => emp.id === parseInt(employeeId))
    if (employee) {
      console.log('Empleado seleccionado:', employee)
      setFormData({
        ...formData,
        employeeId: employee.id,
        employeeCode: employee.employeeCode || '',
        // Usar document como employeeID (documento) según AccessRecordDTO
        employeeID: employee.document || ''
      })
      console.log('FormData actualizado:', {
        ...formData,
        employeeId: employee.id,
        employeeCode: employee.employeeCode || '',
        employeeID: employee.document || ''
      })
    } else {
      console.warn('Empleado no encontrado con ID:', employeeId)
    }
  }

  const extractErrorMessage = (error) => {
    if (error.response?.data) {
      return error.response.data.message || error.response.data.error || JSON.stringify(error.response.data)
    }

    if (error.request?.responseText) {
      try {
        const parsed = JSON.parse(error.request.responseText)
        return parsed.message || parsed.error
      } catch (parseError) {
        return error.request.responseText
      }
    }

    return error.message || 'Error desconocido'
  }

  const handleSubmit = async () => {
    if (!formData.employeeID || !formData.employeeId) {
      alert('Por favor selecciona un empleado antes de registrar el acceso')
      return
    }

    // Validar que haya ubicación si es requerida
    if (!formData.location || formData.location.trim() === '') {
      alert('Por favor ingresa una ubicación')
      return
    }

    // Validar que haya deviceId si es requerido
    if (!formData.deviceId || formData.deviceId.trim() === '') {
      alert('Por favor ingresa un ID de dispositivo')
      return
    }

    try {
      console.log('=== INICIO REGISTRO DE ACCESO ===')
      console.log('FormData completo:', formData)
      console.log('Employee ID:', formData.employeeID)
      console.log('Access Type:', formData.accessType)
      console.log('Location:', formData.location)
      console.log('Device ID:', formData.deviceId)
      
      // Validar que employeeID no esté vacío
      if (!formData.employeeID || formData.employeeID.trim() === '') {
        alert('Error: El documento del empleado es requerido. Por favor selecciona un empleado.')
        console.error('employeeID está vacío:', formData.employeeID)
        return
      }
      
      // Validar que todos los campos requeridos estén presentes
      if (!formData.location || !formData.deviceId) {
        alert('Error: Por favor completa todos los campos requeridos (ubicación y dispositivo).')
        return
      }
      
      // Preparar los datos para enviar (definir fuera del try para que esté disponible en catch)
      // Asegurarse de que employeeCode se envíe correctamente (null si está vacío, para que el backend lo obtenga)
      const accessData = {
        employeeID: formData.employeeID.trim(),
        employeeCode: formData.employeeCode && formData.employeeCode.trim() !== '' ? formData.employeeCode.trim() : null,
        accessType: formData.accessType,
        location: formData.location.trim(),
        deviceId: formData.deviceId.trim()
      }
      
      console.log('=== DATOS DE ACCESO ===')
      console.log('Employee ID (documento):', accessData.employeeID)
      console.log('Employee Code:', accessData.employeeCode)
      console.log('Access Type:', accessData.accessType)
      
      const endpoint = formData.accessType === 'ENTRY' ? '/access/usercheckin' : '/access/usercheckout'
      const fullUrl = `${api.defaults.baseURL}${endpoint}`
      
      console.log('=== DATOS A ENVIAR ===')
      console.log('URL completa:', fullUrl)
      console.log('Datos JSON:', JSON.stringify(accessData, null, 2))
      console.log('Configuración API:', {
        baseURL: api.defaults.baseURL,
        timeout: api.defaults.timeout
      })
      
      // Enviar petición con axios
      console.log('=== ENVIANDO PETICIÓN ===')
      
      let response
      try {
        if (formData.accessType === 'ENTRY') {
          response = await api.post('/access/usercheckin', accessData)
        } else {
          response = await api.post('/access/usercheckout', accessData)
        }
      } catch (apiError) {
        // Re-lanzar el error para que sea manejado por el catch externo
        throw apiError
      }
      
      console.log('=== RESPUESTA EXITOSA ===')
      console.log('Status:', response.status)
      console.log('Data:', response.data)
      
      // Mostrar mensaje de éxito (esto solo se ejecuta si no hubo error)
      const accessTypeText = formData.accessType === 'ENTRY' ? 'entrada' : 'salida'
      alert(`Acceso de ${accessTypeText} registrado exitosamente`)
      
      fetchAccessRecords()
      handleClose()
    } catch (error) {
      console.error('=== CAPTURA DE ERROR GENERAL ===')
      console.error('Error completo:', error)
      console.error('Error response:', error.response)
      console.error('Error data:', error.response?.data)
      console.error('Error message:', error.message)
      console.error('Error stack:', error.stack)
      
      const errorMessage = extractErrorMessage(error)
      console.error('Mensaje de error extraído:', errorMessage)
      
      // Mostrar mensaje de error más descriptivo
      if (error.response) {
        // El servidor respondió con un error
        const status = error.response.status
        const data = error.response.data
        
        if (status === 400) {
          // Error de validación o datos incorrectos
          const message = data?.message || data?.error || 'Error de validación: Los datos enviados no son correctos'
          console.error('Error 400 - Detalles completos:', data)
          
          let errorMsg = `Error al registrar el acceso:\n\n${message}`
          
          // Si hay errores de validación específicos
          if (data?.errors) {
            errorMsg += '\n\nErrores de validación:'
            Object.keys(data.errors).forEach(key => {
              errorMsg += `\n- ${key}: ${data.errors[key]}`
            })
          }
          
          // Si hay un código de alerta
          if (data?.alertCode) {
            errorMsg += `\n\nCódigo: ${data.alertCode}`
          }
          
          alert(errorMsg)
        } else if (status === 401) {
          alert('Error de autenticación. Por favor inicia sesión nuevamente.')
        } else {
          alert(`Error del servidor (${status}): ${data?.message || data?.error || 'Error desconocido'}`)
        }
      } else if (error.code === 'ERR_NETWORK' || error.message?.includes('Failed to fetch') || error.message?.includes('Network Error')) {
        let errorMsg = `Error de conexión: No se pudo conectar al servidor.\n\n`
        
        errorMsg += `Por favor verifica:\n`
        errorMsg += `1. Abre la pestaña Network (Red) en las DevTools (F12)\n`
        errorMsg += `2. Busca la petición a /access/usercheckin\n`
        errorMsg += `3. Si ves la petición:\n`
        errorMsg += `   - ¿Qué código de estado tiene? (200, 400, 500, etc.)\n`
        errorMsg += `   - ¿Hay un error rojo de CORS?\n`
        errorMsg += `4. Si NO ves la petición:\n`
        errorMsg += `   - El navegador la está bloqueando\n`
        errorMsg += `   - Reinicia el API Gateway\n`
        errorMsg += `   - Recarga la página (Ctrl + F5)\n\n`
        
        errorMsg += `Datos de la petición:\n`
        errorMsg += `- URL: ${api.defaults.baseURL}/access/usercheckin\n`
        errorMsg += `- Método: POST\n`
        if (typeof accessData !== 'undefined') {
          errorMsg += `- Datos enviados: ${JSON.stringify(accessData)}\n`
        } else {
          errorMsg += `- Datos enviados: (no disponibles)\n`
        }
        
        alert(errorMsg)
      } else {
        alert(`Error al registrar acceso: ${error.message || 'Error desconocido'}\n\nPor favor intenta nuevamente.`)
      }
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleString('es-ES')
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              Control de Acceso de Empleados
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Registra las entradas y salidas de los empleados del sistema
            </Typography>
          </Box>
          <HelpIcon 
            title="En esta sección puedes registrar los accesos de los empleados (entradas y salidas). Un empleado debe tener una entrada registrada antes de poder registrar una salida. Todos los campos son obligatorios."
            placement="right"
          />
        </Box>
        <Button
          variant="contained"
          size="large"
          startIcon={<AddIcon />}
          onClick={handleOpen}
          sx={{ height: 'fit-content' }}
        >
          Registrar Acceso
        </Button>
      </Box>

      {accessRecords.length === 0 ? (
        <Paper sx={{ p: 0, border: 'none', boxShadow: 'none' }}>
          <EmptyState
            icon={<HistoryIcon />}
            title="No hay registros de acceso"
            description="Los registros de acceso aparecerán aquí una vez que comiences a registrar entradas y salidas de empleados."
            action={
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={handleOpen}
                sx={{ mt: 2 }}
                size="large"
              >
                Registrar Primer Acceso
              </Button>
            }
          />
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Box sx={{ p: 2, bgcolor: 'background.default', borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="subtitle1" color="text.secondary">
              Historial de Accesos ({accessRecords.length} registro{accessRecords.length !== 1 ? 's' : ''})
            </Typography>
          </Box>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: 'primary.main' }}>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Documento Empleado</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Código</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Tipo de Acceso</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Fecha y Hora</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Ubicación</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Dispositivo</TableCell>
                <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Estado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {accessRecords.map((record, index) => (
                <TableRow 
                  key={record.id || index}
                  sx={{ 
                    '&:nth-of-type(odd)': { bgcolor: 'action.hover' },
                    '&:hover': { bgcolor: 'action.selected' }
                  }}
                >
                  <TableCell>
                    <Typography variant="body2" fontWeight="medium">
                      {record.employeeID || record.employeeId || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>{record.employeeCode || '-'}</TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      {record.accessType === 'ENTRY' ? (
                        <>
                          <CheckCircleIcon color="success" fontSize="small" />
                          <Typography variant="body2" color="success.main" fontWeight="bold">
                            ENTRADA
                          </Typography>
                        </>
                      ) : (
                        <>
                          <WarningIcon color="warning" fontSize="small" />
                          <Typography variant="body2" color="warning.main" fontWeight="bold">
                            SALIDA
                          </Typography>
                        </>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {formatDate(record.accessTimestamp)}
                    </Typography>
                  </TableCell>
                  <TableCell>{record.location || '-'}</TableCell>
                  <TableCell>{record.deviceId || '-'}</TableCell>
                  <TableCell>
                    <Typography 
                      variant="body2" 
                      sx={{ 
                        color: record.status === 'SUCCESS' ? 'success.main' : 'error.main',
                        fontWeight: 'medium'
                      }}
                    >
                      {record.status || '-'}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogTitle sx={{ pb: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AddIcon color="primary" />
            <Typography variant="h6">Registrar Acceso de Empleado</Typography>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box
            sx={{
              mb: 3,
              p: 2,
              borderRadius: 2,
              background: 'linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(66, 165, 245, 0.05) 100%)',
              border: '1px solid rgba(25, 118, 210, 0.15)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <InfoIcon color="primary" fontSize="small" />
              <Typography variant="subtitle2" sx={{ fontWeight: 600, color: 'primary.main' }}>
                Pasos para Registrar un Acceso
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {[
                { step: 1, text: 'Selecciona el empleado del listado desplegable' },
                { step: 2, text: 'Elige el tipo: Entrada (INGRESO) o Salida (EGRESO)' },
                { step: 3, text: 'Escribe o selecciona la ubicación (puedes escribir directamente o seleccionar una usada anteriormente)' },
                { step: 4, text: 'Escribe o selecciona el dispositivo (puedes escribir directamente o seleccionar uno usado anteriormente)' },
                { step: 5, text: 'Haz clic en "Registrar Acceso"' },
              ].map((item) => (
                <Box key={item.step} sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      minWidth: 24,
                      height: 24,
                      borderRadius: '50%',
                      background: 'primary.main',
                      color: 'white',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      flexShrink: 0,
                    }}
                  >
                    {item.step}
                  </Box>
                  <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6, pt: 0.25 }}>
                    {item.text}
                  </Typography>
                </Box>
              ))}
            </Box>
            <Box
              sx={{
                mt: 2,
                pt: 2,
                borderTop: '1px solid rgba(25, 118, 210, 0.1)',
                display: 'flex',
                gap: 1,
                alignItems: 'flex-start',
              }}
            >
              <WarningIcon sx={{ color: 'warning.main', fontSize: '1.2rem', flexShrink: 0, mt: 0.1 }} />
              <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.6 }}>
                <strong>Tip:</strong> Para ubicación y dispositivo, puedes escribir un valor nuevo o hacer clic en el campo 
                para ver una lista de valores usados previamente.
              </Typography>
            </Box>
          </Box>
          
          <Divider sx={{ my: 2 }} />

          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <FormControl fullWidth margin="normal" required>
              <InputLabel id="employee-select-label">Selecciona el Empleado *</InputLabel>
              <Select
                labelId="employee-select-label"
                id="employee-select"
                value={formData.employeeId || ''}
                label="Selecciona el Empleado *"
                onChange={(e) => handleEmployeeChange(e.target.value)}
                displayEmpty
              >
                {employees.length === 0 ? (
                  <MenuItem value="" disabled>
                    <em>No hay empleados disponibles. Por favor crea un empleado primero.</em>
                  </MenuItem>
                ) : (
                  employees.map((employee) => (
                    <MenuItem key={employee.id} value={employee.id}>
                      <Box>
                        <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                          {(employee.firstname || employee.firstName) || ''} {(employee.lastname || employee.lastName) || ''}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Código: {employee.employeeCode || 'Sin código'} | Documento: {employee.document || 'Sin documento'}
                        </Typography>
                      </Box>
                    </MenuItem>
                  ))
                )}
              </Select>
              <FormHelperText>
                {formData.employeeID 
                  ? `Empleado seleccionado - Documento: ${formData.employeeID}`
                  : 'Debes seleccionar un empleado de la lista desplegable'}
              </FormHelperText>
            </FormControl>
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Selecciona el empleado para el cual vas a registrar el acceso. Solo aparecen empleados activos en el sistema. Si no ves empleados, primero créalos en la sección 'Empleados'."
                placement="right"
              />
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <FormControl fullWidth margin="normal" required>
              <InputLabel id="access-type-label">Tipo de Acceso *</InputLabel>
              <Select
                labelId="access-type-label"
                id="access-type-select"
                value={formData.accessType}
                label="Tipo de Acceso *"
                onChange={(e) => setFormData({ ...formData, accessType: e.target.value })}
              >
                <MenuItem value="ENTRY">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <CheckCircleIcon color="success" fontSize="small" />
                    <Typography>Entrada (INGRESO)</Typography>
                  </Box>
                </MenuItem>
                <MenuItem value="EXIT">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <WarningIcon color="warning" fontSize="small" />
                    <Typography>Salida (EGRESO)</Typography>
                  </Box>
                </MenuItem>
              </Select>
              <FormHelperText>
                {formData.accessType === 'ENTRY' 
                  ? 'Registra el ingreso del empleado al sistema'
                  : 'Registra la salida del empleado del sistema'}
              </FormHelperText>
            </FormControl>
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="ENTRADA: Registra cuando el empleado ingresa al sistema. Un empleado no puede tener dos entradas consecutivas sin una salida previa. SALIDA: Registra cuando el empleado sale del sistema. El empleado debe tener una entrada previa para poder registrar una salida."
                placement="right"
              />
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <Autocomplete
              freeSolo
              options={availableLocations}
              value={formData.location || ''}
              onInputChange={(event, newValue) => {
                setFormData({ ...formData, location: newValue })
              }}
              onChange={(event, newValue) => {
                setFormData({ ...formData, location: newValue || '' })
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Ubicación *"
                  margin="normal"
                  required
                  fullWidth
                  helperText={
                    formData.location 
                      ? (availableLocations.includes(formData.location) 
                          ? "Ubicación del historial - Puedes escribir o seleccionar de la lista" 
                          : "Nueva ubicación - Se agregará al historial")
                      : availableLocations.length > 0
                      ? "Escribe una ubicación nueva o selecciona una de la lista desplegable"
                      : "Escribe la ubicación donde se registra el acceso"
                  }
                  placeholder={availableLocations.length > 0 ? "Escribe o selecciona" : "Ej: Tunja, Bogotá"}
                />
              )}
            />
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Ingresa la ubicación física donde se registra el acceso (ej: 'Oficina Principal', 'Sede Norte', 'Puerta 1'). Puedes escribir una nueva ubicación o seleccionar una del historial. Las ubicaciones usadas anteriormente aparecerán en la lista desplegable."
                placement="right"
              />
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <Autocomplete
              freeSolo
              options={availableDevices}
              value={formData.deviceId || ''}
              onInputChange={(event, newValue) => {
                setFormData({ ...formData, deviceId: newValue })
              }}
              onChange={(event, newValue) => {
                setFormData({ ...formData, deviceId: newValue || '' })
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="ID de Dispositivo *"
                  margin="normal"
                  required
                  fullWidth
                  helperText={
                    formData.deviceId 
                      ? (availableDevices.includes(formData.deviceId) 
                          ? "Dispositivo del historial - Puedes escribir o seleccionar de la lista" 
                          : "Nuevo dispositivo - Se agregará al historial")
                      : availableDevices.length > 0
                      ? "Escribe un dispositivo nuevo o selecciona uno de la lista desplegable"
                      : "Escribe el ID del dispositivo que registra el acceso"
                  }
                  placeholder={availableDevices.length > 0 ? "Escribe o selecciona" : "Ej: 1, Puerta 1"}
                />
              )}
            />
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Ingresa el identificador del dispositivo que registra el acceso (ej: 'Terminal 1', 'Lector RFID-001', 'Puerta Principal'). Puedes escribir un nuevo ID o seleccionar uno del historial. Los dispositivos usados anteriormente aparecerán en la lista desplegable."
                placement="right"
              />
            </Box>
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2, gap: 1 }}>
          <Button 
            onClick={handleClose} 
            variant="outlined"
            size="large"
          >
            Cancelar
          </Button>
          <LoadingButton
            onClick={handleSubmit} 
            variant="contained" 
            size="large"
            disabled={!formData.employeeId || !formData.location || !formData.deviceId}
            startIcon={<CheckCircleIcon />}
            loading={false}
          >
            Registrar Acceso
          </LoadingButton>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default AccessControl

