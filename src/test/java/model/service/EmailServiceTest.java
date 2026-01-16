package model.service;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class EmailServiceTest {

    private EmailService emailService;
    private MockedStatic<Transport> transportMock;

    @BeforeEach
    void setUp() {
        // Mock statico di Transport per evitare che cerchi di connettersi a Gmail
        transportMock = Mockito.mockStatic(Transport.class);
        emailService = new EmailService();
    }

    @AfterEach
    void tearDown() {
        transportMock.close();
    }

    @Test
    void testInviaEmail_NonCrasha() {
        // Poiché il metodo inviaEmail lancia un Thread separato,
        // è difficile verificare con certezza l'esecuzione in un unit test semplice.
        // Qui verifichiamo solo che chiamando i metodi pubblici non vengano lanciate eccezioni
        // (es. NullPointer per stringhe vuote o formati errati).

        assertDoesNotThrow(() ->
                emailService.inviaCodiceRecuperoPassword("test@test.it", "123456")
        );

        assertDoesNotThrow(() ->
                emailService.inviaAccettazioneDropPoint("dp@test.it", "Bar Sport")
        );

        assertDoesNotThrow(() ->
                emailService.inviaNotificaNuovoReclamo("finder@test.it", "Chiavi", "ownerUser")
        );

        assertDoesNotThrow(() ->
                emailService.inviaReclamoAccettatoOwner("owner@test.it", "Oggetto", "Finder", "CODE")
        );
    }

    // Metodo helper per JUnit 5 se non lo hai importato staticamente
    private void assertDoesNotThrow(org.junit.jupiter.api.function.Executable executable) {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(executable);
    }
}