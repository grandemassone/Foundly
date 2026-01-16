package model.service;

import model.bean.Utente;
import model.bean.enums.Ruolo;
import model.dao.UtenteDAO;
import model.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UtenteServiceTest {

    private UtenteService service; // Niente @InjectMocks qui

    @Mock private UtenteDAO utenteDAO;

    private MockedStatic<PasswordUtils> passwordUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        passwordUtilsMock = mockStatic(PasswordUtils.class);

        // 1. Creiamo il service reale
        service = new UtenteService();

        // 2. FORZIAMO il mock dentro il campo privato
        injectMock(service, "utenteDAO", utenteDAO);
    }

    // Metodo helper per la Reflection
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
    void testRegistraUtente_Successo() {
        // Setup mocks
        when(utenteDAO.doRetrieveByEmail(anyString())).thenReturn(null);
        when(utenteDAO.doRetrieveByUsername(anyString())).thenReturn(null);
        when(utenteDAO.doSave(any(Utente.class))).thenReturn(true);

        passwordUtilsMock.when(() -> PasswordUtils.hashPassword(anyString())).thenReturn("hashedPass");

        boolean result = service.registraUtente("Mario", "Rossi", "mario99", "mario@email.it", "pass", "123");

        assertTrue(result);
    }

    @Test
    void testRegistraUtente_EmailEsistente() {
        when(utenteDAO.doRetrieveByEmail("mario@email.it")).thenReturn(new Utente());
        boolean result = service.registraUtente("Mario", "Rossi", "mario99", "mario@email.it", "pass", "123");
        assertFalse(result);
    }

    @Test
    void testLogin_Successo() {
        Utente u = new Utente();
        u.setPasswordHash("hash123");

        when(utenteDAO.doRetrieveByEmail("test@email.it")).thenReturn(u);
        passwordUtilsMock.when(() -> PasswordUtils.checkPassword("passwordVera", "hash123")).thenReturn(true);

        Utente result = service.login("test@email.it", "passwordVera");
        assertNotNull(result);
    }

    @Test
    void testLogin_Fallito() {
        Utente u = new Utente();
        u.setPasswordHash("hash123");

        when(utenteDAO.doRetrieveByEmail("test@email.it")).thenReturn(u);
        passwordUtilsMock.when(() -> PasswordUtils.checkPassword("passwordErrata", "hash123")).thenReturn(false);

        Utente result = service.login("test@email.it", "passwordErrata");
        assertNull(result);
    }

    @Test
    void testAggiornaPunteggioEBadge() {
        Utente u = new Utente();
        u.setId(1L);
        u.setPunteggio(99);
        u.setBadge("OCCHIO_DI_FALCO");

        when(utenteDAO.updatePunteggioEBadge(u)).thenReturn(true);

        boolean res = service.aggiornaPunteggioEBadge(u, 2);

        assertTrue(res);
        assertEquals(101, u.getPunteggio());
        assertEquals("DETECTIVE", u.getBadge());
    }

    @Test
    void testGetEmailAdmins() {
        Utente admin = new Utente();
        admin.setRuolo(Ruolo.ADMIN);
        admin.setEmail("admin@foundly.it");

        Utente user = new Utente();
        user.setRuolo(Ruolo.UTENTE_BASE);
        user.setEmail("user@foundly.it");

        // Dobbiamo restituire una lista modificabile o fissa
        List<Utente> lista = new ArrayList<>();
        lista.add(admin);
        lista.add(user);

        when(utenteDAO.doRetrieveAll()).thenReturn(lista);

        List<String> admins = service.getEmailAdmins();

        assertEquals(1, admins.size());
        assertEquals("admin@foundly.it", admins.get(0));
    }
}