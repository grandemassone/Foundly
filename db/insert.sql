USE defaultdb;

-- =========================================================
-- 0) PULIZIA (opzionale se ri-esegui più volte i test)
-- =========================================================
DELETE FROM reclamo;
DELETE FROM oggetto_smarrito;
DELETE FROM animale_smarrito;
DELETE FROM drop_point;
DELETE FROM `user`;

-- =========================================================
-- 1) UTENTI
--    - u1 = Finder
--    - u2 = Owner (reclamante)
--    - u3 = Admin
--    - u4 = Responsabile Drop-Point (ruolo drop_point)
-- =========================================================
INSERT INTO `user` (username, nome, cognome, email, passkey, citta, provincia, numero_telefono, ruolo, punti)
VALUES
('finder1', 'Mario',  'Rossi',   'finder1@ex.com', 'hashpw', 'Milano',  'MI', '3331112222', 'user', 0),
('owner1',  'Lucia',  'Bianchi', 'owner1@ex.com',  'hashpw', 'Milano',  'MI', '3332221111', 'user', 0),
('admin1',  'Ada',    'Admin',   'admin@ex.com',   'hashpw', 'Torino',  'TO', '3330000000', 'admin', 0),
('dpuser1', 'Luca',   'Verdi',   'dp1@ex.com',     'hashpw', 'Milano',  'MI', '3339998888', 'drop_point', 0);

-- =========================================================
-- 2) DROP-POINT (approvato) con responsabile u4
-- =========================================================
INSERT INTO drop_point (
  nome_attivita, indirizzo, citta, provincia, telefono, email_contatto,
  orari_apertura, orari_chiusura, descrizione, latitudine, longitudine,
  immagine, stato, id_utente_responsabile
)
VALUES
('Cartoleria Centrale', 'Via Roma 10', 'Milano', 'MI', '0287654321', 'contatti@cartocentrale.it',
 '09:00', '19:00', 'Luogo sicuro per consegna/ritiro Foundly', 45.4642, 9.1914,
 'img/drop/cartoleria.jpg', 'approvato', 4);

-- Teniamo l'id in una variabile
SET @DP := (SELECT id_drop_point FROM drop_point WHERE nome_attivita='Cartoleria Centrale');

-- =========================================================
-- 3) SEGNALAZIONI (Finder = u1)
--    - una di tipo OGGETTO
--    - una di tipo ANIMALE
-- =========================================================
INSERT INTO oggetto_smarrito (
  id_utente, id_drop_point, titolo, descrizione, categoria, marca, caratteristiche,
  domanda_verifica1, domanda_verifica2, domanda_verifica3,
  data_ritrovamento, luogo_ritrovamento, citta, provincia, immagine
) VALUES
(1, @DP, 'Portafoglio nero', 'Trovato vicino a Piazza Duomo.', 'Portafogli e borse', '—',
 'Iniziali M.R. all\'interno.',
 'Di che colore è?', 'Quali iniziali sono incise?', 'Contiene contanti?',
 '2025-10-01', 'Piazza Duomo', 'Milano', 'MI', 'img/obj/portafoglio.jpg');

INSERT INTO animale_smarrito (
  id_utente, id_drop_point, nome, specie, razza, colore, caratteristiche,
  domanda_verifica1, domanda_verifica2, domanda_verifica3,
  data_ritrovamento, luogo_ritrovamento, citta, provincia, immagine
) VALUES
(1, NULL, '—', 'cane', 'meticcio', 'marrone', 'Collare rosso.',
 'Che colore ha il collare?', 'Ha il microchip?', 'Sesso?',
 '2025-10-02', 'Parco Sempione', 'Milano', 'MI', 'img/an/cane.jpg');

SET @OBJ := LAST_INSERT_ID();                          -- id_animale appena inserito? NO: il LAST_INSERT_ID ora è dell'animale
-- Recupero esplicito gli ID
SET @OGG := (SELECT id_oggetto FROM oggetto_smarrito WHERE titolo='Portafoglio nero');
SET @ANI := (SELECT id_animale FROM animale_smarrito WHERE luogo_ritrovamento='Parco Sempione');

