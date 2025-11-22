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
  IconButton,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  FormHelperText,
  Tooltip,
  Chip
} from '@mui/material'
import {
  Add as AddIcon,
  Edit as EditIcon,
  Block as BlockIcon,
  CheckCircle as CheckCircleIcon,
  People as PeopleIcon
} from '@mui/icons-material'
import api from '../services/api'
import HelpIcon from '../components/HelpIcon'
import EmptyState from '../components/EmptyState'
import LoadingButton from '../components/LoadingButton'

function Employees() {
  const [employees, setEmployees] = useState([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [formErrors, setFormErrors] = useState({})
  const [formData, setFormData] = useState({
    document: '',
    employeeCode: '',
    firstname: '',
    lastname: '',
    email: '',
    phone: '',
    department: '',
    position: '',
    status: true  // Boolean: true = activo
  })

  // Opciones predefinidas para departamentos y cargos
  const departments = [
    'Administración',
    'Recursos Humanos',
    'Tecnología',
    'Ventas',
    'Marketing',
    'Contabilidad',
    'Producción',
    'Logística',
    'Calidad',
    'Seguridad',
    'Mantenimiento',
    'Atención al Cliente',
    'Gerencia',
    'Operaciones',
    'Otro'
  ]

  const positions = [
    'Director',
    'Gerente',
    'Supervisor',
    'Coordinador',
    'Analista',
    'Asistente',
    'Especialista',
    'Técnico',
    'Operador',
    'Auxiliar',
    'Ejecutivo',
    'Consultor',
    'Líder de Proyecto',
    'Desarrollador',
    'Diseñador',
    'Otro'
  ]

  useEffect(() => {
    fetchEmployees()
  }, [])

  const fetchEmployees = async () => {
    try {
      console.log('Obteniendo empleados...')
      console.log('Base URL:', api.defaults.baseURL)
      console.log('URL completa:', `${api.defaults.baseURL}/employee/findallemployees`)
      
      const response = await api.get('/employee/findallemployees')
      console.log('Respuesta completa:', response)
      console.log('Response data:', response.data)
      console.log('Tipo de response.data:', typeof response.data)
      console.log('Es un array?', Array.isArray(response.data))
      
      // Asegurarse de que siempre sea un array
      const employeesList = Array.isArray(response.data) ? response.data : []
      console.log('Empleados obtenidos:', employeesList.length)
      setEmployees(employeesList)
    } catch (error) {
      console.error('Error fetching employees:', error)
      console.error('Error code:', error.code)
      console.error('Error message:', error.message)
      console.error('Error response:', error.response)
      console.error('Error config:', error.config)
      
      if (error.code === 'ERR_NETWORK') {
        console.error('ERROR DE RED: El API Gateway no está respondiendo.')
        console.error('Por favor verifica que:')
        console.error('1. El API Gateway esté corriendo en el puerto 8080')
        console.error('2. No haya problemas de CORS')
        console.error('3. La URL del API sea correcta:', api.defaults.baseURL)
        console.error('4. El servicio employee-service esté corriendo en el puerto 8082')
      }
      
      setEmployees([])
    }
  }

  const handleOpen = (employee = null) => {
    if (employee) {
      setEditing(employee.id)
      setFormData({
        document: employee.document || '',
        employeeCode: employee.employeeCode || '',
        firstname: employee.firstname || employee.firstName || '',
        lastname: employee.lastname || employee.lastName || '',
        email: employee.email || '',
        phone: employee.phone || '',
        department: employee.department || '',
        position: employee.position || '',
        status: employee.status === true || employee.status === 'ACTIVE' || employee.status === 'true'
      })
    } else {
      setEditing(null)
      setFormData({
        document: '',
        employeeCode: '',
        firstname: '',
        lastname: '',
        email: '',
        phone: '',
        department: '',
        position: '',
        status: true  // Boolean: true = activo
      })
    }
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    setEditing(null)
    setFormErrors({})
    setFormData({
      document: '',
      employeeCode: '',
      firstname: '',
      lastname: '',
      email: '',
      phone: '',
      department: '',
      position: '',
      status: true
    })
  }

  const validateForm = () => {
    const errors = {}
    
    // Validar documento (debe ser numérico y tener entre 7 y 15 dígitos)
    if (!formData.document || formData.document.trim() === '') {
      errors.document = 'El documento es requerido'
    } else if (!/^\d{7,15}$/.test(formData.document.trim())) {
      errors.document = 'El documento debe contener solo números y tener entre 7 y 15 dígitos'
    }
    
    // Validar email
    if (!formData.email || formData.email.trim() === '') {
      errors.email = 'El email es requerido'
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(formData.email.trim())) {
        errors.email = 'El email no tiene un formato válido'
      }
    }
    
    // Validar teléfono (si se proporciona, debe ser válido)
    if (formData.phone && formData.phone.trim() !== '') {
      // Permitir números, espacios, guiones, paréntesis y +
      const phoneRegex = /^[\d\s\-\+\(\)]{7,15}$/
      if (!phoneRegex.test(formData.phone.trim())) {
        errors.phone = 'El teléfono debe contener entre 7 y 15 caracteres y solo números, espacios, guiones o paréntesis'
      }
    }
    
    // Validar nombre y apellido
    if (!formData.firstname || formData.firstname.trim() === '') {
      errors.firstname = 'El nombre es requerido'
    }
    
    if (!formData.lastname || formData.lastname.trim() === '') {
      errors.lastname = 'El apellido es requerido'
    }
    
    setFormErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async () => {
    // Validar formulario antes de enviar
    if (!validateForm()) {
      alert('Por favor corrige los errores en el formulario antes de continuar.')
      return
    }

    try {
      console.log('Enviando datos del empleado:', formData)
      
      if (editing) {
        const response = await api.put(`/employee/updateemployee/${editing}`, formData)
        console.log('Empleado actualizado:', response.data)
      } else {
        const response = await api.post('/employee/createemployee', formData)
        console.log('Empleado creado:', response.data)
      }
      
      fetchEmployees()
      handleClose()
      
      // Disparar evento personalizado para notificar al dashboard
      window.dispatchEvent(new CustomEvent('employeeUpdated'))
    } catch (error) {
      console.error('Error saving employee:', error)
      console.error('Error response:', error.response?.data)
      
      // Mostrar mensaje de error más descriptivo
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error ||
                          (error.response?.data?.errors ? JSON.stringify(error.response.data.errors) : null) ||
                          'Error al guardar empleado'
      
      alert(errorMessage)
    }
  }

  const handleStatusChange = async (employee) => {
    const isActive = employee.status === true || employee.status === 'ACTIVE' || employee.status === 'true'
    const actionText = isActive ? 'inactivar' : 'activar'
    
    if (!window.confirm(`¿Deseas ${actionText} al empleado ${employee.firstname || employee.firstName}?`)) {
      return
    }

    try {
      if (isActive) {
        await api.patch(`/employee/disableemployee/${employee.id}`)
      } else {
        await api.patch(`/employee/enableemployee/${employee.id}`)
      }
      fetchEmployees()
      
      // Disparar evento personalizado para notificar al dashboard
      window.dispatchEvent(new CustomEvent('employeeUpdated'))
    } catch (error) {
      console.error('Error updating status:', error)
      const message = error.response?.data?.message || 'No fue posible actualizar el estado del empleado'
      alert(message)
    }
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Box>
            <Typography variant="h4" component="h1">
              Gestión de Empleados
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Total: {employees.length} empleado{employees.length !== 1 ? 's' : ''}
            </Typography>
          </Box>
          <HelpIcon 
            title="En esta sección puedes gestionar todos los empleados del sistema: crear nuevos empleados, editar información existente y activar o desactivar empleados. Los campos marcados con * son obligatorios."
            placement="right"
          />
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpen()}
        >
          Nuevo Empleado
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Documento</TableCell>
              <TableCell>Código</TableCell>
              <TableCell>Nombre</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Departamento</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {employees.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} sx={{ p: 0, border: 'none' }}>
                  <EmptyState
                    icon={<PeopleIcon />}
                    title="No hay empleados registrados"
                    description="Comienza agregando el primer empleado al sistema haciendo clic en el botón 'Nuevo Empleado'."
                    action={
                      <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => handleOpen()}
                        sx={{ mt: 2 }}
                      >
                        Crear Primer Empleado
                      </Button>
                    }
                  />
                </TableCell>
              </TableRow>
            ) : (
              employees.map((employee, index) => (
                <TableRow key={employee.id || employee.document || `employee-${index}`}>
                  <TableCell>{employee.document || '-'}</TableCell>
                  <TableCell>{employee.employeeCode || '-'}</TableCell>
                  <TableCell>{`${employee.firstname || employee.firstName || ''} ${employee.lastname || employee.lastName || ''}`}</TableCell>
                  <TableCell>{employee.email || '-'}</TableCell>
                  <TableCell>{employee.department || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={employee.status === true || employee.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}
                      color={employee.status === true || employee.status === 'ACTIVE' ? 'success' : 'default'}
                      size="small"
                      sx={{
                        fontWeight: 500,
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.5 }}>
                      <Tooltip title="Editar empleado" arrow>
                        <IconButton
                          size="small"
                          onClick={() => handleOpen(employee)}
                          color="primary"
                          sx={{
                            '&:hover': {
                              backgroundColor: 'primary.light',
                              color: 'white',
                              transform: 'scale(1.1)',
                            },
                            transition: 'all 0.2s ease-in-out',
                          }}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip 
                        title={employee.status === true || employee.status === 'ACTIVE' ? 'Desactivar empleado' : 'Activar empleado'} 
                        arrow
                      >
                        <IconButton
                          size="small"
                          onClick={() => handleStatusChange(employee)}
                          color={employee.status === true || employee.status === 'ACTIVE' ? 'warning' : 'success'}
                          sx={{
                            '&:hover': {
                              transform: 'scale(1.1)',
                            },
                            transition: 'all 0.2s ease-in-out',
                          }}
                        >
                          {employee.status === true || employee.status === 'ACTIVE'
                            ? <BlockIcon fontSize="small" />
                            : <CheckCircleIcon fontSize="small" />
                          }
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editing ? 'Editar Empleado' : 'Nuevo Empleado'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <TextField
              fullWidth
              label="Documento (Requerido)"
              margin="normal"
              value={formData.document}
              onChange={(e) => {
                setFormData({ ...formData, document: e.target.value })
                if (formErrors.document) setFormErrors({ ...formErrors, document: '' })
              }}
              error={!!formErrors.document}
              helperText={formErrors.document || 'Número de documento de identidad (solo números, 7-15 dígitos)'}
              required
              inputProps={{ pattern: '[0-9]*', inputMode: 'numeric' }}
            />
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Ingresa el número de documento de identidad del empleado. Debe contener solo números y tener entre 7 y 15 dígitos. Este campo es único y obligatorio."
                placement="right"
              />
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <TextField
              fullWidth
              label="Código de Empleado"
              margin="normal"
              value={formData.employeeCode}
              onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
              helperText="Código único de identificación del empleado (opcional)"
            />
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Código interno de identificación del empleado. Este campo es opcional y puede ser usado para búsquedas y reportes. Si no se proporciona, el sistema puede generar uno automáticamente."
                placement="right"
              />
            </Box>
          </Box>
          <TextField
            fullWidth
            label="Nombre (Requerido)"
            margin="normal"
            value={formData.firstname}
            onChange={(e) => {
              setFormData({ ...formData, firstname: e.target.value })
              if (formErrors.firstname) setFormErrors({ ...formErrors, firstname: '' })
            }}
            error={!!formErrors.firstname}
            helperText={formErrors.firstname}
            required
          />
          <TextField
            fullWidth
            label="Apellido (Requerido)"
            margin="normal"
            value={formData.lastname}
            onChange={(e) => {
              setFormData({ ...formData, lastname: e.target.value })
              if (formErrors.lastname) setFormErrors({ ...formErrors, lastname: '' })
            }}
            error={!!formErrors.lastname}
            helperText={formErrors.lastname}
            required
          />
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <TextField
              fullWidth
              label="Email (Requerido)"
              type="email"
              margin="normal"
              value={formData.email}
              onChange={(e) => {
                setFormData({ ...formData, email: e.target.value })
                if (formErrors.email) setFormErrors({ ...formErrors, email: '' })
              }}
              error={!!formErrors.email}
              helperText={formErrors.email || 'Correo electrónico válido (ejemplo@dominio.com)'}
              required
            />
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Correo electrónico del empleado. Debe tener un formato válido (ejemplo@dominio.com). Este campo es obligatorio y se usará para comunicaciones y notificaciones del sistema."
                placement="right"
              />
            </Box>
          </Box>
          <TextField
            fullWidth
            label="Teléfono"
            margin="normal"
            value={formData.phone}
            onChange={(e) => {
              setFormData({ ...formData, phone: e.target.value })
              if (formErrors.phone) setFormErrors({ ...formErrors, phone: '' })
            }}
            error={!!formErrors.phone}
            helperText={formErrors.phone || 'Número de teléfono (opcional)'}
            inputProps={{ maxLength: 15 }}
          />
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Departamento</InputLabel>
              <Select
                value={formData.department}
                label="Departamento"
                onChange={(e) => setFormData({ ...formData, department: e.target.value })}
              >
                <MenuItem value="">
                  <em>Seleccione un departamento</em>
                </MenuItem>
                {departments.map((dept) => (
                  <MenuItem key={dept} value={dept}>
                    {dept}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>Seleccione el departamento del empleado</FormHelperText>
            </FormControl>
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Selecciona el departamento al que pertenece el empleado. Esta información se utiliza para organización y reportes. Si el departamento no está en la lista, selecciona 'Otro'."
                placement="right"
              />
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Cargo</InputLabel>
              <Select
                value={formData.position}
                label="Cargo"
                onChange={(e) => setFormData({ ...formData, position: e.target.value })}
              >
                <MenuItem value="">
                  <em>Seleccione un cargo</em>
                </MenuItem>
                {positions.map((pos) => (
                  <MenuItem key={pos} value={pos}>
                    {pos}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>Seleccione el cargo o posición del empleado</FormHelperText>
            </FormControl>
            <Box sx={{ mt: 3 }}>
              <HelpIcon 
                title="Selecciona el cargo o posición del empleado dentro de la organización. Esta información ayuda a identificar el rol y responsabilidades del empleado. Si el cargo no está en la lista, selecciona 'Otro'."
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
            loading={false}
          >
            {editing ? 'Actualizar' : 'Crear'} Empleado
          </LoadingButton>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default Employees

