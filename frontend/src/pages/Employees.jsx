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
  IconButton
} from '@mui/material'
import {
  Add as AddIcon,
  Edit as EditIcon,
  Block as BlockIcon,
  CheckCircle as CheckCircleIcon
} from '@mui/icons-material'
import api from '../services/api'

function Employees() {
  const [employees, setEmployees] = useState([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
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
  }

  const handleSubmit = async () => {
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
        <Box>
          <Typography variant="h4" component="h1">
            Gestión de Empleados
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total: {employees.length} empleado{employees.length !== 1 ? 's' : ''}
          </Typography>
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
                <TableCell colSpan={7} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No hay empleados registrados
                  </Typography>
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
                  <TableCell>{employee.status === true || employee.status === 'ACTIVE' ? 'Activo' : employee.status === false || employee.status === 'INACTIVE' ? 'Inactivo' : employee.status || '-'}</TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      onClick={() => handleOpen(employee)}
                      color="primary"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleStatusChange(employee)}
                      color={employee.status === true || employee.status === 'ACTIVE' ? 'warning' : 'success'}
                    >
                      {employee.status === true || employee.status === 'ACTIVE'
                        ? <BlockIcon />
                        : <CheckCircleIcon />
                      }
                    </IconButton>
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
          <TextField
            fullWidth
            label="Documento (Requerido)"
            margin="normal"
            value={formData.document}
            onChange={(e) => setFormData({ ...formData, document: e.target.value })}
            required
            helperText="Número de documento de identidad"
          />
          <TextField
            fullWidth
            label="Código de Empleado"
            margin="normal"
            value={formData.employeeCode}
            onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
          />
          <TextField
            fullWidth
            label="Nombre (Requerido)"
            margin="normal"
            value={formData.firstname}
            onChange={(e) => setFormData({ ...formData, firstname: e.target.value })}
            required
          />
          <TextField
            fullWidth
            label="Apellido (Requerido)"
            margin="normal"
            value={formData.lastname}
            onChange={(e) => setFormData({ ...formData, lastname: e.target.value })}
            required
          />
          <TextField
            fullWidth
            label="Email (Requerido)"
            type="email"
            margin="normal"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            required
          />
          <TextField
            fullWidth
            label="Teléfono"
            margin="normal"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
          />
          <TextField
            fullWidth
            label="Departamento"
            margin="normal"
            value={formData.department}
            onChange={(e) => setFormData({ ...formData, department: e.target.value })}
          />
          <TextField
            fullWidth
            label="Cargo"
            margin="normal"
            value={formData.position}
            onChange={(e) => setFormData({ ...formData, position: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancelar</Button>
          <Button onClick={handleSubmit} variant="contained">
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default Employees

