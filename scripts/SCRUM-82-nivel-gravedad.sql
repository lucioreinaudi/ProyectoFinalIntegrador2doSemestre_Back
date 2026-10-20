-- SCRUM-82: restringir el nivel de gravedad de las alertas a alto / medio / bajo
-- (los mismos niveles que usa el Home). El back ya lo valida; el CHECK es la
-- segunda barrera, igual que con el estado y el tipo de la alerta.
-- Correr una sola vez en la BD compartida (Supabase).

-- 1. Normalizar lo que ya existe (antes del CHECK, si no el ALTER falla).
--    Al momento de escribir esto habia 1 alerta cargada con 'alta'.
UPDATE "Alerta" SET nivel_gravedad = lower(trim(nivel_gravedad));
UPDATE "Alerta" SET nivel_gravedad = 'alto' WHERE nivel_gravedad = 'alta';
UPDATE "Alerta" SET nivel_gravedad = 'medio' WHERE nivel_gravedad = 'media';
UPDATE "Alerta" SET nivel_gravedad = 'bajo' WHERE nivel_gravedad = 'baja';

-- 2. Verificar que no quede ningun valor raro (tiene que devolver 0 filas).
--    Si devuelve algo, corregirlo a mano antes de seguir con el paso 3.
SELECT id_alerta, nivel_gravedad FROM "Alerta" WHERE nivel_gravedad NOT IN ('alto', 'medio', 'bajo');

-- 3. El CHECK.
ALTER TABLE "Alerta"
    ADD CONSTRAINT "Alerta_nivel_gravedad_check"
    CHECK (nivel_gravedad IN ('alto', 'medio', 'bajo'));
