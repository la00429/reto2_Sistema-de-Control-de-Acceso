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
import { Search as SearchIcon } from '@mui/icons-material'
import api from '../services/api'

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
      <Typography variant="h4" component="h1" gutterBottom>
        Reportes de Acceso
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          <TextField
            label="Código de Empleado"
            value={employeeCode}
            onChange={(e) => setEmployeeCode(e.target.value)}
            size="small"
          />
          <TextField
            label="Fecha Inicio"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <TextField
            label="Fecha Fin"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
          />
          <Button
            variant="contained"
            startIcon={<SearchIcon />}
            onClick={handleSearch}
          >
            Buscar
          </Button>
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
                <TableCell colSpan={5} align="center">
                  No hay registros para mostrar
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

