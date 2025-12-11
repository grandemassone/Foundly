package model.service;

import model.bean.DropPoint;
import model.bean.enums.StatoDropPoint;
import model.dao.DropPointDAO;
import model.utils.PasswordUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DropPointService {

    private final DropPointDAO dropPointDAO = new DropPointDAO();

    // ==========================
    //  REGISTRAZIONE / LOGIN
    // ==========================

    public boolean registraDropPoint(String nomeAttivita, String email, String password,
                                     String indirizzo, String citta, String provincia,
                                     String telefono, String orari,
                                     Double latitudine, Double longitudine) {

        if (dropPointDAO.doRetrieveByEmail(email) != null) {
            return false;
        }

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

        // Nessuna immagine in registrazione (BLOB null)
        dp.setImmagine(null);
        dp.setImmagineContentType(null);

        return dropPointDAO.doSave(dp);
    }

    public DropPoint login(String email, String password) {
        DropPoint dp = dropPointDAO.doRetrieveByEmail(email);
        if (dp == null) return null;

        if (!PasswordUtils.checkPassword(password, dp.getPasswordHash())) {
            return null;
        }

        // ricarico dal DB così ho lo stato aggiornato (APPROVATO / IN_ATTESA / RIFIUTATO)
        return dropPointDAO.doRetrieveById(dp.getId());
    }

    // ==========================
    //   QUERY DI SUPPORTO
    // ==========================

    /** Drop-Point approvati per la mappa/pubblico. */
    public List<DropPoint> findAllApprovati() {
        return dropPointDAO.doRetrieveAllApprovati();
    }

    /** Drop-Point in attesa per l’area admin. */
    public List<DropPoint> findAllInAttesa() {
        return dropPointDAO.doRetrieveByStato(StatoDropPoint.IN_ATTESA);
    }

    /** Approvazione da Area Admin. */
    public boolean approvaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.APPROVATO);
    }

    /** Rifiuto da Area Admin. */
    public boolean rifiutaDropPoint(long id) {
        return dropPointDAO.updateStato(id, StatoDropPoint.RIFIUTATO);
    }

    /** Recupero per id. */
    public DropPoint trovaPerId(long id) {
        return dropPointDAO.doRetrieveById(id);
    }

    // =====================================================
    //   LOGICA IN-MEMORY DEPOSITO / RITIRO (NO DB CHANGE)
    // =====================================================

    private enum StatoOperazione {
        DEPOSITATO,
        RITIRATO
    }

    /**
     * Mappa: idDropPoint -> (codiceConsegna -> stato operazione)
     * È statica così vale per tutta la JVM (finché il server resta acceso).
     */
    private static final Map<Long, Map<String, StatoOperazione>> operazioniByDropPoint =
            new ConcurrentHashMap<>();

    private Map<String, StatoOperazione> getMappaOperazioni(long idDropPoint) {
        return operazioniByDropPoint.computeIfAbsent(idDropPoint,
                k -> new ConcurrentHashMap<>());
    }

    /**
     * Registra un DEPOSITO per quel Drop-Point e codice.
     * Ritorna false se il codice è già stato usato (depositato o ritirato).
     */
    public synchronized boolean registraDeposito(long idDropPoint, String codiceConsegna) {
        if (codiceConsegna == null || codiceConsegna.isBlank()) {
            return false;
        }

        Map<String, StatoOperazione> mappa = getMappaOperazioni(idDropPoint);
        StatoOperazione stato = mappa.get(codiceConsegna);

        if (stato == null) {
            mappa.put(codiceConsegna, StatoOperazione.DEPOSITATO);
            return true;
        }

        // se esiste già, non permetto un secondo deposito con lo stesso codice
        return false;
    }

    /**
     * Registra un RITIRO.
     * È consentito SOLO se prima esisteva un DEPOSITATO per quel codice.
     */
    public synchronized boolean registraRitiro(long idDropPoint, String codiceConsegna) {
        if (codiceConsegna == null || codiceConsegna.isBlank()) {
            return false;
        }

        Map<String, StatoOperazione> mappa = getMappaOperazioni(idDropPoint);
        StatoOperazione stato = mappa.get(codiceConsegna);

        // posso ritirare solo se prima è stato registrato un deposito
        if (stato == StatoOperazione.DEPOSITATO) {
            mappa.put(codiceConsegna, StatoOperazione.RITIRATO);
            return true;
        }

        return false; // nessun deposito, o già ritirato
    }

    // ---------- Contatori per dashboard ----------

    public int countDepositiAttivi(long idDropPoint) {
        Map<String, StatoOperazione> mappa = operazioniByDropPoint.get(idDropPoint);
        if (mappa == null) return 0;

        int c = 0;
        for (StatoOperazione s : mappa.values()) {
            if (s == StatoOperazione.DEPOSITATO) c++;
        }
        return c;
    }

    public int countConsegneCompletate(long idDropPoint) {
        Map<String, StatoOperazione> mappa = operazioniByDropPoint.get(idDropPoint);
        if (mappa == null) return 0;

        int c = 0;
        for (StatoOperazione s : mappa.values()) {
            if (s == StatoOperazione.RITIRATO) c++;
        }
        return c;
    }

    public int countTotaleOperazioni(long idDropPoint) {
        Map<String, StatoOperazione> mappa = operazioniByDropPoint.get(idDropPoint);
        return (mappa == null) ? 0 : mappa.size();
    }
}
