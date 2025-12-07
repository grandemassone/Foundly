package model.service;

import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint; // Assicurati di avere questo import
import model.dao.DropPointDAO;
import model.utils.PasswordUtils;

public class DropPointService {

    private final DropPointDAO dropPointDAO = new DropPointDAO();

    // NOTA: Ho corretto l'ordine: prima latitudine, poi longitudine (come nella Servlet)
    public boolean registraDropPoint(String nomeAttivita, String email, String password,
                                     String indirizzo, String citta, String provincia,
                                     String telefono, String orari,
                                     Double latitudine, Double longitudine) {

        if (dropPointDAO.doRetrieveByEmail(email) != null) return false;

        DropPoint dp = new DropPoint();
        dp.setNomeAttivita(nomeAttivita);
        dp.setEmail(email);
        dp.setPasswordHash(PasswordUtils.hashPassword(password));
        dp.setIndirizzo(indirizzo);
        dp.setCitta(citta);
        dp.setProvincia(provincia);
        dp.setTelefono(telefono);
        dp.setOrariApertura(orari);

        // CORRETTO: Uso dei setter
        dp.setLatitudine(latitudine);
        dp.setLongitudine(longitudine);

        // CORRETTO: Uso Enum e valori default obbligatori nel DB
        dp.setStato(StatoDropPoint.IN_ATTESA);
        dp.setRitiriEffettuati(0);
        dp.setImmagine("default.png");

        return dropPointDAO.doSave(dp);
    }

    public DropPoint login(String email, String password) {
        DropPoint dp = dropPointDAO.doRetrieveByEmail(email);
        if (dp == null) return null;
        if (PasswordUtils.checkPassword(password, dp.getPasswordHash())) return dp;
        return null;
    }
}