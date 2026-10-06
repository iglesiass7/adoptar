// Tipos de las variables de entorno que usa el frontend.
// Vite solo expone al navegador las variables que empiezan con VITE_.
interface ImportMetaEnv {
  readonly VITE_API_URL?: string
}
