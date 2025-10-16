-- =========================================================
-- Foundly - Schema minimale (MySQL 8.0+ / InnoDB / utf8mb4)
-- =========================================================

-- CREATE DATABASE IF NOT EXISTS foundly DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE foundly;

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ---------------------------------------------------------
-- UTENTE (utente registrato / admin)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS utente;
CREATE TABLE utente (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email           VARCHAR(190)  NOT NULL,
  telefono        VARCHAR(40)   NOT NULL,
  password_hash   VARCHAR(255)  NOT NULL,
  ruolo           ENUM('utente','admin') NOT NULL DEFAULT 'utente',
  stato           ENUM('attivo','sospeso') NOT NULL DEFAULT 'attivo',
  punteggio       INT NOT NULL DEFAULT 0,
  creato_il       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_utente_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- DROP_POINT (ha un proprio account)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS drop_point;
CREATE TABLE drop_point (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome            VARCHAR(190)  NOT NULL,
  email           VARCHAR(190)  NOT NULL,
  telefono        VARCHAR(40)   NOT NULL,
  password_hash   VARCHAR(255)  NOT NULL,
  indirizzo       VARCHAR(255)  NOT NULL,
  citta           VARCHAR(120)  NOT NULL,
  provincia       VARCHAR(60)   NOT NULL,
  cap             VARCHAR(20)   NOT NULL,
  stato           ENUM('in_attesa','approvato','sospeso') NOT NULL DEFAULT 'in_attesa',
  creato_il       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_dp_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- SEGNALAZIONE (oggetto/animale ritrovato)
-- modalita_consegna: 'diretta' (no codice) | 'drop_point' (con codice)
-- lo stato lo gestisci da codice (aperta/in_consegna/chiusa/annullata)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS segnalazione;
CREATE TABLE segnalazione (
  id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  creatore_id             BIGINT UNSIGNED NOT NULL,            -- utente che pubblica
  titolo                  VARCHAR(190) NOT NULL,
  descrizione             TEXT NOT NULL,
  categoria               ENUM('oggetto','animale') NOT NULL,
  data_ritrovamento       DATE NOT NULL,
  indirizzo               VARCHAR(255) NULL,
  latitudine              DECIMAL(10,7) NULL,
  longitudine             DECIMAL(10,7) NULL,

  stato                   ENUM('aperta','in_consegna','chiusa','annullata') NOT NULL DEFAULT 'aperta',
  modalita_consegna       ENUM('diretta','drop_point') NOT NULL DEFAULT 'diretta',

  -- consegna diretta (due bottoni)
  consegna_ok_il          DATETIME NULL,
  ricezione_ok_il         DATETIME NULL,

  -- drop-point (con codice)
  drop_point_id           BIGINT UNSIGNED NULL,
  codice_consegna         CHAR(6) NULL,
  dp_deposito_il          DATETIME NULL,
  dp_ritiro_il            DATETIME NULL,

  -- claim accettato che ha avviato la restituzione (facoltativo)
  claim_accettato_id      BIGINT UNSIGNED NULL,

  creato_il               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY ux_segn_codice (codice_consegna),
  KEY ix_segn_creatore (creatore_id),
  KEY ix_segn_dp (drop_point_id),
  CONSTRAINT fk_segn_user FOREIGN KEY (creatore_id)
    REFERENCES utente(id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_segn_dp FOREIGN KEY (drop_point_id)
    REFERENCES drop_point(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- FOTO della segnalazione (semplice: URL)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS segnalazione_foto;
CREATE TABLE segnalazione_foto (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  segnalazione_id   BIGINT UNSIGNED NOT NULL,
  url               VARCHAR(500) NOT NULL,
  ordinamento       INT NOT NULL DEFAULT 0,
  creato_il         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_foto_segn (segnalazione_id, ordinamento),
  CONSTRAINT fk_foto_segn FOREIGN KEY (segnalazione_id)
    REFERENCES segnalazione(id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- SECURE_CLAIM (richiesta di restituzione)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS secure_claim;
CREATE TABLE secure_claim (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  segnalazione_id   BIGINT UNSIGNED NOT NULL,
  richiedente_id    BIGINT UNSIGNED NOT NULL,   -- utente che reclama
  messaggio         TEXT NULL,
  prove_url         TEXT NULL,
  stato             ENUM('inviato','accettato','rifiutato','annullato') NOT NULL DEFAULT 'inviato',
  motivazione_rifiuto TEXT NULL,
  creato_il         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deciso_il         DATETIME NULL,
  PRIMARY KEY (id),
  KEY ix_sc_segn (segnalazione_id),
  KEY ix_sc_user (richiedente_id),
  CONSTRAINT fk_sc_segn FOREIGN KEY (segnalazione_id)
    REFERENCES segnalazione(id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_sc_user FOREIGN KEY (richiedente_id)
    REFERENCES utente(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- collega (facoltativo) il claim accettato alla segnalazione
ALTER TABLE segnalazione
  ADD CONSTRAINT fk_segn_claim_acc
  FOREIGN KEY (claim_accettato_id) REFERENCES secure_claim(id)
  ON UPDATE CASCADE ON DELETE SET NULL;

-- =========================================================
-- FINE
-- =========================================================
