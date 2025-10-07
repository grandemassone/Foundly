-- Various queries

use defaultdb;

drop table user;
drop table drop_point;
drop table oggetto_smarrito;
drop table animale_smarrito;
drop table reclamo;

select * from user;
select * from drop_point;
select * from oggetto_smarrito;
select * from animale_smarrito;
select * from reclamo;

-- Creation tables

CREATE TABLE user (
  id_utente INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  nome VARCHAR(50) NOT NULL,
  cognome VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  passkey VARCHAR(255) NOT NULL,
  citta VARCHAR(100) NOT NULL,
  provincia VARCHAR(100) NOT NULL,
  numero_telefono VARCHAR(15) NOT NULL,
  punti INT NOT NULL DEFAULT 0,
  badge ENUM('Sherlock Holmes', 'Lara Croft', 'Indiana Jones', 'Dora') DEFAULT NULL,
  ruolo ENUM('user', 'admin') DEFAULT 'user',
  immagine_profilo VARCHAR(255)
);

CREATE TABLE drop_point (
  id_drop_point INT AUTO_INCREMENT PRIMARY KEY,
  nome_attivita VARCHAR(100) NOT NULL,
  indirizzo VARCHAR(255) NOT NULL,
  citta VARCHAR(100) NOT NULL,
  provincia VARCHAR(100) NOT NULL,
  telefono VARCHAR(20),
  email_contatto VARCHAR(100),
  orari_apertura ENUM(
  '00:00', '00:30', '01:00', '01:30', '02:00', '02:30',
  '03:00', '03:30', '04:00', '04:30', '05:00', '05:30',
  '06:00', '06:30', '07:00', '07:30', '08:00', '08:30',
  '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
  '12:00', '12:30', '13:00', '13:30', '14:00', '14:30',
  '15:00', '15:30', '16:00', '16:30', '17:00', '17:30',
  '18:00', '18:30', '19:00', '19:30', '20:00', '20:30',
  '21:00', '21:30', '22:00', '22:30', '23:00', '23:30'),
  orari_chiusura ENUM(
  '00:00', '00:30', '01:00', '01:30', '02:00', '02:30',
  '03:00', '03:30', '04:00', '04:30', '05:00', '05:30',
  '06:00', '06:30', '07:00', '07:30', '08:00', '08:30',
  '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
  '12:00', '12:30', '13:00', '13:30', '14:00', '14:30',
  '15:00', '15:30', '16:00', '16:30', '17:00', '17:30',
  '18:00', '18:30', '19:00', '19:30', '20:00', '20:30',
  '21:00', '21:30', '22:00', '22:30', '23:00', '23:30'),
  descrizione TEXT,
  latitudine DECIMAL(10,7),
  longitudine DECIMAL(10,7),
  data_registrazione DATETIME DEFAULT CURRENT_TIMESTAMP,
  immagine VARCHAR(255) NOT NULL
);

CREATE TABLE oggetto_smarrito (
  id_oggetto INT AUTO_INCREMENT PRIMARY KEY,
  id_utente INT NOT NULL,
  id_drop_point INT,
  
  titolo VARCHAR(100) NOT NULL,
  descrizione TEXT NOT NULL,
  
  categoria ENUM('Documenti', 'Portafogli e borse', 'Abbigliamento', 'Gioielli e accessori', 'Chiavi', 'Elettronica', 'Altro'),
  marca VARCHAR(100),
  caratteristiche TEXT,
  
  domanda_verifica1 VARCHAR(255) NOT NULL,
  domanda_verifica2 VARCHAR(255) NOT NULL,
  domanda_verifica3 VARCHAR(255),
  data_ritrovamento DATE NOT NULL,
  luogo_ritrovamento VARCHAR(255) NOT NULL,
  citta VARCHAR(100) NOT NULL,
  provincia VARCHAR(100) NOT NULL,
  stato ENUM('disponibile', 'restituito') DEFAULT 'disponibile',
  data_pubblicazione DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  immagine VARCHAR(255) NOT NULL,
  
  FOREIGN KEY (id_utente) REFERENCES user(id_utente) ON DELETE CASCADE,
  FOREIGN KEY (id_drop_point) REFERENCES drop_point(id_drop_point) ON DELETE SET NULL
);

