package model.bean;

import model.bean.enums.StatoSegnalazione;
import model.bean.enums.TipoSegnalazione;

import java.sql.Timestamp;

public abstract class Segnalazione {
    private long id;
    private long idUtente;
    private String titolo;
    private String descrizione;
    private Timestamp dataRitrovamento;
    private String luogoRitrovamento;
    private String citta;
    private String provincia;
    private Double latitudine;
    private Double longitudine;

    // Ora BLOB nel DB → byte[] nel model
    private byte[] immagine;
    private String immagineContentType;

    private String domandaVerifica1;
    private String domandaVerifica2;
    private Timestamp dataPubblicazione;
    private StatoSegnalazione stato;
    private TipoSegnalazione tipoSegnalazione;

    public Segnalazione() {}

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIdUtente() { return idUtente; }
    public void setIdUtente(long idUtente) { this.idUtente = idUtente; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Timestamp getDataRitrovamento() { return dataRitrovamento; }
    public void setDataRitrovamento(Timestamp dataRitrovamento) {
        this.dataRitrovamento = dataRitrovamento;
    }

    public String getLuogoRitrovamento() { return luogoRitrovamento; }
    public void setLuogoRitrovamento(String luogoRitrovamento) {
        this.luogoRitrovamento = luogoRitrovamento;
    }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public Double getLatitudine() { return latitudine; }
    public void setLatitudine(Double latitudine) { this.latitudine = latitudine; }

    public Double getLongitudine() { return longitudine; }
    public void setLongitudine(Double longitudine) { this.longitudine = longitudine; }

    public byte[] getImmagine() { return immagine; }
    public void setImmagine(byte[] immagine) { this.immagine = immagine; }

    public String getImmagineContentType() { return immagineContentType; }
    public void setImmagineContentType(String immagineContentType) {
        this.immagineContentType = immagineContentType;
    }

    public String getDomandaVerifica1() { return domandaVerifica1; }
    public void setDomandaVerifica1(String domandaVerifica1) {
        this.domandaVerifica1 = domandaVerifica1;
    }

    public String getDomandaVerifica2() { return domandaVerifica2; }
    public void setDomandaVerifica2(String domandaVerifica2) {
        this.domandaVerifica2 = domandaVerifica2;
    }

    public Timestamp getDataPubblicazione() { return dataPubblicazione; }
    public void setDataPubblicazione(Timestamp dataPubblicazione) {
        this.dataPubblicazione = dataPubblicazione;
    }

    public StatoSegnalazione getStato() { return stato; }
    public void setStato(StatoSegnalazione stato) { this.stato = stato; }

    public TipoSegnalazione getTipoSegnalazione() { return tipoSegnalazione; }
    public void setTipoSegnalazione(TipoSegnalazione tipoSegnalazione) {
        this.tipoSegnalazione = tipoSegnalazione;
    }
}
