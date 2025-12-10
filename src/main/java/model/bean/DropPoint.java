package model.bean;

import model.bean.enums.StatoDropPoint;

public class DropPoint {

    private Long id;
    private String nomeAttivita;
    private String email;
    private String passwordHash;
    private String indirizzo;
    private String citta;
    private String provincia;
    private String telefono;
    private String orariApertura;
    private String descrizione;

    // Ora BLOB nel DB → byte[] nel model
    private byte[] immagine;
    private String immagineContentType;

    private Double latitudine;
    private Double longitudine;
    private int ritiriEffettuati;

    private StatoDropPoint stato;

    public DropPoint() {}

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeAttivita() {
        return nomeAttivita;
    }

    public void setNomeAttivita(String nomeAttivita) {
        this.nomeAttivita = nomeAttivita;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getOrariApertura() {
        return orariApertura;
    }

    public void setOrariApertura(String orariApertura) {
        this.orariApertura = orariApertura;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public byte[] getImmagine() {
        return immagine;
    }

    public void setImmagine(byte[] immagine) {
        this.immagine = immagine;
    }

    public String getImmagineContentType() {
        return immagineContentType;
    }

    public void setImmagineContentType(String immagineContentType) {
        this.immagineContentType = immagineContentType;
    }

    public Double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(Double latitudine) {
        this.latitudine = latitudine;
    }

    public Double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(Double longitudine) {
        this.longitudine = longitudine;
    }

    public int getRitiriEffettuati() {
        return ritiriEffettuati;
    }

    public void setRitiriEffettuati(int ritiriEffettuati) {
        this.ritiriEffettuati = ritiriEffettuati;
    }

    public StatoDropPoint getStato() {
        return stato;
    }

    public void setStato(StatoDropPoint stato) {
        this.stato = stato;
    }
}
