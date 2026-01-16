package model.bean;

import model.bean.enums.StatoReclamo;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ReclamoTest {

    @Test
    void testGettersAndSetters() {
        // 1. Creazione oggetto
        Reclamo reclamo = new Reclamo();

        // 2. Preparazione dati
        long id = 1L;
        long idSegnalazione = 10L;
        long idUtenteRichiedente = 5L;
        String risposta1 = "Ho perso le chiavi";
        String risposta2 = "Erano rosse";

        // Creiamo un timestamp attuale
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Prendiamo il primo valore dell'Enum per sicurezza
        StatoReclamo stato = StatoReclamo.values()[0];

        String codiceConsegna = "CODE-123";
        boolean confermaFinder = true;
        boolean confermaOwner = false;

        String titolo = "Mazzo di chiavi";
        byte[] img = {10, 20, 30}; // Fake image bytes
        String contentType = "image/png";

        // 3. Esecuzione dei SETTER
        reclamo.setId(id);
        reclamo.setIdSegnalazione(idSegnalazione);
        reclamo.setIdUtenteRichiedente(idUtenteRichiedente);
        reclamo.setRispostaVerifica1(risposta1);
        reclamo.setRispostaVerifica2(risposta2);
        reclamo.setDataRichiesta(now);
        reclamo.setStato(stato);

        reclamo.setCodiceConsegna(codiceConsegna);
        reclamo.setDataDeposito(now); // Uso lo stesso timestamp per comodità
        reclamo.setDataRitiro(now);
        reclamo.setConfermaFinder(confermaFinder);
        reclamo.setConfermaOwner(confermaOwner);

        reclamo.setTitoloSegnalazione(titolo);
        reclamo.setImmagineSegnalazione(img);
        reclamo.setImmagineSegnalazioneContentType(contentType);

        // 4. Verifica con i GETTER
        assertEquals(id, reclamo.getId());
        assertEquals(idSegnalazione, reclamo.getIdSegnalazione());
        assertEquals(idUtenteRichiedente, reclamo.getIdUtenteRichiedente());
        assertEquals(risposta1, reclamo.getRispostaVerifica1());
        assertEquals(risposta2, reclamo.getRispostaVerifica2());
        assertEquals(now, reclamo.getDataRichiesta());
        assertEquals(stato, reclamo.getStato());

        assertEquals(codiceConsegna, reclamo.getCodiceConsegna());
        assertEquals(now, reclamo.getDataDeposito());
        assertEquals(now, reclamo.getDataRitiro());

        // Per i boolean verifica che il metodo si chiami 'is...' o 'get...' come nel Bean
        assertTrue(reclamo.isConfermaFinder());
        assertFalse(reclamo.isConfermaOwner());

        assertEquals(titolo, reclamo.getTitoloSegnalazione());
        assertArrayEquals(img, reclamo.getImmagineSegnalazione());
        assertEquals(contentType, reclamo.getImmagineSegnalazioneContentType());
    }

    @Test
    void testCostruttore() {
        Reclamo r = new Reclamo();
        assertNotNull(r);
    }
}