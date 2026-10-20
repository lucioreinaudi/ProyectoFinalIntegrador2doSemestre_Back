-- SCRUM-77: cambios de esquema para la Fase 2+ del panel de administracion.
-- Correr una sola vez en la BD compartida (Supabase), antes de levantar el back con esta rama (ddl-auto=validate).

-- 1. Agregar "rechazado" a los estados posibles de un reclamo.
-- No se puede "ampliar" un CHECK in-place en Postgres: hay que sacarlo y volver a crearlo con la lista completa.
ALTER TABLE "EstadoReclamo" DROP CONSTRAINT "EstadoReclamo_estado_check";
ALTER TABLE "EstadoReclamo"
    ADD CONSTRAINT "EstadoReclamo_estado_check"
    CHECK (estado IN ('recibido', 'en revision', 'en proceso', 'resuelto', 'rechazado'));

-- 2. Alta/baja logica de Zona y Categoria (no se borran filas, se desactivan).
ALTER TABLE "Zona" ADD COLUMN activa boolean NOT NULL DEFAULT true;
ALTER TABLE "Categoria" ADD COLUMN activa boolean NOT NULL DEFAULT true;

-- 3. Fecha de registro del usuario (para el directorio de usuarios del panel).
-- Los 8 usuarios que ya existen van a quedar con la fecha de HOY como aproximacion
-- (no se guardo la fecha real de alta en su momento, no hay forma de recuperarla).
ALTER TABLE "Usuario" ADD COLUMN fecha_registro timestamptz NOT NULL DEFAULT now();
