package model.service;

import model.bean.DropPoint;
import model.bean.Reclamo;
import model.bean.Segnalazione;
import model.bean.enums.StatoDropPoint;
import model.bean.enums.StatoSegnalazione;
import model.dao.DropPointDAO;
import model.dao.ReclamoDAO;
import model.dao.SegnalazioneDAO;
import model.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DropPointServiceTest {

    private DropPointService service;

    @Mock private DropPointDAO dropPointDAO;
    @Mock private ReclamoDAO reclamoDAO;
    @Mock private SegnalazioneDAO segnalazioneDAO;
    @Mock private UtenteService utenteService;
    @Mock private EmailService emailService;

    private MockedStatic<PasswordUtils> passwordUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        passwordUtilsMock = mockStatic(PasswordUtils.class);

        service = new DropPointService();

        // Reflection Injection: sovrascriviamo i campi privati
        injectMock(service, "dropPointDAO", dropPointDAO);
        injectMock(service, "reclamoDAO", reclamoDAO);
        injectMock(service, "segnalazioneDAO", segnalazioneDAO);
        injectMock(service, "utenteService", utenteService);
        injectMock(service, "emailService", emailService);
    }

    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @AfterEach
    void tearDown() {
        passwordUtilsMock.close();
    }

    @Test
    void testRegistraDropPoint_Success() {
        when(dropPointDAO.doRetrieveByEmail(anyString())).thenReturn(null);
        when(dropPointDAO.doSave(any())).thenReturn(true);
        when(utenteService.getEmailAdmins()).thenReturn(new ArrayList<>());
        passwordUtilsMock.when(() -> PasswordUtils.hashPassword(anyString())).thenReturn("hash");

        boolean res = service.registraDropPoint("Bar", "bar@email.it", "pass", "Via A", "NA", "NA", "123", "09-18", 10.0, 10.0);

        assertTrue(res);
    }

    @Test
    void testApprovaDropPoint() {
        when(dropPointDAO.updateStato(1L, StatoDropPoint.APPROVATO)).thenReturn(true);
        DropPoint dp = new DropPoint();
        dp.setEmail("dp@test.it");
        dp.setNomeAttivita("Bar");
        when(dropPointDAO.doRetrieveById(1L)).thenReturn(dp);

        boolean res = service.approvaDropPoint(1L);

        assertTrue(res);
        verify(emailService).inviaAccettazioneDropPoint("dp@test.it", "Bar");
    }

    @Test
    void testRegistraRitiro_Successo() {
        long idDP = 1L;
        String code = "123";

        Reclamo r = new Reclamo();
        r.setId(10L);
        r.setIdSegnalazione(20L);
        r.setDataDeposito(new Timestamp(System.currentTimeMillis()));
        r.setDataRitiro(null);

        when(reclamoDAO.doRetrieveByCodiceAndDropPoint(code, idDP)).thenReturn(r);
        when(reclamoDAO.marcaRitiro(10L)).thenReturn(true);
        when(segnalazioneDAO.updateStato(20L, StatoSegnalazione.CHIUSA)).thenReturn(true);
        // Creiamo una classe anonima o mock per Segnalazione
        Segnalazione s = mock(Segnalazione.class);
        when(s.getIdUtente()).thenReturn(5L);
        when(segnalazioneDAO.doRetrieveById(20L)).thenReturn(s);

        boolean res = service.registraRitiro(idDP, code);

        assertTrue(res);
    }

    @Test
    void testRegistraDeposito_Successo() {
        Reclamo r = new Reclamo();
        r.setId(10L);
        r.setDataDeposito(null);

        when(reclamoDAO.doRetrieveByCodiceAndDropPoint("CODE", 1L)).thenReturn(r);
        when(reclamoDAO.marcaDeposito(10L)).thenReturn(true);

        boolean res = service.registraDeposito(1L, "CODE");
        assertTrue(res);
    }
}