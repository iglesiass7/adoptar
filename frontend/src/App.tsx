import { useEffect, useState } from 'react'
import { API_URL, obtenerSalud, type EstadoComponente, type Salud } from './api/salud'

// Los tres estados posibles de la consulta a la API
type Consulta =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; salud: Salud }
  | { tipo: 'error'; mensaje: string }

// En desarrollo Swagger se abre directo en el backend; en producción, en la URL pública.
const SWAGGER_URL = `${API_URL || 'http://localhost:8080'}/swagger-ui.html`

function App() {
  const [consulta, setConsulta] = useState<Consulta>({ tipo: 'cargando' })
  const [intento, setIntento] = useState(0)

  // Cada vez que cambia "intento" se vuelve a consultar la salud de la API
  useEffect(() => {
    let cancelada = false
    obtenerSalud()
      .then((salud) => {
        if (!cancelada) setConsulta({ tipo: 'ok', salud })
      })
      .catch((error: unknown) => {
        if (!cancelada) {
          const mensaje = error instanceof Error ? error.message : 'Error desconocido'
          setConsulta({ tipo: 'error', mensaje })
        }
      })
    // Si el componente se desmonta antes de que llegue la respuesta, la ignoramos
    return () => {
      cancelada = true
    }
  }, [intento])

  function reintentar() {
    setConsulta({ tipo: 'cargando' })
    setIntento((n) => n + 1)
  }

  return (
    <main className="pagina">
      <header className="encabezado">
        <Huella />
        <div>
          <h1>AdoptAR</h1>
          <p className="subtitulo">Plataforma de gestión de adopción de mascotas</p>
        </div>
      </header>

      <section className="tarjeta" aria-live="polite">
        <h2>Estado del sistema</h2>
        <EstadoSistema consulta={consulta} />
        <div className="acciones">
          <button type="button" onClick={reintentar} disabled={consulta.tipo === 'cargando'}>
            Volver a verificar
          </button>
          <a href={SWAGGER_URL} target="_blank" rel="noreferrer">
            Documentación de la API (Swagger)
          </a>
        </div>
      </section>

      <p className="pie">Iteración 0 · esqueleto del proyecto</p>
    </main>
  )
}

function EstadoSistema({ consulta }: { consulta: Consulta }) {
  if (consulta.tipo === 'cargando') {
    return <p className="mensaje">Consultando la API…</p>
  }
  if (consulta.tipo === 'error') {
    return (
      <ul className="estados">
        <Fila nombre="API" estado="DOWN" detalle={consulta.mensaje} />
      </ul>
    )
  }
  // Si la API respondió, está viva; el estado de la base viene en el componente "db"
  const baseDeDatos = consulta.salud.components?.db?.status ?? 'UNKNOWN'
  return (
    <ul className="estados">
      <Fila nombre="API" estado="UP" />
      <Fila nombre="Base de datos" estado={baseDeDatos} />
    </ul>
  )
}

const ETIQUETAS: Record<EstadoComponente, string> = {
  UP: 'Funcionando',
  DOWN: 'Sin respuesta',
  OUT_OF_SERVICE: 'Fuera de servicio',
  UNKNOWN: 'Desconocido',
}

function Fila(props: { nombre: string; estado: EstadoComponente; detalle?: string }) {
  return (
    <li className="estado">
      <span className={`punto punto-${props.estado.toLowerCase()}`} aria-hidden="true" />
      <span className="nombre">{props.nombre}</span>
      <span className="valor">{ETIQUETAS[props.estado]}</span>
      {props.detalle && <span className="detalle">{props.detalle}</span>}
    </li>
  )
}

function Huella() {
  return (
    <svg className="huella" viewBox="0 0 64 64" width="44" height="44" aria-hidden="true">
      <g fill="currentColor">
        <ellipse cx="32" cy="43" rx="14" ry="12" />
        <ellipse cx="13" cy="27" rx="6" ry="7.5" />
        <ellipse cx="24.5" cy="15" rx="6" ry="7.5" />
        <ellipse cx="39.5" cy="15" rx="6" ry="7.5" />
        <ellipse cx="51" cy="27" rx="6" ry="7.5" />
      </g>
    </svg>
  )
}

export default App
