USE defaultdb;

-- ============================================
-- RESET (solo se vuoi ripetere i test)
-- ============================================
DELETE FROM reclamo;
DELETE FROM oggetto_smarrito;
DELETE FROM animale_smarrito;
DELETE FROM drop_point;
DELETE FROM `user`;

-- ============================================
-- 1) UTENTI
--    u1 = Finder, u2 = Owner, u3 = Admin, u4 = Account DP
-- ============================================
INSERT INTO `user`
(username, nome, cognome, email, passkey, citta, provincia, numero_telefono, ruolo, punti, badge)
VALUES
('finder1', 'Mario',  'Rossi',   'finder1@ex.com', 'hashpw', 'Milano', 'MI', '3331112222', 'user',       0, NULL),
('owner1',  'Lucia',  'Bianchi', 'owner1@ex.com',  'hashpw', 'Milano', 'MI', '3332221111', 'user',       0, NULL),
('admin1',  'Ada',    'Admin',   'admin@ex.com',   'hashpw', 'Torino', 'TO', '3330000000', 'admin',      0, NULL),
('dp1user', 'Luca',   'Verdi',   'dp1@ex.com',     'hashpw', 'Milano', 'MI', '3339998888', 'drop_point', 0, NULL);

-- ============================================
-- 2) DROP-POINT (APPROVATO) con responsabile u4
-- ============================================
INSERT INTO drop_point
(nome_attivita, indirizzo, citta, provincia, telefono, email_contatto,
 orari_apertura, orari_chiusura, descrizione, latitudine, longitudine,
 immagine, stato, id_utente_responsabile)
VALUES
('Cartoleria Centrale', 'Via Roma 10', 'Milano', 'MI', '0287654321', 'contatti@cartocentrale.it',
 '09:00', '19:00', 'Luogo sicuro per Foundly', 45.4642, 9.1914,
 'img/drop/cartoleria.jpg', 'approvato', 4);

SET @DP := (SELECT id_drop_point FROM drop_point WHERE nome_attivita='Cartoleria Centrale');

-- ============================================
-- 3) SEGNALAZIONI (Finder = u1)
--    - O1: Oggetto (userà flusso Drop-Point)
--    - A1: Animale (userà flusso Scambio diretto)
-- ============================================
INSERT INTO oggetto_smarrito
(id_utente, id_drop_point, titolo, descrizione, categoria, marca, caratteristiche,
 domanda_verifica1, domanda_verifica2, domanda_verifica3,
 data_ritrovamento, luogo_ritrovamento, citta, provincia, stato, immagine)
VALUES
(1, @DP, 'Portafoglio nero', 'Trovato vicino a Piazza Duomo.', 'Portafogli e borse', '—', 'Iniziali M.R.',
 'Di che colore è?', 'Quali iniziali sono incise?', 'Contiene contanti?',
 '2025-10-01', 'Piazza Duomo', 'Milano', 'MI', 'aperta', 'img/obj/portafoglio.jpg');

INSERT INTO animale_smarrito
(id_utente, id_drop_point, nome, specie, razza, colore, caratteristiche,
 domanda_verifica1, domanda_verifica2, domanda_verifica3,
 data_ritrovamento, luogo_ritrovamento, citta, provincia, stato, immagine)
VALUES
(1, NULL, '—', 'cane', 'meticcio', 'marrone', 'Collare rosso.',
 'Che colore ha il collare?', 'Ha il microchip?', 'Sesso?',
 '2025-10-02', 'Parco Sempione', 'Milano', 'MI', 'aperta', 'img/an/cane.jpg');

SET @OGG := (SELECT id_oggetto FROM oggetto_smarrito WHERE titolo='Portafoglio nero');
SET @ANI := (SELECT id_animale FROM animale_smarrito WHERE luogo_ritrovamento='Parco Sempione');

-- ============================================
-- 4) CLAIM (Owner = u2)
--    C1 per O1 (oggetto) -> DP
--    C2 per A1 (animale) -> Diretto
-- ============================================
INSERT INTO reclamo
(id_utente_richiedente, id_oggetto, id_animale,
 risposta1, risposta2, risposta3, messaggio, stato)
VALUES
(2, @OGG, NULL, 'Nero', 'M.R.', 'No', 'È il mio portafoglio.', 'inviato'),
(2, NULL, @ANI, 'Rosso', 'Sì', 'Maschio', 'È il mio cane.',    'inviato');

SET @C1 := (SELECT id_reclamo FROM reclamo WHERE id_oggetto=@OGG);
SET @C2 := (SELECT id_reclamo FROM reclamo WHERE id_animale=@ANI);

