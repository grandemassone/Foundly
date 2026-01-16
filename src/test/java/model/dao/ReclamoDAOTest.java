package model.dao;

import model.ConPool;
import model.bean.Reclamo;
import model.bean.enums.StatoReclamo;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReclamoDAOTest {

    private ReclamoDAO dao;

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private MockedStatic<ConPool> mockedConPool;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dao = new ReclamoDAO();

        // Mock statico del ConPool
        mockedConPool = Mockito.mockStatic(ConPool.class);
        mockedConPool.when(ConPool::getConnection).thenReturn(connection);

        // Quando viene chiesto un PreparedStatement, restituisci il mock
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        mockedConPool.close();
    }

    // Helper per configurare un ResultSet "standard" ed evitare ripetizioni
    private void setupResultSetStandard() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getLong("id_segnalazione")).thenReturn(10L);
        when(resultSet.getLong("id_utente_richiedente")).thenReturn(5L);
        when(resultSet.getString("risposta_verifica1")).thenReturn("Risp1");
        when(resultSet.getString("risposta_verifica2")).thenReturn("Risp2");
        when(resultSet.getString("stato")).thenReturn("IN_ATTESA");
        when(resultSet.getString("codice_consegna")).thenReturn("ABC-123");
        // Timestamp
        when(resultSet.getTimestamp(anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    void testDoSave() throws SQLException {
        Reclamo r = new Reclamo();
        r.setIdSegnalazione(10L);
        r.setIdUtenteRichiedente(5L);
        r.setRispostaVerifica1("Risp1");
        r.setRispostaVerifica2("Risp2");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dao.doSave(r);

        assertTrue(result);
        verify(preparedStatement).setLong(1, 10L);
        verify(preparedStatement).setString(5, "IN_ATTESA"); // Verifica lo stato di default
    }

    @Test
    void testConfermaFinder() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.confermaFinder(1L);
        assertTrue(res);
    }

    @Test
    void testConfermaOwner() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.confermaOwner(1L);
        assertTrue(res);
    }

    @Test
    void testDoRetrieveByCodice() throws SQLException {
        setupResultSetStandard(); // Configura il resultset che trova qualcosa

        Reclamo r = dao.doRetrieveByCodice("ABC-123");

        assertNotNull(r);
        assertEquals("ABC-123", r.getCodiceConsegna());
    }

    @Test
    void testDoRetrieveBySegnalazione() throws SQLException {
        setupResultSetStandard();
        when(resultSet.next()).thenReturn(true, false); // Trova 1 elemento poi basta

        List<Reclamo> list = dao.doRetrieveBySegnalazione(10L);

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void testDoRetrieveBySegnalazioneAndUtente() throws SQLException {
        setupResultSetStandard();

        Reclamo r = dao.doRetrieveBySegnalazioneAndUtente(10L, 5L);
        assertNotNull(r);
    }

    @Test
    void testAccettaReclamo() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean res = dao.accettaReclamo(1L, "NEW-CODE");

        assertTrue(res);
        verify(preparedStatement).setString(1, "NEW-CODE");
    }

    @Test
    void testRifiutaReclamo() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.rifiutaReclamo(1L);
        assertTrue(res);
    }

    @Test
    void testDoRetrieveById() throws SQLException {
        setupResultSetStandard();
        Reclamo r = dao.doRetrieveById(1L);
        assertNotNull(r);
        assertEquals(1L, r.getId());
    }

    @Test
    void testDoRetrieveByRichiedente_ConImmagine() throws SQLException {
        // Questo test è importante perché c'è una JOIN e logica extra
        setupResultSetStandard();
        when(resultSet.next()).thenReturn(true, false);

        // Mock dei campi extra della JOIN
        when(resultSet.getString("seg_titolo")).thenReturn("Titolo Segnalazione");
        when(resultSet.getString("seg_immagine")).thenReturn("path/to/img");
        // Nota: nel tuo codice fai getBytes() su una stringa path,
        // simuliamo che non esploda anche se qui è un mock

        List<Reclamo> list = dao.doRetrieveByRichiedente(5L);

        assertFalse(list.isEmpty());
        assertEquals("Titolo Segnalazione", list.get(0).getTitoloSegnalazione());
    }

    @Test
    void testDoRetrieveByCodiceAndDropPoint() throws SQLException {
        setupResultSetStandard();
        Reclamo r = dao.doRetrieveByCodiceAndDropPoint("CODE", 100L);
        assertNotNull(r);
    }

    @Test
    void testMarcaDeposito() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.marcaDeposito(1L);
        assertTrue(res);
    }

    @Test
    void testMarcaRitiro() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.marcaRitiro(1L);
        assertTrue(res);
    }

    @Test
    void testSetDataDepositoIfNull() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.setDataDepositoIfNull(1L);
        assertTrue(res);
    }

    @Test
    void testCountDepositiAttivi() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("cnt")).thenReturn(5);

        int count = dao.countDepositiAttiviByDropPoint(50L);
        assertEquals(5, count);
    }

    @Test
    void testCountConsegneCompletate() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("cnt")).thenReturn(10);

        int count = dao.countConsegneCompletateByDropPoint(50L);
        assertEquals(10, count);
    }

    // Test per coprire il caso Exception nel mapping
    @Test
    void testMapRowException() throws SQLException {
        setupResultSetStandard();
        // Simuliamo uno stato non valido per forzare l'eccezione nel mapRow
        when(resultSet.getString("stato")).thenReturn("STATO_INESISTENTE");

        Reclamo r = dao.doRetrieveById(1L);

        // Deve tornare IN_ATTESA perché c'è il catch(Exception) che fa fallback
        assertEquals(StatoReclamo.IN_ATTESA, r.getStato());
    }
}