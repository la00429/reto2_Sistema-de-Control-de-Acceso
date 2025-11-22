import React, { useState } from 'react'
import {
  Container,
  Paper,
  Typography,
  Box,
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow
} from '@mui/material'
import { Search as SearchIcon, Assessment as AssessmentIcon } from '@mui/icons-material'
import api from '../services/api'
import HelpIcon from '../components/HelpIcon'
import EmptyState from '../components/EmptyState'
import LoadingButton from '../components/LoadingButton'

function Reports() {
  const [employeeCode, setEmployeeCode] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [accessRecords, setAccessRecords] = useState([])

  const handleSearch = async () => {
    try {
      const params = {}
      if (employeeCode) params.employeeCode = employeeCode
      if (startDate) params.startDate = `${startDate}T00:00:00`
      if (endDate) params.endDate = `${endDate}T23:59:59`

      const response = await api.get('/access/history', { params })
      setAccessRecords(response.data)
    } catch (error) {
      console.error('Error fetching reports:', error)
      alert('Error al generar reporte')
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleString('es-ES')
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
        <Typography variant="h4" component="h1">
          Reportes de Acceso
        </Typography>
        <HelpIcon 
          title="En esta sección puedes generar reportes de acceso filtrando por código de empleado y rango de fechas. Puedes usar todos los filtros juntos o solo algunos. Si no especificas filtros, se mostrarán todos los registros."
          placement="right"
        />
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <TextField
              label="Código de Empleado"
              value={employeeCode}
              onChange={(e) => setEmployeeCode(e.target.value)}
              size="small"
              helperText="Filtra por código de empleado (opcional)"
            />
            <HelpIcon 
              title="Ingresa el código del empleado para filtrar sus accesos. Si dejas este campo vacío, se mostrarán accesos de todos los empleados."
              placement="right"
            />
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <TextField
              label="Fecha Inicio"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
              size="small"
              helperText="Fecha inicial del rango (opcional)"
            />
            <HelpIcon 
              title="Selecciona la fecha de inicio del rango de búsqueda. Solo se mostrarán registros desde esta fecha en adelante. Si no especificas una fecha, se buscarán desde el inicio de los registros."
              placement="right"
            />
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <TextField
              label="Fecha Fin"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
              size="small"
              helperText="Fecha final del rango (opcional)"
            />
            <HelpIcon 
              title="Selecciona la fecha de fin del rango de búsqueda. Solo se mostrarán registros hasta esta fecha. Si no especificas una fecha, se buscarán hasta la fecha actual. La fecha fin debe ser posterior o igual a la fecha inicio."
              placement="right"
            />
          </Box>
          <LoadingButton
            variant="contained"
            startIcon={<SearchIcon />}
            onClick={handleSearch}
            loading={false}
            sx={{ height: '40px', minWidth: '120px' }}
          >
            Buscar
          </LoadingButton>
        </Box>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Código Empleado</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Fecha y Hora</TableCell>
              <TableCell>Ubicación</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {accessRecords.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} sx={{ p: 0, border: 'none' }}>
                  <EmptyState
                    icon={<AssessmentIcon />}
                    title="No hay registros para mostrar"
                    description="Utiliza los filtros arriba para buscar registros de acceso. Puedes filtrar por código de empleado, fecha de inicio y fecha de fin."
                  />
                </TableCell>
              </TableRow>
            ) : (
              accessRecords.map((record) => (
                <TableRow key={record.id}>
                  <TableCell>{record.employeeCode}</TableCell>
                  <TableCell>{record.accessType}</TableCell>
                  <TableCell>{formatDate(record.accessTimestamp)}</TableCell>
                  <TableCell>{record.location || '-'}</TableCell>
                  <TableCell>{record.status}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Container>
  )
}

export default Reports

