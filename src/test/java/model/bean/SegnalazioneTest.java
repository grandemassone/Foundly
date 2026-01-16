package model.bean;

import model.bean.enums.StatoSegnalazione;
import model.bean.enums.TipoSegnalazione;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class SegnalazioneTest {

    @Test
    void testGettersAndSetters() {
        // TRUCCO: Creiamo una istanza anonima perché la classe è abstract
        // Le parentesi {} creano al volo una sottoclasse concreta
        Segnalazione s = new Segnalazione() {};

        // 1. Preparazione dati
        long id = 50L;
        long idUtente = 99L;
        String titolo = "Portafoglio smarrito";
        String descrizione = "In pelle nera, marca X";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String luogo = "Parco Comunale";
        String citta = "Salerno";
        String provincia = "SA";
        Double lat = 40.68;
        Double lon = 14.76;

        byte[] img = {9, 8, 7}; // Fake image
        String contentType = "image/jpeg";

        String dom1 = "Di che colore è?";
        String dom2 = "Cosa c'è dentro?";

        // Prendo il primo valore degli Enum per sicurezza
        StatoSegnalazione stato = StatoSegnalazione.values()[0];
        TipoSegnalazione tipo = TipoSegnalazione.values()[0];

        // 2. Setters
        s.setId(id);
        s.setIdUtente(idUtente);
        s.setTitolo(titolo);
        s.setDescrizione(descrizione);
        s.setDataRitrovamento(now);
        s.setLuogoRitrovamento(luogo);
        s.setCitta(citta);
        s.setProvincia(provincia);
        s.setLatitudine(lat);
        s.setLongitudine(lon);
        s.setImmagine(img);
        s.setImmagineContentType(contentType);
        s.setDomandaVerifica1(dom1);
        s.setDomandaVerifica2(dom2);
        s.setDataPubblicazione(now);
        s.setStato(stato);
        s.setTipoSegnalazione(tipo);

        // 3. Getters & Assertions
        assertEquals(id, s.getId());
        assertEquals(idUtente, s.getIdUtente());
        assertEquals(titolo, s.getTitolo());
        assertEquals(descrizione, s.getDescrizione());
        assertEquals(now, s.getDataRitrovamento());
        assertEquals(luogo, s.getLuogoRitrovamento());
        assertEquals(citta, s.getCitta());
        assertEquals(provincia, s.getProvincia());
        assertEquals(lat, s.getLatitudine());
        assertEquals(lon, s.getLongitudine());

        assertArrayEquals(img, s.getImmagine());
        assertEquals(contentType, s.getImmagineContentType());

        assertEquals(dom1, s.getDomandaVerifica1());
        assertEquals(dom2, s.getDomandaVerifica2());
        assertEquals(now, s.getDataPubblicazione());
        assertEquals(stato, s.getStato());
        assertEquals(tipo, s.getTipoSegnalazione());
    }
}