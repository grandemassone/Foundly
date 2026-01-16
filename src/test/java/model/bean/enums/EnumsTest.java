package model.bean.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void testBadge() {
        for (Badge e : Badge.values()) {
            assertNotNull(e);
            assertEquals(e, Badge.valueOf(e.name()));
        }
    }

    @Test
    void testCategoriaOggetto() {
        for (CategoriaOggetto e : CategoriaOggetto.values()) {
            assertNotNull(e);
            assertEquals(e, CategoriaOggetto.valueOf(e.name()));
        }
    }

    @Test
    void testModalitaConsegna() {
        for (ModalitaConsegna e : ModalitaConsegna.values()) {
            assertNotNull(e);
            assertEquals(e, ModalitaConsegna.valueOf(e.name()));
        }
    }

    @Test
    void testRuolo() {
        for (Ruolo e : Ruolo.values()) {
            assertNotNull(e);
            assertEquals(e, Ruolo.valueOf(e.name()));
        }
    }

    @Test
    void testStatoDropPoint() {
        for (StatoDropPoint e : StatoDropPoint.values()) {
            assertNotNull(e);
            assertEquals(e, StatoDropPoint.valueOf(e.name()));
        }
    }

    @Test
    void testStatoReclamo() {
        for (StatoReclamo e : StatoReclamo.values()) {
            assertNotNull(e);
            assertEquals(e, StatoReclamo.valueOf(e.name()));
        }
    }

    @Test
    void testStatoSegnalazione() {
        for (StatoSegnalazione e : StatoSegnalazione.values()) {
            assertNotNull(e);
            assertEquals(e, StatoSegnalazione.valueOf(e.name()));
        }
    }

    @Test
    void testTipoSegnalazione() {
        for (TipoSegnalazione e : TipoSegnalazione.values()) {
            assertNotNull(e);
            assertEquals(e, TipoSegnalazione.valueOf(e.name()));
        }
    }
}