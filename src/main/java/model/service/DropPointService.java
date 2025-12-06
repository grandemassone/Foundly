package model.service;

import model.bean.DropPoint;
import model.dao.DropPointDAO;
import model.utils.PasswordUtils;

public class DropPointService {
    private final DropPointDAO dropPointDAO = new DropPointDAO();

    public boolean registraDropPoint(String nomeAttivita, String email, String password, String indirizzo, String citta, String provincia, String telefono, String orari) {
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
        dp.setStato("IN_ATTESA"); // Di default deve essere approvato dall'admin

        return dropPointDAO.doSave(dp);
    }

    public DropPoint login(String email, String password) {
        // 1. Recupera il DropPoint dal DB
        DropPoint dp = dropPointDAO.doRetrieveByEmail(email);

        if (dp == null) {
            return null; // Email non trovata
        }

        // 2. Verifica la password (hash)
        if (PasswordUtils.checkPassword(password, dp.getPasswordHash())) {
            return dp; // Login successo
        }

        return null; // Password errata
    }
}