import process from 'node:process'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Escucha en todas las interfaces para poder entrar desde otros equipos de la red local
    // (http://<IP-del-equipo>:5173). El backend no se expone: Vite le reenvía /api.
    host: true,
    // En desarrollo, las llamadas a /api se envían al backend de Spring Boot.
    // Si el backend corre en otro puerto: BACKEND_URL=http://localhost:8081 npm run dev
    proxy: {
      '/api': {
        target: process.env.BACKEND_URL ?? 'http://localhost:8080',
        // Para el navegador la petición es del mismo origen (va a Vite). Se quita el Origin
        // para que el backend no la trate como CORS y la rechace (403) al entrar por la IP de red.
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'))
        },
      },
    },
  },
})