-- =========================================================
-- 4) RECLAMI (Owner = u2)
--    C1) Claim per OGGETTO  -> useremo il flusso ***Drop-Point***
--    C2) Claim per ANIMALE  -> useremo il flusso ***Diretto***
-- =========================================================
INSERT INTO reclamo (
  id_utente_richiedente, id_oggetto, id_animale,
  risposta1, risposta2, risposta3, messaggio
) VALUES
(2, @OGG, NULL, 'Nero', 'M.R.', 'No', 'È il mio portafoglio.'),          -- C1
(2, NULL, @ANI, 'Rosso', 'Sì', 'Maschio', 'È il mio cane.');             -- C2

SET @C1 := (SELECT id_reclamo FROM reclamo WHERE id_oggetto=@OGG);
SET @C2 := (SELECT id_reclamo FROM reclamo WHERE id_animale=@ANI);

-- =========================================================
-- 5) ACCETTAZIONE CLAIM -> il trigger genera il CODICE (6 cifre)
-- =========================================================
UPDATE reclamo SET stato='accettato' WHERE id_reclamo=@C1;
UPDATE reclamo SET stato='accettato' WHERE id_reclamo=@C2;

-- Leggo i codici generati
SET @CODE1 := (SELECT codice_consegna FROM reclamo WHERE id_reclamo=@C1);
SET @CODE2 := (SELECT codice_consegna FROM reclamo WHERE id_reclamo=@C2);

-- Verifica visiva
SELECT @C1 AS claim_oggetto, @CODE1 AS codice_oggetto;
SELECT @C2 AS claim_animale, @CODE2 AS codice_animale;

-- =========================================================
-- 6-A) CHIUSURA ***VIA DROP-POINT*** per C1 (oggetto)
--      L'operatore DP conferma prima DEPOSITO, poi RITIRO (stesso codice)
-- =========================================================
UPDATE reclamo
   SET dp_deposito_at = NOW(),
       dp_deposito_id = @DP
 WHERE id_reclamo = @C1
   AND codice_consegna = @CODE1;

UPDATE reclamo
   SET dp_ritiro_at = NOW(),
       dp_ritiro_id = @DP
 WHERE id_reclamo = @C1
   AND codice_consegna = @CODE1;

-- Il trigger AFTER UPDATE chiude la segnalazione oggetto e assegna +1 punto al finder
SELECT stato FROM oggetto_smarrito WHERE id_oggetto=@OGG;     -- atteso: 'chiusa'
SELECT punti, badge FROM `user` WHERE id_utente=1;            -- punti: 1, badge coerente

-- =========================================================
-- 6-B) CHIUSURA ***SCAMBIO DIRETTO*** per C2 (animale)
--      Finder e Owner confermano entrambi inserendo lo stesso codice
-- =========================================================
UPDATE reclamo
   SET finder_conferma_at = NOW()
 WHERE id_reclamo = @C2
   AND codice_consegna = @CODE2;

UPDATE reclamo
   SET owner_conferma_at = NOW()
 WHERE id_reclamo = @C2
   AND codice_consegna = @CODE2;

-- Anche qui il trigger chiude la segnalazione animale e assegna +1 punto
SELECT stato FROM animale_smarrito WHERE id_animale=@ANI;     -- atteso: 'chiusa'
SELECT punti, badge FROM `user` WHERE id_utente=1;            -- punti: 2 totali ora

-- =========================================================
-- 7) CONTROLLI FINALI: integrità e storico
-- =========================================================
-- Codici marcati come 'usato'
SELECT id_reclamo, codice_consegna, codice_stato FROM reclamo WHERE id_reclamo IN (@C1,@C2);

-- Eventi DP registrati per C1 (deposito + ritiro) e nessun evento DP per C2
SELECT id_reclamo, dp_deposito_at, dp_deposito_id, dp_ritiro_at, dp_ritiro_id FROM reclamo WHERE id_reclamo=@C1;
SELECT id_reclamo, dp_deposito_at, dp_deposito_id, dp_ritiro_at, dp_ritiro_id FROM reclamo WHERE id_reclamo=@C2;

-- Storico “scoreboard” del finder (u1)
SELECT username, punti, badge FROM `user` WHERE id_utente=1;

-- Drop-Point solo se 'approvato'
SELECT id_drop_point, nome_attivita, stato FROM drop_point;
