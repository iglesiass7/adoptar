-- =====================================================================
-- Datos de prueba para DESARROLLO
-- =====================================================================
-- * Solo se cargan con el perfil "dev" (ver application-dev.yml). En producción no.
-- * Es una migración "repetible" (R__): Flyway la vuelve a correr cada vez que
--   este archivo cambia. Por eso empieza vaciando las tablas: si lo modifican,
--   al reiniciar la app los datos de prueba se recargan desde cero.
-- * Contraseña de todos los usuarios: adoptar123 (hash BCrypt)
-- =====================================================================

TRUNCATE usuarios, adoptantes, refugios, mascotas, fotos_mascota, preguntas,
         solicitudes, respuestas, entrevistas, adopciones, notificaciones
    RESTART IDENTITY CASCADE;


-- Usuarios ------------------------------------------------------------
INSERT INTO usuarios (email, password_hash, nombre, telefono, rol) VALUES
    ('admin@adoptar.test',         '$2b$10$p5BzSmRf7mY7aSN1y1Fn9OdJeNutM/17R/0Y9IT6LFDnwH6X2nzlO', 'Administración AdoptAR', NULL,           'ADMIN'),
    ('refugio.sur@adoptar.test',   '$2b$10$p5BzSmRf7mY7aSN1y1Fn9OdJeNutM/17R/0Y9IT6LFDnwH6X2nzlO', 'Refugio Sur',            '11 4000-1000', 'REFUGIO'),
    ('patitas.oeste@adoptar.test', '$2b$10$p5BzSmRf7mY7aSN1y1Fn9OdJeNutM/17R/0Y9IT6LFDnwH6X2nzlO', 'Patitas del Oeste',      '11 4000-2000', 'REFUGIO'),
    ('ana@adoptar.test',           '$2b$10$p5BzSmRf7mY7aSN1y1Fn9OdJeNutM/17R/0Y9IT6LFDnwH6X2nzlO', 'Ana Gómez',              '11 5000-1000', 'ADOPTANTE'),
    ('bruno@adoptar.test',         '$2b$10$p5BzSmRf7mY7aSN1y1Fn9OdJeNutM/17R/0Y9IT6LFDnwH6X2nzlO', 'Bruno Díaz',             '11 5000-2000', 'ADOPTANTE');


-- Refugios: uno verificado y uno pendiente (sirve para probar la regla 11)
INSERT INTO refugios (usuario_id, razon_social, cuit, descripcion, direccion, provincia, localidad, estado_verificacion)
SELECT id, 'Asociación Civil Refugio Sur', '30-71234567-1',
       'Rescatamos perros y gatos de la zona sur desde 2015.',
       'Av. Ejemplo 1234', 'Buenos Aires', 'Quilmes', 'VERIFICADO'
FROM usuarios WHERE email = 'refugio.sur@adoptar.test';

INSERT INTO refugios (usuario_id, razon_social, cuit, descripcion, direccion, provincia, localidad, estado_verificacion)
SELECT id, 'Patitas del Oeste', '30-79876543-2',
       'Red de hogares de tránsito del oeste del conurbano.',
       NULL, 'Buenos Aires', 'Morón', 'PENDIENTE'
FROM usuarios WHERE email = 'patitas.oeste@adoptar.test';


-- Adoptantes: Ana tiene el perfil completo. Bruno no tiene fila acá,
-- así que todavía no puede enviar solicitudes (regla 4).
INSERT INTO adoptantes (usuario_id, fecha_nacimiento, provincia, localidad, tipo_vivienda, tiene_patio, otras_mascotas)
SELECT id, DATE '1995-04-12', 'Buenos Aires', 'Lanús', 'CASA', TRUE, FALSE
FROM usuarios WHERE email = 'ana@adoptar.test';


-- Mascotas del Refugio Sur (Mora está en un hogar de tránsito en otra localidad)
INSERT INTO mascotas (refugio_id, nombre, especie, raza, sexo, tamanio, fecha_nac_aprox,
                      castrado, vacunado, desparasitado, descripcion, provincia, localidad)
SELECT r.usuario_id, m.nombre, m.especie, m.raza, m.sexo, m.tamanio, m.fecha_nac::date,
       m.castrado, m.vacunado, m.desparasitado, m.descripcion, 'Buenos Aires', m.localidad
FROM refugios r
CROSS JOIN (VALUES
    ('Lola',  'PERRO', 'Mestiza',       'HEMBRA', 'MEDIANO', '2023-03-01', TRUE,  TRUE,  TRUE, 'Muy cariñosa, se lleva bien con chicos.',        'Quilmes'),
    ('Toto',  'PERRO', 'Mestizo',       'MACHO',  'GRANDE',  '2021-08-15', TRUE,  TRUE,  TRUE, 'Tranquilo, ideal para una casa con patio.',      'Quilmes'),
    ('Simón', 'GATO',  'Común europeo', 'MACHO',  'CHICO',   '2024-01-10', FALSE, TRUE,  TRUE, 'Juguetón y curioso.',                            'Quilmes'),
    ('Mora',  'GATO',  NULL,            'HEMBRA', 'CHICO',   NULL,         TRUE,  FALSE, TRUE, 'Rescatada de la calle, todavía un poco tímida.', 'Bernal')
) AS m (nombre, especie, raza, sexo, tamanio, fecha_nac, castrado, vacunado, desparasitado, descripcion, localidad)
WHERE r.cuit = '30-71234567-1';

INSERT INTO fotos_mascota (mascota_id, url, orden)
SELECT id, 'https://example.com/fotos/mascota-' || id || '.jpg', 0
FROM mascotas;


-- Cuestionario del Refugio Sur
INSERT INTO preguntas (refugio_id, texto, tipo, obligatoria, orden)
SELECT r.usuario_id, p.texto, p.tipo, p.obligatoria, p.orden
FROM refugios r
CROSS JOIN (VALUES
    ('¿Por qué querés adoptar?',                         'TEXTO',  TRUE,  1),
    ('¿Todas las personas con las que vivís están de acuerdo?', 'SI_NO',  TRUE,  2),
    ('¿Cuántas horas por día estaría sola la mascota?',  'NUMERO', TRUE,  3),
    ('¿Tuviste mascotas antes? Contanos.',               'TEXTO',  FALSE, 4)
) AS p (texto, tipo, obligatoria, orden)
WHERE r.cuit = '30-71234567-1';


-- Una solicitud en curso: Ana quiere adoptar a Lola
INSERT INTO solicitudes (mascota_id, adoptante_id, estado)
SELECT m.id, a.usuario_id, 'ENVIADA'
FROM mascotas m
CROSS JOIN adoptantes a
JOIN usuarios u ON u.id = a.usuario_id
WHERE m.nombre = 'Lola' AND u.email = 'ana@adoptar.test';

INSERT INTO respuestas (solicitud_id, pregunta_id, valor)
SELECT s.id, p.id, r.valor
FROM solicitudes s
JOIN mascotas m  ON m.id = s.mascota_id
JOIN preguntas p ON p.refugio_id = m.refugio_id
JOIN (VALUES
    (1, 'Siempre quise un perro y ahora tengo una casa con patio.'),
    (2, 'SI'),
    (3, '4'),
    (4, 'Tuve una perra durante 12 años.')
) AS r (orden, valor) ON r.orden = p.orden
WHERE m.nombre = 'Lola';

INSERT INTO notificaciones (usuario_id, tipo, mensaje)
SELECT id, 'SOLICITUD_RECIBIDA', 'Ana Gómez envió una solicitud para adoptar a Lola.'
FROM usuarios WHERE email = 'refugio.sur@adoptar.test';
