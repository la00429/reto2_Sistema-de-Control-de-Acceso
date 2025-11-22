import React from 'react'
import { Paper, Skeleton, Box } from '@mui/material'

/**
 * Skeleton loader para tarjetas del dashboard
 */
function SkeletonCard() {
  return (
    <Paper
      elevation={3}
      sx={{
        p: 3,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        borderRadius: 3,
      }}
    >
      <Skeleton variant="circular" width={64} height={64} sx={{ mb: 2 }} />
      <Skeleton variant="text" width={80} height={60} sx={{ mb: 1 }} />
      <Skeleton variant="text" width={120} height={32} />
    </Paper>
  )
}

export default SkeletonCard