CREATE TABLE animale_smarrito (
  id_animale INT AUTO_INCREMENT PRIMARY KEY,
  id_utente INT NOT NULL,
  id_drop_point INT,
  
  nome VARCHAR(50),
  specie VARCHAR(50) NOT NULL,  -- cane, gatto, ecc.
  razza VARCHAR(50),
  colore VARCHAR(50),
  caratteristiche TEXT,
  
  domanda_verifica1 VARCHAR(255) NOT NULL,
  domanda_verifica2 VARCHAR(255) NOT NULL,
  domanda_verifica3 VARCHAR(255),
  data_ritrovamento DATE NOT NULL,
  luogo_ritrovamento VARCHAR(255) NOT NULL,
  citta VARCHAR(100) NOT NULL,
  provincia VARCHAR(100) NOT NULL,
  stato ENUM('disponibile', 'restituito') DEFAULT 'disponibile',
  data_pubblicazione DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  immagine VARCHAR(255),
  
  FOREIGN KEY (id_utente) REFERENCES user(id_utente) ON DELETE CASCADE,
  FOREIGN KEY (id_drop_point) REFERENCES drop_point(id_drop_point) ON DELETE SET NULL
);

CREATE TABLE reclamo (
  id_reclamo INT AUTO_INCREMENT PRIMARY KEY,

  id_utente_richiedente INT NOT NULL,
  id_oggetto INT,
  id_animale INT,

  risposta1 VARCHAR(255) NOT NULL,
  risposta2 VARCHAR(255) NOT NULL,
  risposta3 VARCHAR(255),

  messaggio TEXT,
  data_richiesta DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (id_utente_richiedente) REFERENCES user(id_utente) ON DELETE CASCADE,
  FOREIGN KEY (id_oggetto) REFERENCES oggetto_smarrito(id_oggetto) ON DELETE CASCADE,
  FOREIGN KEY (id_animale) REFERENCES animale_smarrito(id_animale) ON DELETE CASCADE
);

-- Fill the tables

INSERT INTO user (username, nome, cognome, email, passkey, citta, provincia, numero_telefono, punti, badge, ruolo, immagine_profilo)
VALUES
('maria.rossi', 'Maria', 'Rossi', 'maria.rossi@email.com', 'pass123', 'Milano', 'MI', '3456789012', 120, 'Indiana Jones', 'user', 'img/profili/maria.jpg'),
('luca.verdi', 'Luca', 'Verdi', 'luca.verdi@email.com', 'pass123', 'Roma', 'RM', '3332221111', 80, 'Lara Croft', 'user', 'img/profili/luca.jpg'),
('admin', 'Admin', 'System', 'admin@foundly.com', 'admin123', 'Torino', 'TO', '3200000000', 0, NULL, 'admin', 'img/profili/admin.jpg'),
('giulia.bianchi', 'Giulia', 'Bianchi', 'giulia.bianchi@email.com', 'pass123', 'Bologna', 'BO', '3471122334', 45, NULL, 'user', 'img/profili/giulia.jpg');

INSERT INTO drop_point (nome_attivita, indirizzo, citta, provincia, telefono, email_contatto, orari_apertura, orari_chiusura, descrizione, latitudine, longitudine, immagine)
VALUES
('Pet Shop Amici a 4 Zampe', 'Via Roma 42', 'Milano', 'MI', '0287654321', 'info@amicia4zampe.it', '09:00', '19:00', 'Punto di raccolta per oggetti e animali smarriti.', 45.464211, 9.191383, 'img/drop/petshop.jpg'),
('Libreria Il Segnalibro', 'Corso Garibaldi 15', 'Roma', 'RM', '066712345', 'contatti@ilsegnalibro.it', '10:00', '18:00', 'Accetta piccoli oggetti e documenti smarriti.', 41.902782, 12.496366, 'img/drop/libreria.jpg'),
('Bar Centrale', 'Piazza Maggiore 1', 'Bologna', 'BO', '051987654', 'barcentrale@gmail.com', '07:30', '22:00', 'Luogo di scambio sicuro per Foundly.', 44.493671, 11.343035, 'img/drop/bar.jpg');

