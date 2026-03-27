-- Installation locale SANS Docker : créer l'utilisateur et la base pour FleetPro.
-- Identifiants alignés sur Backend/src/main/resources/application.yml (défaut).
--
-- Exemple (PowerShell) — mot de passe demandé pour « postgres » :
--   psql -U postgres -h localhost -d postgres -f "C:\Users\...\FlootPro\scripts\init-local-postgres.sql"
--
-- En cas d'erreur « already exists », l'utilisateur ou la base existe déjà : passe à l'étape backend.

CREATE ROLE fleetpro WITH LOGIN PASSWORD 'fleetpro';

CREATE DATABASE fleetpro OWNER fleetpro ENCODING 'UTF8' TEMPLATE template0;

\connect fleetpro

-- PostgreSQL 15+ : droits explicites sur le schéma public pour le propriétaire applicatif
GRANT ALL ON SCHEMA public TO fleetpro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO fleetpro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO fleetpro;
