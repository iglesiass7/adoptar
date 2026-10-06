# Documentación

| Documento | Qué tiene |
| --- | --- |
| [propuesta.pdf](propuesta.pdf) | Propuesta presentada a la cátedra: alcance, stack, arquitectura y plan de trabajo |

Pendiente para la entrega:

- [ ] Diagrama de casos de uso
- [ ] Diagrama de clases del dominio
- [ ] Informe con las decisiones de diseño

## Modelo de datos

El esquema real (columnas, restricciones e índices) está en
[`V1__esquema_inicial.sql`](../backend/src/main/resources/db/migration/V1__esquema_inicial.sql).

```mermaid
erDiagram
    USUARIOS ||--o| ADOPTANTES : "perfil"
    USUARIOS ||--o| REFUGIOS : "perfil"
    USUARIOS ||--o{ NOTIFICACIONES : recibe
    REFUGIOS ||--o{ MASCOTAS : publica
    REFUGIOS ||--o{ PREGUNTAS : define
    MASCOTAS ||--o{ FOTOS_MASCOTA : tiene
    MASCOTAS ||--o{ SOLICITUDES : recibe
    ADOPTANTES ||--o{ SOLICITUDES : envia
    SOLICITUDES ||--o{ RESPUESTAS : contiene
    PREGUNTAS ||--o{ RESPUESTAS : "se responde en"
    SOLICITUDES ||--o| ENTREVISTAS : agenda
    SOLICITUDES ||--o| ADOPCIONES : concreta
```

## Ciclo de vida de una solicitud

```mermaid
stateDiagram-v2
    [*] --> ENVIADA
    ENVIADA --> EN_REVISION: el refugio la toma
    ENVIADA --> EXPIRADA: 30 días sin revisar
    ENVIADA --> CANCELADA: el adoptante desiste
    EN_REVISION --> ENTREVISTA: se agenda un encuentro
    EN_REVISION --> APROBADA: sin entrevista
    EN_REVISION --> RECHAZADA: no cumple
    EN_REVISION --> CANCELADA: el adoptante desiste
    ENTREVISTA --> APROBADA: salió bien
    ENTREVISTA --> RECHAZADA: salió mal
    ENTREVISTA --> CANCELADA: el adoptante desiste
    APROBADA --> COMPLETADA: se entregó
    APROBADA --> CANCELADA: se cayó la adopción
    COMPLETADA --> [*]
    RECHAZADA --> [*]
    CANCELADA --> [*]
    EXPIRADA --> [*]
```

## Ciclo de vida de una mascota

```mermaid
stateDiagram-v2
    [*] --> DISPONIBLE: se publica
    DISPONIBLE --> RESERVADA: se aprueba una solicitud
    RESERVADA --> ADOPTADA: se registra la entrega
    RESERVADA --> DISPONIBLE: se cayó la adopción
    DISPONIBLE --> NO_DISPONIBLE: baja del refugio
    RESERVADA --> NO_DISPONIBLE: baja del refugio
    ADOPTADA --> [*]
```
