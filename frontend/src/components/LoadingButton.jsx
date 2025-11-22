import React from 'react'
import { Button, CircularProgress } from '@mui/material'

/**
 * Botón con estado de carga integrado
 */
function LoadingButton({ loading, children, startIcon, ...props }) {
  return (
    <Button
      {...props}
      disabled={loading || props.disabled}
      startIcon={
        loading ? (
          <CircularProgress 
            size={16} 
            color="inherit" 
            sx={{ 
              '& .MuiCircularProgress-circle': {
                strokeLinecap: 'round',
              },
            }}
          />
        ) : (
          startIcon
        )
      }
      sx={{
        ...props.sx,
        position: 'relative',
        '&:disabled': {
          opacity: loading ? 0.7 : 0.5,
        },
      }}
    >
      {children}
    </Button>
  )
}

export default LoadingButton

