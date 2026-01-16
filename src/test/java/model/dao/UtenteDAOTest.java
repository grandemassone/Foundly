package model.dao;

import model.ConPool;
import model.bean.Utente;
import model.bean.enums.Ruolo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UtenteDAOTest {

    private UtenteDAO dao;

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private MockedStatic<ConPool> mockedConPool;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dao = new UtenteDAO();

        // 1. Mock statico del ConPool
        mockedConPool = Mockito.mockStatic(ConPool.class);
        mockedConPool.when(ConPool::getConnection).thenReturn(connection);

        // 2. Configurazione base degli statement
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        mockedConPool.close();
    }

    // --- Helper per popolare il ResultSet (evita copia-incolla) ---
    private void setupResultSet() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Trova 1 riga, poi basta

        // Mapping dei campi
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("mario99");
        when(resultSet.getString("email")).thenReturn("mario@email.it");
        when(resultSet.getString("password_hash")).thenReturn("hash123");
        when(resultSet.getString("nome")).thenReturn("Mario");
        when(resultSet.getString("cognome")).thenReturn("Rossi");
        when(resultSet.getString("telefono")).thenReturn("3331234567");

        // Immagine
        when(resultSet.getBytes("immagine_profilo")).thenReturn(new byte[]{1, 2, 3});
        when(resultSet.getString("immagine_profilo_content_type")).thenReturn("image/jpeg");

        when(resultSet.getInt("punteggio")).thenReturn(100);
        when(resultSet.getString("ruolo")).thenReturn("UTENTE_BASE");
        when(resultSet.getString("badge")).thenReturn("DETECTIVE");
    }

    @Test
    void testDoSave_ConImmagine() throws SQLException {
        Utente u = new Utente();
        u.setUsername("luigi");
        u.setEmail("luigi@test.it");
        u.setImmagineProfilo(new byte[]{10, 20}); // Caso con immagine
        u.setImmagineProfiloContentType("image/png");
        u.setRuolo(Ruolo.UTENTE_BASE);
        u.setPunteggio(0);

        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Mock ID generato
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(55L);

        boolean result = dao.doSave(u);

        assertTrue(result);
        assertEquals(55L, u.getId());
        verify(preparedStatement).setBytes(eq(7), any()); // Verifica che setti l'immagine
    }

    @Test
    void testDoSave_SenzaImmagine() throws SQLException {
        Utente u = new Utente();
        u.setUsername("toad");
        u.setImmagineProfilo(null); // Caso senza immagine
        u.setRuolo(Ruolo.UTENTE_BASE);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(56L);

        boolean result = dao.doSave(u);

        assertTrue(result);
        verify(preparedStatement).setNull(7, Types.BLOB); // Verifica che setti NULL
    }

    @Test
    void testDoSave_Exception() throws SQLException {
        Utente u = new Utente();
        when(connection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException());

        boolean result = dao.doSave(u);
        assertFalse(result);
    }

    @Test
    void testDoRetrieveByEmail() throws SQLException {
        setupResultSet();

        Utente u = dao.doRetrieveByEmail("mario@email.it");

        assertNotNull(u);
        assertEquals("mario@email.it", u.getEmail());
        assertEquals(Ruolo.UTENTE_BASE, u.getRuolo());
    }

    @Test
    void testDoRetrieveByEmail_NotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Nessun risultato

        Utente u = dao.doRetrieveByEmail("inesistente@email.it");
        assertNull(u);
    }

    @Test
    void testDoRetrieveByUsername() throws SQLException {
        setupResultSet();
        Utente u = dao.doRetrieveByUsername("mario99");
        assertNotNull(u);
        assertEquals("mario99", u.getUsername());
    }

    @Test
    void testDoRetrieveById() throws SQLException {
        setupResultSet();
        Utente u = dao.doRetrieveById(1L);
        assertNotNull(u);
        assertEquals(1L, u.getId());
    }

    @Test
    void testDoRetrieveAllByPunteggio() throws SQLException {
        setupResultSet(); // Simula 1 utente trovato

        List<Utente> list = dao.doRetrieveAllByPunteggio();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void testDoRetrieveAll() throws SQLException {
        setupResultSet();
        List<Utente> list = dao.doRetrieveAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdateProfilo() throws SQLException {
        Utente u = new Utente();
        u.setId(1L);
        u.setUsername("NewUser");
        u.setImmagineProfilo(new byte[]{1}); // Test update con immagine

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dao.updateProfilo(u);
        assertTrue(result);
    }

    @Test
    void testUpdateProfilo_SenzaImmagine() throws SQLException {
        Utente u = new Utente();
        u.setId(1L);
        u.setImmagineProfilo(null); // Test update senza immagine

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dao.updateProfilo(u);
        assertTrue(result);
        verify(preparedStatement).setNull(4, Types.BLOB);
    }

    @Test
    void testUpdatePasswordByEmail() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = dao.updatePasswordByEmail("email@test.it", "newHash");
        assertTrue(result);
    }

    @Test
    void testUpdatePunteggioEBadge() throws SQLException {
        Utente u = new Utente();
        u.setId(1L);
        u.setPunteggio(500);
        u.setBadge("NUOVO_BADGE");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dao.updatePunteggioEBadge(u);
        assertTrue(result);
    }

    @Test
    void testDeleteById() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean result = dao.deleteById(1L);
        assertTrue(result);
    }
}