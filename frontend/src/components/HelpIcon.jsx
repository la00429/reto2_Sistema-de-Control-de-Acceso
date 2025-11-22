import React from 'react'
import { Tooltip, IconButton } from '@mui/material'
import { HelpOutline as HelpIcon } from '@mui/icons-material'

/**
 * Componente reutilizable para mostrar iconos de ayuda con tooltip
 * @param {string} title - Texto del tooltip
 * @param {string} placement - Posición del tooltip (top, bottom, left, right)
 * @param {object} sx - Estilos adicionales
 */
function HelpIconComponent({ title, placement = 'top', sx = {} }) {
  return (
    <Tooltip
      title={title}
      placement={placement}
      arrow
      enterDelay={300}
      leaveDelay={200}
    >
      <IconButton
        size="small"
        sx={{
          color: 'primary.main',
          background: 'linear-gradient(135deg, rgba(25, 118, 210, 0.08) 0%, rgba(66, 165, 245, 0.08) 100%)',
          border: '1px solid rgba(25, 118, 210, 0.2)',
          '&:hover': {
            color: 'primary.dark',
            backgroundColor: 'rgba(25, 118, 210, 0.12)',
            borderColor: 'rgba(25, 118, 210, 0.4)',
            transform: 'scale(1.15) rotate(5deg)',
            boxShadow: '0 4px 8px rgba(25, 118, 210, 0.2)',
          },
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          padding: '6px',
          borderRadius: '50%',
          ...sx
        }}
      >
        <HelpIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  )
}

export default HelpIconComponent

