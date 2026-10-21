-- SCRUM-88: fotos de reclamos (varias por reclamo).
-- Correr una sola vez en la BD compartida (Supabase), antes de levantar el back con esta rama (ddl-auto=validate).
-- Ademas hay que crear el bucket publico "reclamos" en Storage.

CREATE TABLE IF NOT EXISTS "Reclamo_Foto" (
    id_foto      bigserial PRIMARY KEY,
    reclamo_id   bigint NOT NULL REFERENCES "Reclamo"(id_reclamo) ON DELETE CASCADE,
    url          text NOT NULL,
    orden        smallint NOT NULL DEFAULT 0,
    fecha_subida timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reclamo_foto_reclamo ON "Reclamo_Foto"(reclamo_id);
