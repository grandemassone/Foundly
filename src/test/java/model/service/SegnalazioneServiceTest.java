package model.service;

import model.bean.Reclamo;
import model.bean.SegnalazioneOggetto;
import model.bean.Utente;
import model.bean.enums.StatoSegnalazione;
import model.dao.ReclamoDAO;
import model.dao.SegnalazioneDAO;
import model.utils.GeocodingUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SegnalazioneServiceTest {

    private SegnalazioneService service;

    @Mock private SegnalazioneDAO segnalazioneDAO;
    @Mock private ReclamoDAO reclamoDAO;
    @Mock private UtenteService utenteService;

    private MockedStatic<GeocodingUtils> geoUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        geoUtilsMock = mockStatic(GeocodingUtils.class);

        service = new SegnalazioneService();
        injectMock(service, "segnalazioneDAO", segnalazioneDAO);
        injectMock(service, "reclamoDAO", reclamoDAO);
        injectMock(service, "utenteService", utenteService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @AfterEach
    void tearDown() {
        geoUtilsMock.close();
    }

    @Test
    void testCreaSegnalazione() {
        SegnalazioneOggetto s = new SegnalazioneOggetto();
        s.setLuogoRitrovamento("Via Roma");
        s.setCitta("Salerno");
        s.setProvincia("SA");

        geoUtilsMock.when(() -> GeocodingUtils.getCoordinates(anyString(), anyString(), anyString()))
                .thenReturn(new double[]{40.5, 14.5});

        when(segnalazioneDAO.doSave(s)).thenReturn(true);

        boolean result = service.creaSegnalazione(s);

        assertTrue(result);
        assertEquals(40.5, s.getLatitudine());
    }

    @Test
    void testGestisciConfermaScambio_Completo() {
        long idReclamo = 5L;
        long idSegnalazione = 50L;
        long idFinder = 99L; // ID fittizio del Finder

        // 1. Simuliamo l'azione di conferma
        when(reclamoDAO.confermaFinder(idReclamo)).thenReturn(true);

        // 2. Simuliamo che il reclamo ora abbia entrambe le spunte
        Reclamo r = new Reclamo();
        r.setId(idReclamo);
        r.setIdSegnalazione(idSegnalazione);
        r.setConfermaFinder(true);
        r.setConfermaOwner(true);
        when(reclamoDAO.doRetrieveById(idReclamo)).thenReturn(r);

        // 3. Simuliamo la segnalazione collegata al Finder
        SegnalazioneOggetto s = new SegnalazioneOggetto();
        s.setIdUtente(idFinder); // <--- FONDAMENTALE: Diciamo chi è il finder
        when(segnalazioneDAO.doRetrieveById(idSegnalazione)).thenReturn(s);

        // 4. Simuliamo che l'utente esista nel DB (altrimenti finder == null)
        Utente finder = new Utente();
        finder.setId(idFinder);
        when(utenteService.trovaPerId(idFinder)).thenReturn(finder); // <--- FONDAMENTALE

        // EXECUTE
        boolean res = service.gestisciConfermaScambio(idReclamo, true);

        // VERIFY
        assertTrue(res);
        verify(segnalazioneDAO).updateStato(idSegnalazione, StatoSegnalazione.CHIUSA);
        // Ora il finder viene trovato e il metodo viene chiamato!
        verify(utenteService).aggiornaPunteggioEBadge(eq(finder), eq(1));
    }

    @Test
    void testAccettaReclamoEChiudi() {
        when(reclamoDAO.accettaReclamo(1L, "CODE")).thenReturn(true);
        when(segnalazioneDAO.updateStato(2L, StatoSegnalazione.IN_CONSEGNA)).thenReturn(true);

        boolean res = service.accettaReclamoEChiudiSegnalazione(1L, 2L, "CODE");
        assertTrue(res);
    }
}