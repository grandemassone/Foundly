package model.bean;

import model.bean.enums.StatoDropPoint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DropPointTest {

    @Test
    void testGettersAndSetters() {
        // 1. Creiamo l'oggetto vuoto
        DropPoint dp = new DropPoint();

        // 2. Prepariamo i dati di test
        Long id = 100L;
        String nome = "Bar dello Sport";
        String email = "bar@test.it";
        String pass = "hash12345";
        String indirizzo = "Via Roma 1";
        String citta = "Milano";
        String provincia = "MI";
        String telefono = "021234567";
        String orari = "08:00 - 20:00";
        String desc = "Punto di ritiro centrale";
        byte[] img = {1, 2, 3, 4}; // Un finto array di byte per l'immagine
        String imgType = "image/png";
        Double lat = 45.123;
        Double lon = 9.456;
        int ritiri = 50;

        // ATTENZIONE: Assumo che nel tuo Enum esista un valore chiamato ATTIVO o simile.
        // Se si chiama diversamente (es. DISPONIBILE, VERIFICATO), cambia 'values()[0]'
        // con il nome specifico, es: StatoDropPoint.ATTIVO
        StatoDropPoint stato = StatoDropPoint.values()[0];

        // 3. Usiamo i SETTERS
        dp.setId(id);
        dp.setNomeAttivita(nome);
        dp.setEmail(email);
        dp.setPasswordHash(pass);
        dp.setIndirizzo(indirizzo);
        dp.setCitta(citta);
        dp.setProvincia(provincia);
        dp.setTelefono(telefono);
        dp.setOrariApertura(orari);
        dp.setDescrizione(desc);
        dp.setImmagine(img);
        dp.setImmagineContentType(imgType);
        dp.setLatitudine(lat);
        dp.setLongitudine(lon);
        dp.setRitiriEffettuati(ritiri);
        dp.setStato(stato);

        // 4. Usiamo i GETTERS per verificare che i dati siano giusti
        assertEquals(id, dp.getId());
        assertEquals(nome, dp.getNomeAttivita());
        assertEquals(email, dp.getEmail());
        assertEquals(pass, dp.getPasswordHash());
        assertEquals(indirizzo, dp.getIndirizzo());
        assertEquals(citta, dp.getCitta());
        assertEquals(provincia, dp.getProvincia());
        assertEquals(telefono, dp.getTelefono());
        assertEquals(orari, dp.getOrariApertura());
        assertEquals(desc, dp.getDescrizione());

        // Per gli array si usa assertArrayEquals
        assertArrayEquals(img, dp.getImmagine());
        assertEquals(imgType, dp.getImmagineContentType());

        assertEquals(lat, dp.getLatitudine());
        assertEquals(lon, dp.getLongitudine());
        assertEquals(ritiri, dp.getRitiriEffettuati());
        assertEquals(stato, dp.getStato());
    }

    @Test
    void testCostruttoreVuoto() {
        DropPoint dp = new DropPoint();
        assertNotNull(dp);
    }
}