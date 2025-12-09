package model.service;

import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;
import model.dao.DropPointDAO;
import model.utils.PasswordUtils;

import java.util.List;

public class DropPointService {

    private final DropPointDAO dropPointDAO = new DropPointDAO();

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

        dp.setLatitudine(latitudine);
        dp.setLongitudine(longitudine);

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

    /** Drop-Point approvati per la mappa/pubblico. */
    public List<DropPoint> findAllApprovati() {
        return dropPointDAO.doRetrieveAllApprovati();
    }

    /** NUOVO: Drop-Point in attesa per l’area admin. */
    public List<DropPoint> findAllInAttesa() {
        return dropPointDAO.doRetrieveByStato(StatoDropPoint.IN_ATTESA);
    }

    /** NUOVO: approvazione da Area Admin. */
    public boolean approvaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.APPROVATO);
    }

    /** NUOVO: rifiuto da Area Admin. */
    public boolean rifiutaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.RIFIUTATO);
    }
}
