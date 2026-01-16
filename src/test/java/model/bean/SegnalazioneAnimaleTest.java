package model.bean;

import model.bean.enums.TipoSegnalazione;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SegnalazioneAnimaleTest {

    @Test
    void testCostruttoreEType() {
        // Verifica che il costruttore imposti automaticamente il TIPO corretto
        SegnalazioneAnimale sa = new SegnalazioneAnimale();
        assertEquals(TipoSegnalazione.ANIMALE, sa.getTipoSegnalazione());
    }

    @Test
    void testGettersAndSetters() {
        SegnalazioneAnimale sa = new SegnalazioneAnimale();

        String specie = "Cane";
        String razza = "Labrador";

        // Set
        sa.setSpecie(specie);
        sa.setRazza(razza);

        // Get & Assert
        assertEquals(specie, sa.getSpecie());
        assertEquals(razza, sa.getRazza());

        // Testiamo anche un metodo ereditato per essere sicuri
        sa.setId(10L);
        assertEquals(10L, sa.getId());
    }
}