package model.bean;

import model.bean.enums.CategoriaOggetto;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.TipoSegnalazione;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SegnalazioneOggettoTest {

    @Test
    void testCostruttoreEType() {
        // Verifica che il costruttore imposti automaticamente il TIPO corretto
        SegnalazioneOggetto so = new SegnalazioneOggetto();
        assertEquals(TipoSegnalazione.OGGETTO, so.getTipoSegnalazione());
    }

    @Test
    void testGettersAndSetters() {
        SegnalazioneOggetto so = new SegnalazioneOggetto();

        // Usiamo i valori degli Enum in modo sicuro
        CategoriaOggetto cat = CategoriaOggetto.values()[0];
        ModalitaConsegna mod = ModalitaConsegna.values()[0];
        Long idDropPoint = 55L;

        // Set
        so.setCategoria(cat);
        so.setModalitaConsegna(mod);
        so.setIdDropPoint(idDropPoint);

        // Get & Assert
        assertEquals(cat, so.getCategoria());
        assertEquals(mod, so.getModalitaConsegna());
        assertEquals(idDropPoint, so.getIdDropPoint());
    }
}