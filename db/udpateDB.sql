-- ============================================
-- Foundly - Schema minimale (solo integrità)
-- - Nessun trigger, niente logiche di business
-- - Vincoli FK/UNIQUE/CHECK e indici essenziali
-- ============================================

USE defaultdb;

-- Drop in ordine
DROP TABLE IF EXISTS reclamo;
DROP TABLE IF EXISTS oggetto_smarrito;
DROP TABLE IF EXISTS animale_smarrito;
DROP TABLE IF EXISTS drop_point;
DROP TABLE IF EXISTS `user`;

-- 1) Utenti
CREATE TABLE `user` (
  id_utente        INT AUTO_INCREMENT PRIMARY KEY,
  username         VARCHAR(50)  NOT NULL UNIQUE,
  nome             VARCHAR(50)  NOT NULL,
  cognome          VARCHAR(50)  NOT NULL,
  email            VARCHAR(100) NOT NULL UNIQUE,
  passkey          VARCHAR(255) NOT NULL,
  citta            VARCHAR(100) NOT NULL,
  provincia        VARCHAR(100) NOT NULL,
  numero_telefono  VARCHAR(15)  NOT NULL,
  punti            INT NOT NULL DEFAULT 0,
  badge            ENUM('Sherlock Holmes','Lara Croft','Indiana Jones','Dora') DEFAULT NULL,
  ruolo            ENUM('user','admin','drop_point') NOT NULL DEFAULT 'user',
  immagine_profilo VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Drop-Point (con stato + responsabile opzionale)
CREATE TABLE drop_point (
  id_drop_point       INT AUTO_INCREMENT PRIMARY KEY,
  nome_attivita       VARCHAR(100) NOT NULL,
  indirizzo           VARCHAR(255) NOT NULL,
  citta               VARCHAR(100) NOT NULL,
  provincia           VARCHAR(100) NOT NULL,
  telefono            VARCHAR(20),
  email_contatto      VARCHAR(100),
  orari_apertura      ENUM(
    '00:00','00:30','01:00','01:30','02:00','02:30','03:00','03:30','04:00','04:30',
    '05:00','05:30','06:00','06:30','07:00','07:30','08:00','08:30','09:00','09:30',
    '10:00','10:30','11:00','11:30','12:00','12:30','13:00','13:30','14:00','14:30',
    '15:00','15:30','16:00','16:30','17:00','17:30','18:00','18:30','19:00','19:30',
    '20:00','20:30','21:00','21:30','22:00','22:30','23:00','23:30'
  ),
  orari_chiusura      ENUM(
    '00:00','00:30','01:00','01:30','02:00','02:30','03:00','03:30','04:00','04:30',
    '05:00','05:30','06:00','06:30','07:00','07:30','08:00','08:30','09:00','09:30',
    '10:00','10:30','11:00','11:30','12:00','12:30','13:00','13:30','14:00','14:30',
    '15:00','15:30','16:00','16:30','17:00','17:30','18:00','18:30','19:00','19:30',
    '20:00','20:30','21:00','21:30','22:00','22:30','23:00','23:30'
  ),
  descrizione         TEXT,
  latitudine          DECIMAL(10,7),
  longitudine         DECIMAL(10,7),
  data_registrazione  DATETIME DEFAULT CURRENT_TIMESTAMP,
  immagine            VARCHAR(255) NOT NULL,

  -- stato operativo (per RF_GDP_2 e RF_GA_3)
  stato               ENUM('in_attesa','approvato','sospeso') NOT NULL DEFAULT 'in_attesa',

  -- (opzionale) account responsabile con ruolo 'drop_point'
  id_utente_responsabile INT NULL,
  CONSTRAINT fk_dp_responsabile
    FOREIGN KEY (id_utente_responsabile) REFERENCES `user`(id_utente) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Segnalazioni oggetti
CREATE TABLE oggetto_smarrito (
  id_oggetto          INT AUTO_INCREMENT PRIMARY KEY,
  id_utente           INT NOT NULL,           -- finder
  id_drop_point       INT NULL,               -- DP suggerito (facoltativo)

  titolo              VARCHAR(100) NOT NULL,
  descrizione         TEXT NOT NULL,
  categoria           ENUM('Documenti','Portafogli e borse','Abbigliamento','Gioielli e accessori','Chiavi','Elettronica','Altro'),
  marca               VARCHAR(100),
  caratteristiche     TEXT,

  domanda_verifica1   VARCHAR(255) NOT NULL,
  domanda_verifica2   VARCHAR(255) NOT NULL,
  domanda_verifica3   VARCHAR(255),

  data_ritrovamento   DATE NOT NULL,
  luogo_ritrovamento  VARCHAR(255) NOT NULL,
  citta               VARCHAR(100) NOT NULL,
  provincia           VARCHAR(100) NOT NULL,

  stato               ENUM('aperta','chiusa') NOT NULL DEFAULT 'aperta',
  data_pubblicazione  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  immagine            VARCHAR(255) NOT NULL,

  CONSTRAINT fk_obj_user  FOREIGN KEY (id_utente)     REFERENCES `user`(id_utente) ON DELETE CASCADE,
  CONSTRAINT fk_obj_dp    FOREIGN KEY (id_drop_point) REFERENCES drop_point(id_drop_point) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) Segnalazioni animali
CREATE TABLE animale_smarrito (
  id_animale          INT AUTO_INCREMENT PRIMARY KEY,
  id_utente           INT NOT NULL,           -- finder
  id_drop_point       INT NULL,

  nome                VARCHAR(50),
  specie              VARCHAR(50) NOT NULL,
  razza               VARCHAR(50),
  colore              VARCHAR(50),
  caratteristiche     TEXT,

  domanda_verifica1   VARCHAR(255) NOT NULL,
  domanda_verifica2   VARCHAR(255) NOT NULL,
  domanda_verifica3   VARCHAR(255),

  data_ritrovamento   DATE NOT NULL,
  luogo_ritrovamento  VARCHAR(255) NOT NULL,
  citta               VARCHAR(100) NOT NULL,
  provincia           VARCHAR(100) NOT NULL,

  stato               ENUM('aperta','chiusa') NOT NULL DEFAULT 'aperta',
  data_pubblicazione  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  immagine            VARCHAR(255),

  CONSTRAINT fk_an_user  FOREIGN KEY (id_utente)     REFERENCES `user`(id_utente) ON DELETE CASCADE,
  CONSTRAINT fk_an_dp    FOREIGN KEY (id_drop_point) REFERENCES drop_point(id_drop_point) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) Reclami (claim)
CREATE TABLE reclamo (
  id_reclamo            INT AUTO_INCREMENT PRIMARY KEY,

  id_utente_richiedente INT  NOT NULL,  -- owner (reclamante)
  id_oggetto            INT  NULL,
  id_animale            INT  NULL,

  risposta1             VARCHAR(255) NOT NULL,
  risposta2             VARCHAR(255) NOT NULL,
  risposta3             VARCHAR(255),
  messaggio             TEXT,
  data_richiesta        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  stato                 ENUM('inviato','accettato','rifiutato') NOT NULL DEFAULT 'inviato',
  motivazione_rifiuto   TEXT,

  -- Codice di consegna (generato a codice nell'app quando il claim è accettato)
  codice_consegna       CHAR(6) NULL,
  codice_scadenza       DATETIME NULL,
  codice_stato          ENUM('valido','usato','scaduto') NULL,

  -- Eventi DP (se usato il Drop-Point)
  dp_deposito_at        DATETIME NULL,
  dp_deposito_id        INT NULL,
  dp_ritiro_at          DATETIME NULL,
  dp_ritiro_id          INT NULL,

  -- Doppia conferma (scambio diretto)
  finder_conferma_at    DATETIME NULL,
  owner_conferma_at     DATETIME NULL,

  CONSTRAINT fk_re_user   FOREIGN KEY (id_utente_richiedente) REFERENCES `user`(id_utente) ON DELETE CASCADE,
  CONSTRAINT fk_re_obj    FOREIGN KEY (id_oggetto)            REFERENCES oggetto_smarrito(id_oggetto)   ON DELETE CASCADE,
  CONSTRAINT fk_re_an     FOREIGN KEY (id_animale)            REFERENCES animale_smarrito(id_animale)   ON DELETE CASCADE,
  CONSTRAINT fk_re_dp_dep FOREIGN KEY (dp_deposito_id)        REFERENCES drop_point(id_drop_point)      ON DELETE SET NULL,
  CONSTRAINT fk_re_dp_rit FOREIGN KEY (dp_ritiro_id)          REFERENCES drop_point(id_drop_point)      ON DELETE SET NULL,

  -- esattamente una delle due referenze (MySQL 8.0.16+)
  CONSTRAINT chk_re_target_xor CHECK (
    (id_oggetto IS NOT NULL AND id_animale IS NULL) OR
    (id_oggetto IS NULL     AND id_animale IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indici utili
CREATE UNIQUE INDEX ux_reclamo_codice  ON reclamo (codice_consegna);   -- univoco (più NULL ammessi)
CREATE INDEX idx_reclamo_stato         ON reclamo (stato);
CREATE INDEX idx_reclamo_oggetto       ON reclamo (id_oggetto);
CREATE INDEX idx_reclamo_animale       ON reclamo (id_animale);
