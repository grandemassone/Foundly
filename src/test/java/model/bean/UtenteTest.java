package model.bean;

import model.bean.enums.Ruolo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtenteTest {

    @Test
    void testGettersAndSetters() {
        Utente u = new Utente();

        // Dati di test
        long id = 123L;
        String user = "supermario";
        String email = "mario@email.it";
        String pass = "hashSegreto";
        String nome = "Mario";
        String cognome = "Rossi";
        String tel = "3331234567";
        int punti = 100;
        String badge = "Veterano";

        byte[] img = {5, 6, 7};
        String contentType = "image/png";

        // Prendo il primo ruolo disponibile
        Ruolo ruolo = Ruolo.values()[0];

        // Setters
        u.setId(id);
        u.setUsername(user);
        u.setEmail(email);
        u.setPasswordHash(pass);
        u.setNome(nome);
        u.setCognome(cognome);
        u.setTelefono(tel);
        u.setPunteggio(punti);
        u.setBadge(badge);
        u.setRuolo(ruolo);
        u.setImmagineProfilo(img);
        u.setImmagineProfiloContentType(contentType);

        // Getters & Asserts
        assertEquals(id, u.getId());
        assertEquals(user, u.getUsername());
        assertEquals(email, u.getEmail());
        assertEquals(pass, u.getPasswordHash());
        assertEquals(nome, u.getNome());
        assertEquals(cognome, u.getCognome());
        assertEquals(tel, u.getTelefono());
        assertEquals(punti, u.getPunteggio());
        assertEquals(badge, u.getBadge());
        assertEquals(ruolo, u.getRuolo());

        assertArrayEquals(img, u.getImmagineProfilo());
        assertEquals(contentType, u.getImmagineProfiloContentType());
    }
}