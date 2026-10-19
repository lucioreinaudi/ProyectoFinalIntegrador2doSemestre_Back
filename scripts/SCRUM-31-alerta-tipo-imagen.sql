-- SCRUM-31: tipo e imagen de las alertas.
-- Correr una sola vez en la BD compartida (Supabase), antes de levantar el back con esta rama (ddl-auto=validate).

ALTER TABLE "Alerta"
    ADD COLUMN tipo varchar(30) NOT NULL DEFAULT 'otro',
    ADD COLUMN imagen_url text;

ALTER TABLE "Alerta"
    ADD CONSTRAINT alerta_tipo_check
    CHECK (tipo IN ('tormenta', 'viento', 'electricidad', 'arbol', 'otro'));
