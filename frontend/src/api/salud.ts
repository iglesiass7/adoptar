// URL base de la API.
// - En desarrollo queda vacía: Vite redirige /api y /actuator al backend (ver vite.config.ts).
// - En producción se define VITE_API_URL con la URL pública del backend.
export const API_URL = import.meta.env.VITE_API_URL ?? ''

export type EstadoComponente = 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN'

// Forma de la respuesta de GET /actuator/health
export interface Salud {
  status: EstadoComponente
  components?: Record<string, { status: EstadoComponente }>
}

export async function obtenerSalud(): Promise<Salud> {
  let respuesta: Response
  try {
    respuesta = await fetch(`${API_URL}/actuator/health`)
  } catch {
    throw new Error('No se pudo conectar con la API.')
  }

  // Actuator responde 503 cuando algún componente (por ejemplo la base) está caído,
  // pero igual manda el detalle en el cuerpo. Cualquier otro error es que no llegamos.
  if (!respuesta.ok && respuesta.status !== 503) {
    throw new Error(
      `La API no respondió (código ${respuesta.status}). ¿Está levantado el backend en el puerto 8080?`,
    )
  }
  return (await respuesta.json()) as Salud
}