-- ============================================
-- 5) ACCETTAZIONE CLAIM (logica applicativa)
--    Genero in CODICE (6 cifre) + scadenza + stato
-- ============================================
-- C1: accettato con codice 123456
UPDATE reclamo
   SET stato='accettato',
       codice_consegna='123456',
       codice_scadenza=DATE_ADD(NOW(), INTERVAL 14 DAY),
       codice_stato='valido'
 WHERE id_reclamo=@C1;

-- C2: accettato con codice 654321
UPDATE reclamo
   SET stato='accettato',
       codice_consegna='654321',
       codice_scadenza=DATE_ADD(NOW(), INTERVAL 14 DAY),
       codice_stato='valido'
 WHERE id_reclamo=@C2;

-- Verifica
SELECT id_reclamo, stato, codice_consegna, codice_stato FROM reclamo WHERE id_reclamo IN (@C1,@C2);

-- ============================================
-- 6-A) FLUSSO CON DROP-POINT (C1, oggetto)
--    DP registra DEPOSITO poi RITIRO con lo stesso codice
--    (in app: farlo in UNICA transazione con i controlli)
-- ============================================
-- deposito
UPDATE reclamo
   SET dp_deposito_at = NOW(),
       dp_deposito_id = @DP
 WHERE id_reclamo=@C1
   AND codice_consegna='123456'
   AND codice_stato='valido'
   AND dp_deposito_at IS NULL;

-- ritiro + chiusura applicativa
UPDATE reclamo
   SET dp_ritiro_at = NOW(),
       dp_ritiro_id = @DP,
       codice_stato = 'usato'
 WHERE id_reclamo=@C1
   AND codice_consegna='123456'
   AND codice_stato='valido'
   AND dp_deposito_at IS NOT NULL
   AND dp_ritiro_at IS NULL;

-- chiudi la segnalazione oggetto e assegna punti al finder (u1)
UPDATE oggetto_smarrito
   SET stato='chiusa'
 WHERE id_oggetto=@OGG
   AND stato <> 'chiusa';

UPDATE `user`
   SET punti = punti + 1
 WHERE id_utente = (SELECT id_utente FROM oggetto_smarrito WHERE id_oggetto=@OGG);

-- Check
SELECT stato FROM oggetto_smarrito WHERE id_oggetto=@OGG;         -- atteso 'chiusa'
SELECT punti FROM `user` WHERE id_utente=1;                        -- atteso 1

-- ============================================
-- 6-B) FLUSSO SCAMBIO DIRETTO (C2, animale)
--    Finder e Owner confermano entrambi con lo stesso codice
--    (in app: sempre in UNICA transazione con controlli)
-- ============================================
-- conferma finder
UPDATE reclamo
   SET finder_conferma_at = NOW()
 WHERE id_reclamo=@C2
   AND codice_consegna='654321'
   AND codice_stato='valido'
   AND finder_conferma_at IS NULL;

-- conferma owner + chiusura applicativa
UPDATE reclamo
   SET owner_conferma_at = NOW(),
       codice_stato = 'usato'
 WHERE id_reclamo=@C2
   AND codice_consegna='654321'
   AND codice_stato='valido'
   AND owner_conferma_at IS NULL
   AND finder_conferma_at IS NOT NULL;

-- chiudi la segnalazione animale e assegna punti al finder (u1)
UPDATE animale_smarrito
   SET stato='chiusa'
 WHERE id_animale=@ANI
   AND stato <> 'chiusa';

UPDATE `user`
   SET punti = punti + 1
 WHERE id_utente = (SELECT id_utente FROM animale_smarrito WHERE id_animale=@ANI);

-- Check
SELECT stato FROM animale_smarrito WHERE id_animale=@ANI;          -- atteso 'chiusa'
SELECT punti FROM `user` WHERE id_utente=1;                        -- atteso 2

-- ============================================
-- 7) VERIFICHE FINALI
-- ============================================
-- Codici marcati 'usato'
SELECT id_reclamo, codice_consegna, codice_stato FROM reclamo WHERE id_reclamo IN (@C1,@C2);

-- Eventi DP solo per C1
SELECT id_reclamo, dp_deposito_at, dp_deposito_id, dp_ritiro_at, dp_ritiro_id FROM reclamo WHERE id_reclamo=@C1;
SELECT id_reclamo, finder_conferma_at, owner_conferma_at FROM reclamo WHERE id_reclamo=@C2;

-- Scoreboard del finder
SELECT username, punti, badge FROM `user` WHERE id_utente=1;