INSERT INTO oggetto_smarrito (
  id_utente, id_drop_point, titolo, descrizione, categoria, marca, caratteristiche,
  domanda_verifica1, domanda_verifica2, domanda_verifica3,
  data_ritrovamento, luogo_ritrovamento, citta, provincia, stato, immagine
)
VALUES
(1, 1, 'Portafoglio in pelle nera', 'Portafoglio trovato vicino alla fermata Duomo. Contiene alcuni documenti.', 
 'Portafogli e borse', 'Gucci', 'Ha iniziali M.R. incise all’interno.', 
 'Di che colore è il portafoglio?', 'Quali iniziali sono incise?', 'Contiene contanti?', 
 '2025-10-01', 'Piazza Duomo', 'Milano', 'MI', 'disponibile', 'img/oggetti/portafoglio.jpg'),

(2, 2, 'Smartphone Samsung Galaxy', 'Telefono ritrovato su una panchina al parco.', 
 'Elettronica', 'Samsung', 'Schermo leggermente graffiato, cover blu.', 
 'Di che colore è la cover?', 'Qual è la marca?', 'Ha una pellicola sullo schermo?', 
 '2025-09-30', 'Villa Borghese', 'Roma', 'RM', 'disponibile', 'img/oggetti/telefono.jpg');

INSERT INTO animale_smarrito (
  id_utente, id_drop_point, nome, specie, razza, colore, caratteristiche,
  domanda_verifica1, domanda_verifica2, domanda_verifica3,
  data_ritrovamento, luogo_ritrovamento, citta, provincia, stato, immagine
)
VALUES
(1, 1, 'N/A', 'cane', 'meticcio', 'marrone', 'Taglia media, indossa collare rosso.',
 'Che colore ha il collare?', 'Ha il microchip?', 'È maschio o femmina?',
 '2025-10-03', 'Parco Sempione', 'Milano', 'MI', 'disponibile', 'img/animali/cane.jpg'),

(4, 3, 'N/A', 'gatto', 'europeo', 'grigio', 'Molto docile, aveva un collarino blu.',
 'Di che colore è il collarino?', 'È maschio o femmina?', 'Ha segni particolari?',
 '2025-10-05', 'Via Indipendenza', 'Bologna', 'BO', 'disponibile', 'img/animali/gatto.jpg');

INSERT INTO reclamo (
  id_utente_richiedente, id_oggetto, id_animale,
  risposta1, risposta2, risposta3, messaggio
)
VALUES
(4, 1, NULL, 'Nero', 'M.R.', 'Sì', 'È il mio portafoglio, l’ho perso in centro il 1 ottobre.'),
(2, NULL, 1, 'Rosso', 'Sì', 'Maschio', 'È il mio cane, ho perso il collare a Parco Sempione.');


-- Trigger

DELIMITER $$

CREATE TRIGGER assegna_badge_iniziale
BEFORE INSERT ON user
FOR EACH ROW
BEGIN
  IF NEW.punti >= 200 THEN
    SET NEW.badge = 'Sherlock Holmes';
  ELSEIF NEW.punti >= 100 THEN
    SET NEW.badge = 'Indiana Jones';
  ELSEIF NEW.punti >= 50 THEN
    SET NEW.badge = 'Lara Croft';
  ELSEIF NEW.punti = 10 THEN
    SET NEW.badge = 'Dora';
  ELSE
    SET NEW.badge = NULL;
  END IF;
END$$

DELIMITER ;