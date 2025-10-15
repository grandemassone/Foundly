-- ============================================
-- Foundly - Schema completo "from scratch"
-- (coerente con: claim -> codice -> chiusura
--  via Drop-Point o doppia conferma diretta)
-- ============================================

USE defaultdb;

-- Drop in ordine (prima i trigger, poi le tabelle)
DROP TRIGGER IF EXISTS trg_user_badge_update;
DROP TRIGGER IF EXISTS trg_user_badge_initial;
DROP TRIGGER IF EXISTS trg_reclamo_close_and_score;
DROP TRIGGER IF EXISTS trg_reclamo_generate_code;

DROP TABLE IF EXISTS reclamo;
DROP TABLE IF EXISTS oggetto_smarrito;
DROP TABLE IF EXISTS animale_smarrito;
DROP TABLE IF EXISTS drop_point;
DROP TABLE IF EXISTS `user`;

-- ======================
-- 1) Tabelle di base
-- ======================
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
  ruolo            ENUM('user','admin') DEFAULT 'user',
  immagine_profilo VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  immagine            VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 2) Segnalazioni (finder)
-- =========================
CREATE TABLE oggetto_smarrito (
  id_oggetto          INT AUTO_INCREMENT PRIMARY KEY,
  id_utente           INT NOT NULL,
  id_drop_point       INT NULL,

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

CREATE TABLE animale_smarrito (
  id_animale          INT AUTO_INCREMENT PRIMARY KEY,
  id_utente           INT NOT NULL,
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

-- =======================
-- 3) Claim (owner)
-- =======================
CREATE TABLE reclamo (
  id_reclamo            INT AUTO_INCREMENT PRIMARY KEY,

  id_utente_richiedente INT  NOT NULL,  -- owner
  id_oggetto            INT  NULL,
  id_animale            INT  NULL,

  risposta1             VARCHAR(255) NOT NULL,
  risposta2             VARCHAR(255) NOT NULL,
  risposta3             VARCHAR(255),

  messaggio             TEXT,
  data_richiesta        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  stato                 ENUM('inviato','accettato','rifiutato') NOT NULL DEFAULT 'inviato',
  motivazione_rifiuto   TEXT,

  -- Codice di consegna (solo dopo accettazione)
  codice_consegna       CHAR(6) NULL,
  codice_scadenza       DATETIME NULL,
  codice_stato          ENUM('valido','usato','scaduto') NULL,

  -- Eventi Drop-Point (facoltativi)
  dp_deposito_at        DATETIME NULL,
  dp_deposito_id        INT NULL,
  dp_ritiro_at          DATETIME NULL,
  dp_ritiro_id          INT NULL,

  -- Doppia conferma diretta (senza DP)
  finder_conferma_at    DATETIME NULL,
  owner_conferma_at     DATETIME NULL,

  CONSTRAINT fk_re_user   FOREIGN KEY (id_utente_richiedente) REFERENCES `user`(id_utente) ON DELETE CASCADE,
  CONSTRAINT fk_re_obj    FOREIGN KEY (id_oggetto)            REFERENCES oggetto_smarrito(id_oggetto)   ON DELETE CASCADE,
  CONSTRAINT fk_re_an     FOREIGN KEY (id_animale)            REFERENCES animale_smarrito(id_animale)   ON DELETE CASCADE,
  CONSTRAINT fk_re_dp_dep FOREIGN KEY (dp_deposito_id)        REFERENCES drop_point(id_drop_point)      ON DELETE SET NULL,
  CONSTRAINT fk_re_dp_rit FOREIGN KEY (dp_ritiro_id)          REFERENCES drop_point(id_drop_point)      ON DELETE SET NULL,

  -- XOR: o oggetto, o animale (MySQL 8.0.16+)
  CONSTRAINT chk_re_target_xor CHECK (
    (id_oggetto IS NOT NULL AND id_animale IS NULL) OR
    (id_oggetto IS NULL     AND id_animale IS NOT NULL)
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indici utili (una sola volta)
CREATE INDEX idx_reclamo_stato   ON reclamo (stato);
CREATE INDEX idx_reclamo_codice  ON reclamo (codice_consegna, codice_stato);
CREATE INDEX idx_reclamo_oggetto ON reclamo (id_oggetto);
CREATE INDEX idx_reclamo_animale ON reclamo (id_animale);

-- ======================
-- 4) Trigger
-- ======================
DELIMITER $$

-- Badge in base ai punti (all'insert)
CREATE TRIGGER trg_user_badge_initial
BEFORE INSERT ON `user`
FOR EACH ROW
BEGIN
  IF NEW.punti >= 200 THEN
    SET NEW.badge = 'Sherlock Holmes';
  ELSEIF NEW.punti >= 100 THEN
    SET NEW.badge = 'Indiana Jones';
  ELSEIF NEW.punti >= 50 THEN
    SET NEW.badge = 'Lara Croft';
  ELSEIF NEW.punti >= 10 THEN
    SET NEW.badge = 'Dora';
  ELSE
    SET NEW.badge = NULL;
  END IF;
END$$

-- Genera codice quando il claim viene accettato
CREATE TRIGGER trg_reclamo_generate_code
BEFORE UPDATE ON reclamo
FOR EACH ROW
BEGIN
  IF NEW.stato = 'accettato' AND (OLD.stato <> 'accettato' OR OLD.stato IS NULL) THEN
    IF NEW.codice_consegna IS NULL OR NEW.codice_consegna = '' THEN
      SET NEW.codice_consegna = LPAD(FLOOR(RAND()*1000000), 6, '0');
      SET NEW.codice_scadenza = DATE_ADD(NOW(), INTERVAL 14 DAY);
      SET NEW.codice_stato    = 'valido';
    END IF;
  END IF;

  IF NEW.codice_consegna IS NOT NULL AND NEW.codice_scadenza IS NOT NULL
     AND NOW() > NEW.codice_scadenza THEN
    SET NEW.codice_stato = 'scaduto';
  END IF;
END$$

-- Chiusura segnalazione + punti (DP o doppia conferma)
CREATE TRIGGER trg_reclamo_close_and_score
AFTER UPDATE ON reclamo
FOR EACH ROW
BEGIN
  DECLARE v_finder_id INT;

  IF NEW.codice_stato = 'valido' AND (
       (NEW.dp_deposito_at IS NOT NULL AND NEW.dp_ritiro_at IS NOT NULL) OR
       (NEW.finder_conferma_at IS NOT NULL AND NEW.owner_conferma_at IS NOT NULL)
     )
  THEN
    -- marca il codice come usato
    UPDATE reclamo
       SET codice_stato = 'usato'
     WHERE id_reclamo = NEW.id_reclamo;

    -- chiudi la segnalazione e ricava il finder
    IF NEW.id_oggetto IS NOT NULL THEN
      UPDATE oggetto_smarrito
         SET stato = 'chiusa'
       WHERE id_oggetto = NEW.id_oggetto AND stato <> 'chiusa';
      SELECT id_utente INTO v_finder_id
        FROM oggetto_smarrito
       WHERE id_oggetto = NEW.id_oggetto;
    ELSEIF NEW.id_animale IS NOT NULL THEN
      UPDATE animale_smarrito
         SET stato = 'chiusa'
       WHERE id_animale = NEW.id_animale AND stato <> 'chiusa';
      SELECT id_utente INTO v_finder_id
        FROM animale_smarrito
       WHERE id_animale = NEW.id_animale;
    END IF;

    -- 1 punto al finder (scoreboard)
    IF v_finder_id IS NOT NULL THEN
      UPDATE `user` SET punti = punti + 1 WHERE id_utente = v_finder_id;
    END IF;
  END IF;
END$$

-- Badge anche quando i punti cambiano
CREATE TRIGGER trg_user_badge_update
AFTER UPDATE ON `user`
FOR EACH ROW
BEGIN
  IF NEW.punti <> OLD.punti THEN
    IF NEW.punti >= 200 THEN
      UPDATE `user` SET badge = 'Sherlock Holmes' WHERE id_utente = NEW.id_utente;
    ELSEIF NEW.punti >= 100 THEN
      UPDATE `user` SET badge = 'Indiana Jones'   WHERE id_utente = NEW.id_utente;
    ELSEIF NEW.punti >= 50 THEN
      UPDATE `user` SET badge = 'Lara Croft'      WHERE id_utente = NEW.id_utente;
    ELSEIF NEW.punti >= 10 THEN
      UPDATE `user` SET badge = 'Dora'            WHERE id_utente = NEW.id_utente;
    ELSE
      UPDATE `user` SET badge = NULL              WHERE id_utente = NEW.id_utente;
    END IF;
  END IF;
END$$

DELIMITER ;
