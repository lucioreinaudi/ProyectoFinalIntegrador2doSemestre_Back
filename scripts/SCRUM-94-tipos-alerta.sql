-- SCRUM-94: tipos de alerta administrables desde el panel (Configuracion).
-- Antes eran una lista fija (CHECK alerta_tipo_check). Ahora viven en la tabla
-- "TipoAlerta" y Alerta.tipo apunta a su clave.
-- Correr una sola vez en la BD compartida (Supabase), antes de levantar el back
-- con esta rama (ddl-auto=validate).

-- 1. La tabla. "clave" es lo que se guarda en Alerta.tipo (no cambia nunca);
--    "nombre" es lo que se muestra y se puede editar.
CREATE TABLE "TipoAlerta" (
    id_tipo    bigserial PRIMARY KEY,
    clave      varchar(30) NOT NULL UNIQUE,
    nombre     varchar(60) NOT NULL UNIQUE,
    icono      varchar(30) NOT NULL DEFAULT 'alert-triangle',
    imagen_url text,
    activa     boolean NOT NULL DEFAULT true
);

-- 2. Los 5 tipos que ya existian, con las mismas claves (imagen_url vacia: el
--    front sigue usando las imagenes de public/img/alertas hasta que se cambien).
INSERT INTO "TipoAlerta" (clave, nombre, icono) VALUES
    ('tormenta', 'Tormenta', 'cloud-rain'),
    ('viento', 'Viento', 'wind'),
    ('electricidad', 'Electricidad', 'zap'),
    ('arbol', 'Árbol', 'tree'),
    ('otro', 'Otro', 'alert-triangle');

-- 3. Cambiar el CHECK fijo por una clave foranea a la tabla nueva.
ALTER TABLE "Alerta" DROP CONSTRAINT alerta_tipo_check;
ALTER TABLE "Alerta"
    ADD CONSTRAINT alerta_tipo_fk FOREIGN KEY (tipo) REFERENCES "TipoAlerta"(clave);
