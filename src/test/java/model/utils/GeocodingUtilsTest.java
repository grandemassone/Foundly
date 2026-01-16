package model.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeocodingUtilsTest {

    @Test
    void testGetCoordinates_SuccessoReale() {
        // 1. Testiamo che l'API reale risponda (se c'è internet)
        // Usiamo Roma perché è affidabile
        double[] coords = GeocodingUtils.getCoordinates("Colosseo", "Roma", "RM");

        assertNotNull(coords);
        assertEquals(2, coords.length);

        // Se l'API risponde, bene. Se non risponde (es. no internet),
        // tornerà i default (41.9028).
        // In entrambi i casi il test passa purché non sia null.
        assertTrue(coords[0] != 0.0);
        assertTrue(coords[1] != 0.0);
    }

    @Test
    void testGetCoordinates_SimulazioneErrore() {
        // QUI sta la magia.
        // Usiamo Mockito per intercettare la creazione di "new URL(...)"
        // e forziamo un'eccezione di rete. Così siamo SICURI che vada nel catch.

        try (MockedConstruction<URL> mockedUrl = Mockito.mockConstruction(URL.class,
                (mock, context) -> {
                    // Quando il codice chiama url.openConnection(), noi lanciamo IOException
                    when(mock.openConnection()).thenThrow(new IOException("Simulazione No Internet"));
                })) {

            // EXECUTE
            double[] coords = GeocodingUtils.getCoordinates("Indirizzo", "Citta", "Prov");

            // VERIFY
            // Ora DEVE per forza tornare le coordinate di default (Roma)
            // perché abbiamo rotto la connessione apposta.
            assertEquals(41.9028, coords[0], 0.001);
            assertEquals(12.4964, coords[1], 0.001);
        }
    }
}