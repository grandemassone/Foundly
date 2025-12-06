-- Cancellare dati da tabella utente
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE utente;
SET FOREIGN_KEY_CHECKS = 1;

-- Cancellare dati da tabella drop-point
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE drop_point;
SET FOREIGN_KEY_CHECKS = 1;