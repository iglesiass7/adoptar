# AdoptAR — Frontend

React + Vite + TypeScript. Interfaz mínima que consume la API del backend.

## Comandos

```bash
npm install      # instalar dependencias (la primera vez)
npm run dev      # levantar en http://localhost:5173
npm run lint     # revisar el código con ESLint
npm run build    # compilar para producción (queda en dist/)
```

## Cómo habla con el backend

En desarrollo, todas las llamadas a `/api/...` y `/actuator/...` se redirigen al backend en
`http://localhost:8080` (ver `vite.config.ts`). Por eso el backend tiene que estar levantado
para que la pantalla muestre el estado en verde.

En producción se define `VITE_API_URL` con la URL pública del backend (ver `.env.example`).
