package model.dao;

import model.ConPool;
import model.bean.Segnalazione;
import model.bean.SegnalazioneAnimale;
import model.bean.SegnalazioneOggetto;
import model.bean.enums.CategoriaOggetto;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.StatoSegnalazione;
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

class SegnalazioneDAOTest {

    private SegnalazioneDAO dao;

    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private MockedStatic<ConPool> mockedConPool;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dao = new SegnalazioneDAO();

        mockedConPool = Mockito.mockStatic(ConPool.class);
        mockedConPool.when(ConPool::getConnection).thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        mockedConPool.close();
    }

    private void setupResultSet(String tipo) throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        when(resultSet.getLong("id")).thenReturn(100L);
        when(resultSet.getLong("id_utente")).thenReturn(1L);
        when(resultSet.getString("titolo")).thenReturn("Titolo Test");
        when(resultSet.getString("tipo_segnalazione")).thenReturn(tipo);
        when(resultSet.getString("stato")).thenReturn("APERTA");
        when(resultSet.getTimestamp(anyString())).thenReturn(new Timestamp(System.currentTimeMillis()));

        when(resultSet.getBytes("immagine")).thenReturn(new byte[]{1, 2, 3});
        when(resultSet.getString("immagine_content_type")).thenReturn("image/png");

        if ("OGGETTO".equals(tipo)) {
            when(resultSet.getString("categoria")).thenReturn("ELETTRONICA");
            when(resultSet.getString("modalita_consegna")).thenReturn("DIRETTA");
            when(resultSet.getObject("id_drop_point", Long.class)).thenReturn(null);
        } else {
            when(resultSet.getString("specie")).thenReturn("Cane");
            when(resultSet.getString("razza")).thenReturn("Labrador");
        }
    }

    @Test
    void testDoSave_Oggetto() throws SQLException {
        SegnalazioneOggetto so = new SegnalazioneOggetto();
        so.setIdUtente(1L);
        so.setTitolo("iPhone Perso");
        so.setCategoria(CategoriaOggetto.ELETTRONICA);
        so.setModalitaConsegna(ModalitaConsegna.DIRETTA);
        so.setImmagine(new byte[]{1});
        // FIX: Settiamo lo stato altrimenti esplode il NullPointer
        so.setStato(StatoSegnalazione.APERTA);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(50L);

        boolean result = dao.doSave(so);

        assertTrue(result);
        assertEquals(50L, so.getId());
        verify(connection).commit();
    }

    @Test
    void testDoSave_Animale() throws SQLException {
        SegnalazioneAnimale sa = new SegnalazioneAnimale();
        sa.setSpecie("Gatto");
        sa.setRazza("Persiano");
        // FIX: Settiamo lo stato
        sa.setStato(StatoSegnalazione.APERTA);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(51L);

        boolean result = dao.doSave(sa);

        assertTrue(result);
        verify(connection).commit();
    }

    @Test
    void testDoSave_Rollback() throws SQLException {
        SegnalazioneOggetto so = new SegnalazioneOggetto();
        // FIX: Anche qui serve lo stato, altrimenti crasha PRIMA di provare a salvare
        so.setStato(StatoSegnalazione.APERTA);

        when(preparedStatement.executeUpdate()).thenReturn(0); // Simuliamo fallimento

        boolean result = dao.doSave(so);

        assertFalse(result);
        verify(connection).rollback();
    }

    @Test
    void testUpdateStato() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.updateStato(100L, StatoSegnalazione.CHIUSA);
        assertTrue(res);
    }

    @Test
    void testDoRetrieveById_Oggetto() throws SQLException {
        setupResultSet("OGGETTO");
        Segnalazione result = dao.doRetrieveById(100L);
        assertNotNull(result);
        assertTrue(result instanceof SegnalazioneOggetto);
        assertEquals("ELETTRONICA", ((SegnalazioneOggetto) result).getCategoria().toString());
    }

    @Test
    void testDoRetrieveById_Animale() throws SQLException {
        setupResultSet("ANIMALE");
        Segnalazione result = dao.doRetrieveById(100L);
        assertNotNull(result);
        assertTrue(result instanceof SegnalazioneAnimale);
        assertEquals("Cane", ((SegnalazioneAnimale) result).getSpecie());
    }

    @Test
    void testDoRetrieveLatest() throws SQLException {
        setupResultSet("OGGETTO");
        List<Segnalazione> list = dao.doRetrieveLatest(5);
        assertFalse(list.isEmpty());
    }

    @Test
    void testDoRetrieveByUtente() throws SQLException {
        setupResultSet("ANIMALE");
        List<Segnalazione> list = dao.doRetrieveByUtente(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void testDoDelete() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.doDelete(100L);
        assertTrue(res);
        verify(connection).commit();
    }

    @Test
    void testDoRetrieveByFiltri_Pieno() throws SQLException {
        setupResultSet("OGGETTO");
        List<Segnalazione> list = dao.doRetrieveByFiltri("iphone", "OGGETTO", "ELETTRONICA");
        assertFalse(list.isEmpty());
    }

    @Test
    void testDoRetrieveByFiltri_Vuoto() throws SQLException {
        setupResultSet("OGGETTO");
        List<Segnalazione> list = dao.doRetrieveByFiltri(null, null, null);
        assertFalse(list.isEmpty());
    }

    @Test
    void testDoRetrieveAll() throws SQLException {
        setupResultSet("OGGETTO");
        List<Segnalazione> list = dao.doRetrieveAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testMapRowExceptionHandling() throws SQLException {
        setupResultSet("OGGETTO");
        when(resultSet.getString("categoria")).thenReturn("NON_ESISTE");
        Segnalazione s = dao.doRetrieveById(100L);
        assertNotNull(s);
        assertTrue(s instanceof SegnalazioneOggetto);
        assertNull(((SegnalazioneOggetto)s).getCategoria());
    }
}