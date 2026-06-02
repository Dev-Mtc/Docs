-- ============================================================================
--  Script de création du schéma pour le batch sandre-sync-batch.
--  PostgreSQL ≥ 12 (utilise le type jsonb).
--
--  À exécuter manuellement avant le premier lancement :
--      psql -d sandre -f schema.sql
--
--  Le batch lui-même n'effectue jamais de DDL (spring.jpa.hibernate.ddl-auto=validate).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table d'historique de synchronisation (supervision exploitation BRGM)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.sync_history (
    id                BIGSERIAL PRIMARY KEY,
    job_execution_id  BIGINT,
    referentiel       VARCHAR(50)  NOT NULL,
    statut            VARCHAR(20)  NOT NULL,
    started_at        TIMESTAMP    NOT NULL,
    finished_at       TIMESTAMP,
    nb_recus          INTEGER,
    nb_upsert         INTEGER,
    derniere_date_maj TIMESTAMP,
    message_erreur    TEXT,
    duree_ms          BIGINT
);

CREATE INDEX IF NOT EXISTS idx_sync_history_ref_date
    ON public.sync_history (referentiel, started_at DESC);

-- ----------------------------------------------------------------------------
-- Référentiels SANDRE
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS public.ref_parametre (
    cd_parametre              VARCHAR(50)  PRIMARY KEY,
    nom_parametre             VARCHAR(500),
    st_parametre              VARCHAR(50),
    auteur_parametre          VARCHAR(500),
    date_creation_parametre   TIMESTAMP,
    date_maj_parametre        TIMESTAMP,
    raw_data                  JSONB
);
CREATE INDEX IF NOT EXISTS idx_ref_parametre_maj
    ON public.ref_parametre (date_maj_parametre);

CREATE TABLE IF NOT EXISTS public.ref_methode (
    cd_methode             VARCHAR(50)  PRIMARY KEY,
    nom_methode            VARCHAR(500),
    st_methode             VARCHAR(50),
    auteur_methode         VARCHAR(500),
    date_creation_methode  TIMESTAMP,
    date_maj_methode       TIMESTAMP,
    raw_data               JSONB
);
CREATE INDEX IF NOT EXISTS idx_ref_methode_maj
    ON public.ref_methode (date_maj_methode);

CREATE TABLE IF NOT EXISTS public.ref_cours_eau (
    cd_entite_hydrographique         VARCHAR(50)  PRIMARY KEY,
    nom_entite_hydrographique        VARCHAR(500),
    st_entite_hydrographique         VARCHAR(50),
    date_cre_entite_hydrographique   TIMESTAMP,
    date_maj_entite_hydrographique   TIMESTAMP,
    raw_data                         JSONB
);
CREATE INDEX IF NOT EXISTS idx_ref_cours_eau_maj
    ON public.ref_cours_eau (date_maj_entite_hydrographique);

CREATE TABLE IF NOT EXISTS public.ref_obstacle (
    cd_obst_ecoul              VARCHAR(50)  PRIMARY KEY,
    nom_principal_obst_ecoul   VARCHAR(500),
    st_obst_ecoul              VARCHAR(50),
    date_valid_obst_ecoul      TIMESTAMP,
    date_maj_obst_ecoul        TIMESTAMP,
    raw_data                   JSONB
);
CREATE INDEX IF NOT EXISTS idx_ref_obstacle_maj
    ON public.ref_obstacle (date_maj_obst_ecoul);

CREATE TABLE IF NOT EXISTS public.ref_intervenant (
    cd_intervenant              VARCHAR(50)  PRIMARY KEY,
    nom_intervenant             VARCHAR(500),
    mn_intervenant              VARCHAR(100),
    st_intervenant              VARCHAR(50),
    auteur_intervenant          VARCHAR(500),
    date_creation_intervenant   TIMESTAMP,
    date_maj_intervenant        TIMESTAMP,
    raw_data                    JSONB
);
CREATE INDEX IF NOT EXISTS idx_ref_intervenant_maj
    ON public.ref_intervenant (date_maj_intervenant);
