-- ==============================================================
-- Script de création de la base de données et de la table clients
-- SGBD cible : MySQL 8+ (moteur InnoDB, charset utf8mb4)
--
-- Exécution (en tant que root) :  mysql -u root -p < db/01_schema.sql
-- Ce script est également monté automatiquement par docker-compose
-- (point d'entrée /docker-entrypoint-initdb.d) — voir Partie 4.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS clientsdb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Compte applicatif dédié — principe du moindre privilège :
-- seuls SELECT / INSERT / UPDATE sont nécessaires au service SOAP et au flux NiFi.
CREATE USER IF NOT EXISTS 'soapapp'@'%' IDENTIFIED BY 'soappass';
GRANT SELECT, INSERT, UPDATE ON clientsdb.* TO 'soapapp'@'%';
FLUSH PRIVILEGES;

USE clientsdb;

CREATE TABLE IF NOT EXISTS clients (
  id                 INT           NOT NULL AUTO_INCREMENT,
  nom                VARCHAR(100)  NOT NULL,
  prenom             VARCHAR(100)  NOT NULL,
  email              VARCHAR(150)  NOT NULL,
  telephone          VARCHAR(20)   NULL,
  ville              VARCHAR(80)   NULL,
  pays               VARCHAR(80)   NULL,
  statut             VARCHAR(10)   NOT NULL DEFAULT 'ACTIF',
  date_creation      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  date_modification  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  -- Unicité métier : un client = une adresse e-mail.
  UNIQUE KEY uq_clients_email (email),
  -- Index de recherche : searchClients filtre sur nom / ville / statut.
  KEY idx_clients_nom (nom),
  KEY idx_clients_ville (ville),
  KEY idx_clients_statut (statut),
  CONSTRAINT chk_clients_statut CHECK (statut IN ('ACTIF', 'INACTIF'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
