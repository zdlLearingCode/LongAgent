import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/agent-core': {
        target: 'http://localhost:6666',
        changeOrigin: true
      }
    }
  }
})
