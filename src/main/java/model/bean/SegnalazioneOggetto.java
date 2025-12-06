package model.bean;

import model.bean.enums.CategoriaOggetto;
import model.bean.enums.ModalitaConsegna;
import model.bean.enums.TipoSegnalazione;

public class SegnalazioneOggetto extends Segnalazione {
    private CategoriaOggetto categoria;
    private ModalitaConsegna modalitaConsegna;
    private Long idDropPoint;

    public SegnalazioneOggetto() {
        super();
        this.setTipoSegnalazione(TipoSegnalazione.OGGETTO);
    }

    public CategoriaOggetto getCategoria() { return categoria; }
    public void setCategoria(CategoriaOggetto categoria) { this.categoria = categoria; }

    public ModalitaConsegna getModalitaConsegna() { return modalitaConsegna; }
    public void setModalitaConsegna(ModalitaConsegna modalitaConsegna) { this.modalitaConsegna = modalitaConsegna; }

    public Long getIdDropPoint() { return idDropPoint; }
    public void setIdDropPoint(Long idDropPoint) { this.idDropPoint = idDropPoint; }
}