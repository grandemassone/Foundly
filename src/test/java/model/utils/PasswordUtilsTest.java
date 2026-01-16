package model.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void testHashAndCheck() {
        String password = "passwordSegreta123";

        // 1. Genera l'hash
        String hash = PasswordUtils.hashPassword(password);

        // Verifica che l'hash non sia nullo e inizi con il prefisso BCrypt standard
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));

        // 2. Verifica che la password corretta venga validata
        assertTrue(PasswordUtils.checkPassword(password, hash));

        // 3. Verifica che una password sbagliata venga rifiutata
        assertFalse(PasswordUtils.checkPassword("passwordSbagliata", hash));
    }

    @Test
    void testCheckPassword_HashNonValido() {
        // Verifica che lanci eccezione se l'hash è null o formato errato
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.checkPassword("password", "hash_farlocco");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.checkPassword("password", null);
        });
    }
}