# AdoptAR

Plataforma de gestión de adopción de mascotas: conecta refugios con personas que quieren adoptar
y gestiona el proceso completo, desde la publicación de una mascota hasta la entrega.

Proyecto de **Diseño de Sistemas — UTN FRBA**.

![CI](https://github.com/iglesiass7/adoptar/actions/workflows/ci.yml/badge.svg)

## Stack

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21 · Spring Boot 4.1 · Spring Data JPA · Spring Security |
| Base de datos | PostgreSQL 17 · Flyway (migraciones versionadas) |
| Frontend | React 19 · Vite · TypeScript |
| Documentación de la API | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5 · Testcontainers |
| Infraestructura | Docker Compose · GitHub Actions |

## Estructura del repositorio

```
adoptar/
├── backend/                  API REST (Spring Boot)
│   └── src/main/
│       ├── java/com/adoptar/
│       │   ├── config/           seguridad, Swagger, auditoría JPA
│       │   ├── shared/           clase base de entidades y manejo de errores
│       │   ├── usuarios/         M1 · registro, login, roles
│       │   ├── mascotas/         M2 · catálogo y búsqueda
│       │   ├── solicitudes/      M3 · el núcleo: estados y reglas de negocio
│       │   ├── refugios/         M4 · perfil y verificación
│       │   └── notificaciones/   M5 · avisos de cambios de estado
│       └── resources/
│           ├── application.yml
│           └── db/
│               ├── migration/    esquema de la base (Flyway)
│               └── demo/         datos de prueba (solo en desarrollo)
├── frontend/                 interfaz web (React + Vite)
├── docs/                     propuesta y diagramas
├── docker-compose.yml        base de datos para desarrollo
└── .env.example              configuración local (copiar como .env)
```

Los paquetes se organizan **por módulo**, no por capa: así cada integrante trabaja en su carpeta
y hay menos conflictos al mergear. Dentro de cada módulo se respetan las capas
**Controller → Service → Repository → Dominio**.

## Qué hay que tener instalado

- **JDK 21**
- **Docker Desktop** (para la base de datos y para los tests)
- **Node.js 22** o superior
- **IntelliJ IDEA** (recomendado)

Maven no hace falta: el proyecto trae el *wrapper* (`mvnw`), que lo descarga solo la primera vez.

## Cómo levantarlo

**1. La base de datos** (desde la raíz del repo, con Docker Desktop abierto)

```bash
cp .env.example .env          # en CMD de Windows: copy .env.example .env
docker compose up -d
```

**2. El backend**

```bash
cd backend
./mvnw spring-boot:run        # en PowerShell o CMD: .\mvnw.cmd spring-boot:run
```

O desde IntelliJ: abrir `AdoptarApplication` y darle Run. Al arrancar, Flyway crea las tablas solo.

**3. El frontend** (en otra terminal)

```bash
cd frontend
npm install                   # solo la primera vez
npm run dev
```

| Qué | URL |
| --- | --- |
| Frontend | http://localhost:5173 |
| Estado de la API | http://localhost:8080/actuator/health |
| Documentación de la API (Swagger) | http://localhost:8080/swagger-ui.html |

Si la pantalla del frontend muestra la API y la base en verde, está todo funcionando.

## Datos de prueba

Al correr en tu compu (perfil `dev`) se cargan datos de ejemplo. Todos los usuarios tienen la
contraseña `adoptar123`.

| Usuario | Para qué sirve |
| --- | --- |
| `admin@adoptar.test` | Administrador |
| `refugio.sur@adoptar.test` | Refugio verificado, con 4 mascotas y su cuestionario |
| `patitas.oeste@adoptar.test` | Refugio pendiente de verificación (no puede publicar) |
| `ana@adoptar.test` | Adoptante con perfil completo y una solicitud enviada por Lola |
| `bruno@adoptar.test` | Adoptante sin perfil completo (todavía no puede solicitar) |

En producción (perfil `prod`) no se cargan.

## Tests

```bash
cd backend
./mvnw verify                 # en PowerShell o CMD: .\mvnw.cmd verify
```

Los tests levantan un PostgreSQL real con Testcontainers: **Docker Desktop tiene que estar abierto**.

## Cómo trabajamos

- `main` está protegida: todo entra por pull request, con 1 aprobación y la CI en verde.
- Una rama corta por tarea: `feat/solicitud-aprobar`, `fix/filtro-provincia`, `docs/readme`.
- Commits con formato: `feat(solicitudes): aprobar solicitud y rechazar competidoras`.
- Cualquier cambio en las tablas es una **migración Flyway nueva** en
  `backend/src/main/resources/db/migration` (`V2__...`, `V3__...`).
  Una migración que ya está en `main` no se edita nunca.

## Problemas comunes

| Síntoma | Solución |
| --- | --- |
| `docker compose up` falla porque el puerto 5432 está ocupado | Ya tenés un PostgreSQL instalado. Cambiá `DB_PORT` en `.env` (por ejemplo a `5433`) y volvé a levantar |
| El backend no arranca: `Connection refused` | La base no está levantada: `docker compose up -d` |
| Los tests fallan con `Could not find a valid Docker environment` | Abrí Docker Desktop |
| `./mvnw: Permission denied` (Mac / Linux) | `chmod +x backend/mvnw` |

## Equipo

| Integrante | GitHub | Módulo |
| --- | --- | --- |
| Mateo Iglesias | [@iglesiass7](https://github.com/iglesiass7) | |
| | | |
| | | |
| | | |
