import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // En desarrollo, las llamadas a /api y /actuator se redirigen al backend.
    // Así el navegador ve un solo origen y no hace falta configurar CORS todavía.
    proxy: {
      '/api': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080',
    },
  },
})
