package model.bean;

import model.bean.enums.TipoSegnalazione;

public class SegnalazioneAnimale extends Segnalazione {
    private String specie;
    private String razza;

    public SegnalazioneAnimale() {
        super();
        this.setTipoSegnalazione(TipoSegnalazione.ANIMALE);
    }

    public String getSpecie() { return specie; }
    public void setSpecie(String specie) { this.specie = specie; }

    public String getRazza() { return razza; }
    public void setRazza(String razza) { this.razza = razza; }
}