package model.bean;

import model.bean.enums.StatoReclamo;

import java.sql.Timestamp;

public class Reclamo {

    private long id;
    private long idSegnalazione;
    private long idUtenteRichiedente;
    private String rispostaVerifica1;
    private String rispostaVerifica2;
    private Timestamp dataRichiesta;
    private StatoReclamo stato;

    // Gestione Consegna
    private String codiceConsegna;
    private Timestamp dataDeposito;
    private Timestamp dataRitiro;
    private boolean confermaFinder;
    private boolean confermaOwner;

    // Dati derivati dalla segnalazione associata (join)
    private String titoloSegnalazione;
    private byte[] immagineSegnalazione;
    private String immagineSegnalazioneContentType;

    public Reclamo() {}

    // Getters e Setters base

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdSegnalazione() {
        return idSegnalazione;
    }

    public void setIdSegnalazione(long idSegnalazione) {
        this.idSegnalazione = idSegnalazione;
    }

    public long getIdUtenteRichiedente() {
        return idUtenteRichiedente;
    }

    public void setIdUtenteRichiedente(long idUtenteRichiedente) {
        this.idUtenteRichiedente = idUtenteRichiedente;
    }

    public String getRispostaVerifica1() {
        return rispostaVerifica1;
    }

    public void setRispostaVerifica1(String rispostaVerifica1) {
        this.rispostaVerifica1 = rispostaVerifica1;
    }

    public String getRispostaVerifica2() {
        return rispostaVerifica2;
    }

    public void setRispostaVerifica2(String rispostaVerifica2) {
        this.rispostaVerifica2 = rispostaVerifica2;
    }

    public Timestamp getDataRichiesta() {
        return dataRichiesta;
    }

    public void setDataRichiesta(Timestamp dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }

    public StatoReclamo getStato() {
        return stato;
    }

    public void setStato(StatoReclamo stato) {
        this.stato = stato;
    }

    public String getCodiceConsegna() {
        return codiceConsegna;
    }

    public void setCodiceConsegna(String codiceConsegna) {
        this.codiceConsegna = codiceConsegna;
    }

    public Timestamp getDataDeposito() {
        return dataDeposito;
    }

    public void setDataDeposito(Timestamp dataDeposito) {
        this.dataDeposito = dataDeposito;
    }

    public Timestamp getDataRitiro() {
        return dataRitiro;
    }

    public void setDataRitiro(Timestamp dataRitiro) {
        this.dataRitiro = dataRitiro;
    }

    public boolean isConfermaFinder() {
        return confermaFinder;
    }

    public void setConfermaFinder(boolean confermaFinder) {
        this.confermaFinder = confermaFinder;
    }

    public boolean isConfermaOwner() {
        return confermaOwner;
    }

    public void setConfermaOwner(boolean confermaOwner) {
        this.confermaOwner = confermaOwner;
    }

    // Dati derivati dalla segnalazione

    public String getTitoloSegnalazione() {
        return titoloSegnalazione;
    }

    public void setTitoloSegnalazione(String titoloSegnalazione) {
        this.titoloSegnalazione = titoloSegnalazione;
    }

    public byte[] getImmagineSegnalazione() {
        return immagineSegnalazione;
    }

    public void setImmagineSegnalazione(byte[] immagineSegnalazione) {
        this.immagineSegnalazione = immagineSegnalazione;
    }

    public String getImmagineSegnalazioneContentType() {
        return immagineSegnalazioneContentType;
    }

    public void setImmagineSegnalazioneContentType(String immagineSegnalazioneContentType) {
        this.immagineSegnalazioneContentType = immagineSegnalazioneContentType;
    }
}
