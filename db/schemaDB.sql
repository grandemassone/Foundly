DROP DATABASE IF EXISTS defaultdb;
CREATE DATABASE defaultdb;
USE defaultdb;

-- 1. UTENTE
CREATE TABLE utente
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                     VARCHAR(50)  NOT NULL UNIQUE,
    email                        VARCHAR(100) NOT NULL UNIQUE,
    password_hash                VARCHAR(255) NOT NULL,
    nome                         VARCHAR(50)  NOT NULL,
    cognome                      VARCHAR(50)  NOT NULL,
    telefono                     VARCHAR(20),
    immagine_profilo             LONGBLOB,
    immagine_profilo_content_type VARCHAR(50),
    punteggio                    INT                                                      DEFAULT 0,
    ruolo                        ENUM ('UTENTE_BASE', 'ADMIN')                            DEFAULT 'UTENTE_BASE',
    badge                        ENUM ('OCCHIO_DI_FALCO', 'DETECTIVE', 'SHERLOCK_HOLMES') DEFAULT 'OCCHIO_DI_FALCO'
);

-- 2. DROP-POINT
CREATE TABLE drop_point
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_attivita            VARCHAR(100) NOT NULL,
    email                    VARCHAR(100) NOT NULL UNIQUE,
    password_hash            VARCHAR(255) NOT NULL,
    indirizzo                VARCHAR(100) NOT NULL,
    citta                    VARCHAR(50)  NOT NULL,
    provincia                VARCHAR(50)  NOT NULL,
    telefono                 VARCHAR(20),
    orari_apertura           VARCHAR(255),
    immagine                 LONGBLOB,
    immagine_content_type    VARCHAR(50),
    latitudine               DOUBLE,
    longitudine              DOUBLE,
    ritiri_effettuati        INT                                          DEFAULT 0,
    stato                    ENUM ('IN_ATTESA', 'APPROVATO', 'RIFIUTATO') DEFAULT 'IN_ATTESA'
);

-- 3. SEGNALAZIONE (Tabella Padre - Abstract)
CREATE TABLE segnalazione
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utente          BIGINT                      NOT NULL,
    titolo             VARCHAR(100)                NOT NULL,
    descrizione        TEXT                        NOT NULL,
    data_ritrovamento  DATETIME                    NOT NULL,
    luogo_ritrovamento VARCHAR(100)                NOT NULL,
    citta              VARCHAR(50)                 NOT NULL,
    provincia          VARCHAR(50)                 NOT NULL,
    latitudine         DOUBLE,
    longitudine        DOUBLE,
    immagine           LONGBLOB,
    immagine_content_type VARCHAR(50),
    domanda_verifica1  VARCHAR(255)                NOT NULL,
    domanda_verifica2  VARCHAR(255)                NOT NULL,
    data_pubblicazione DATETIME                                 DEFAULT CURRENT_TIMESTAMP,
    stato              ENUM ('APERTA', 'IN_CONSEGNA', 'CHIUSA') DEFAULT 'APERTA',
    tipo_segnalazione  ENUM ('OGGETTO', 'ANIMALE') NOT NULL,
    FOREIGN KEY (id_utente) REFERENCES utente (id) ON DELETE CASCADE
);

-- 4. SEGNALAZIONE OGGETTO (Estensione)
CREATE TABLE segnalazione_oggetto
(
    id_segnalazione   BIGINT PRIMARY KEY,
    categoria         ENUM ('DOCUMENTI', 'ELETTRONICA', 'PORTAFOGLI', 'CHIAVI', 'GIOIELLI', 'ABBIGLIAMENTO', 'ALTRO') NOT NULL,
    modalita_consegna ENUM ('DIRETTA', 'DROP_POINT')                                                                  NOT NULL,
    id_drop_point     BIGINT,
    FOREIGN KEY (id_segnalazione) REFERENCES segnalazione (id) ON DELETE CASCADE,
    FOREIGN KEY (id_drop_point) REFERENCES drop_point (id) ON DELETE SET NULL
);

-- 5. SEGNALAZIONE ANIMALE (Estensione)
CREATE TABLE segnalazione_animale
(
    id_segnalazione BIGINT PRIMARY KEY,
    specie          VARCHAR(50) NOT NULL,
    razza           VARCHAR(50),
    FOREIGN KEY (id_segnalazione) REFERENCES segnalazione (id) ON DELETE CASCADE
);

-- 6. RECLAMO (Secure Claim)
CREATE TABLE reclamo
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_segnalazione       BIGINT       NOT NULL,
    id_utente_richiedente BIGINT       NOT NULL,
    risposta_verifica1    VARCHAR(255) NOT NULL,
    risposta_verifica2    VARCHAR(255) NOT NULL,
    data_richiesta        DATETIME                                     DEFAULT CURRENT_TIMESTAMP,
    stato                 ENUM ('IN_ATTESA', 'ACCETTATO', 'RIFIUTATO') DEFAULT 'IN_ATTESA',
    codice_consegna       VARCHAR(6) UNIQUE,
    data_deposito         DATETIME,
    data_ritiro           DATETIME,
    conferma_finder       BOOLEAN                                      DEFAULT FALSE,
    conferma_owner        BOOLEAN                                      DEFAULT FALSE,

    FOREIGN KEY (id_segnalazione) REFERENCES segnalazione (id) ON DELETE CASCADE,
    FOREIGN KEY (id_utente_richiedente) REFERENCES utente (id) ON DELETE CASCADE
);
