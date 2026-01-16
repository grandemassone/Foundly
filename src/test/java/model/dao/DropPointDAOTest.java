package model.dao;

import model.ConPool;
import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;
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

class DropPointDAOTest {

    private DropPointDAO dao;

    // Mocks per simulare il Database
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    // Questo serve per mockare la classe statica ConPool
    private MockedStatic<ConPool> mockedConPool;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dao = new DropPointDAO();

        // 1. Blocchiamo la classe ConPool in modo che non cerchi il DB vero
        mockedConPool = Mockito.mockStatic(ConPool.class);
        mockedConPool.when(ConPool::getConnection).thenReturn(connection);

        // 2. Quando il DAO chiede lo statement, diamogli quello finto
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
    }

    @AfterEach
    void tearDown() {
        // IMPORTANTE: Chiudere il mock statico alla fine di ogni test
        mockedConPool.close();
    }

    @Test
    void testDoSave() throws SQLException {
        DropPoint dp = new DropPoint();
        dp.setNomeAttivita("Bar Prova");
        dp.setImmagine(new byte[]{1, 2, 3}); // Testiamo anche il BLOB
        dp.setStato(StatoDropPoint.IN_ATTESA);

        // Simuliamo che l'update restituisca 1 (successo)
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Simuliamo le chiavi generate (ID auto-increment)
        ResultSet generatedKeys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(55L); // ID generato finto

        // Eseguiamo
        boolean result = dao.doSave(dp);

        // Verifiche
        assertTrue(result);
        assertEquals(55L, dp.getId()); // Verifica che l'ID sia stato settato

        // Verifichiamo che abbia settato i parametri (giusto per sicurezza)
        verify(preparedStatement).setString(1, "Bar Prova");
    }

    @Test
    void testDoRetrieveByEmail_Trovato() throws SQLException {
        // Simuliamo che la query trovi un risultato
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true); // C'è una riga

        // Simuliamo i dati estratti dal DB
        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("email")).thenReturn("test@dao.it");
        when(resultSet.getString("stato")).thenReturn("APPROVATO");

        DropPoint result = dao.doRetrieveByEmail("test@dao.it");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("test@dao.it", result.getEmail());
        assertEquals(StatoDropPoint.APPROVATO, result.getStato());
    }

    @Test
    void testDoRetrieveAllApprovati() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // Simuliamo 2 risultati nel ciclo while: true, true, false
        when(resultSet.next()).thenReturn(true, true, false);

        when(resultSet.getString("stato")).thenReturn("APPROVATO");

        List<DropPoint> list = dao.doRetrieveAllApprovati();

        assertEquals(2, list.size());
    }

    @Test
    void testUpdateStato() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1); // 1 riga aggiornata

        boolean res = dao.updateStato(1L, StatoDropPoint.APPROVATO);

        assertTrue(res);
        verify(preparedStatement).setString(1, "APPROVATO");
        verify(preparedStatement).setLong(2, 1L);
    }

    @Test
    void testUpdateProfilo() throws SQLException {
        DropPoint dp = new DropPoint();
        dp.setId(1L);
        dp.setNomeAttivita("Nuovo Nome");
        dp.setImmagine(new byte[]{1});

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean res = dao.updateProfilo(dp); // O doUpdateProfilo a seconda di quale usi

        assertTrue(res);
    }

    @Test
    void testDoRetrieveById() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(99L);
        when(resultSet.getString("stato")).thenReturn("IN_ATTESA");

        DropPoint result = dao.doRetrieveById(99L);

        assertNotNull(result);
        assertEquals(99L, result.getId());
    }

    @Test
    void testDoDeleteById() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        boolean res = dao.doDeleteById(5L);
        assertTrue(res);
    }
}