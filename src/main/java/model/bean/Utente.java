package model.bean;

import model.bean.enums.Ruolo;

public class Utente {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String nome;
    private String cognome;
    private String telefono;

    // Ora BLOB nel DB → byte[] nel model
    private byte[] immagineProfilo;
    private String immagineProfiloContentType;

    private int punteggio;
    private Ruolo ruolo;
    private String badge;

    public Utente() {}

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public byte[] getImmagineProfilo() { return immagineProfilo; }
    public void setImmagineProfilo(byte[] immagineProfilo) { this.immagineProfilo = immagineProfilo; }

    public String getImmagineProfiloContentType() { return immagineProfiloContentType; }
    public void setImmagineProfiloContentType(String immagineProfiloContentType) {
        this.immagineProfiloContentType = immagineProfiloContentType;
    }

    public int getPunteggio() { return punteggio; }
    public void setPunteggio(int punteggio) { this.punteggio = punteggio; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}