## Qué cambia

<!-- Una o dos líneas. Si resuelve un issue, escribí "Cierra #12" -->

## Cómo probarlo

<!-- Pasos para verificarlo: endpoint y request de ejemplo, o pantalla del front -->

## Checklist de quien abre el PR

- [ ] Los tests pasan (`./mvnw verify` en backend, `npm run build` en frontend)
- [ ] Si toqué el esquema, agregué una migración Flyway **nueva** (no edité una existente)
- [ ] Los endpoints nuevos aparecen en Swagger
- [ ] Cada regla de negocio nueva tiene su test

## Checklist de quien revisa

- [ ] No hay reglas de negocio en los controllers
- [ ] Los services devuelven DTOs, no entidades
- [ ] No hay un `catch` que se trague una excepción sin hacer nada
- [ ] Los nombres son los del dominio (`aprobar`, no `updateStatus`)
- [ ] Se entiende sin preguntarle al autor
