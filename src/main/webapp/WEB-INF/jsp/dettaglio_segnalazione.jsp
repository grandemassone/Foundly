<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.Reclamo" %>
<%@ page import="model.bean.enums.ModalitaConsegna" %>
<%@ page import="model.bean.SegnalazioneOggetto" %>
<%@ page import="model.bean.enums.StatoReclamo" %>
<%@ page import="model.bean.enums.StatoSegnalazione" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    // --- CONTROLLI ---
    model.bean.DropPoint dp = (model.bean.DropPoint) session.getAttribute("dropPoint");
    model.bean.Utente u = (model.bean.Utente) session.getAttribute("utente");

    if (dp != null) { response.sendRedirect(request.getContextPath() + "/area-drop-point"); return; }
    if (u == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }

    Utente utente = (Utente) session.getAttribute("utente");
    model.bean.Segnalazione s = (model.bean.Segnalazione) request.getAttribute("segnalazione");
    boolean isLogged = (utente != null);
    boolean isOwner = (isLogged && s != null && s.getIdUtente() == utente.getId());

    // --- MODALITÀ ---
    boolean isDropPoint = false;
    if (s instanceof SegnalazioneOggetto) {
        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
        if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
            isDropPoint = true;
        }
    }

    // --- RECLAMI ---
    @SuppressWarnings("unchecked")
    List<Reclamo> reclami = (List<Reclamo>) request.getAttribute("reclamiRicevuti");
    Reclamo reclamoAccettato = null;

    if(reclami != null) {
        for(Reclamo r : reclami) {
            if(r.getStato() == StatoReclamo.ACCETTATO) { reclamoAccettato = r; break; }
        }
    }
    if(reclamoAccettato == null) {
        Reclamo mio = (Reclamo) request.getAttribute("mioReclamo");
        if(mio != null && mio.getStato() == StatoReclamo.ACCETTATO) {
            reclamoAccettato = mio;
        }
    }

    // --- CONTROPARTE ---
    Utente controparte = null;
    String etichettaControparte = "Utente";

    if (reclamoAccettato != null) {
        if (isOwner) {
            @SuppressWarnings("unchecked")
            Map<Long, Utente> mappa = (Map<Long, Utente>) request.getAttribute("mappaRichiedenti");
            if (mappa != null) controparte = mappa.get(reclamoAccettato.getIdUtenteRichiedente());
            etichettaControparte = "Restituito a";
        } else {
            controparte = (Utente) request.getAttribute("proprietarioSegnalazione");
            etichettaControparte = "Contatti Finder";
        }
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <title>${segnalazione.titolo != null ? segnalazione.titolo : "Dettaglio"} - Foundly</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio_segnalazione.css?v=16">
</head>

<body>

<jsp:include page="/WEB-INF/jsp/navbar.jsp"/>

<div class="local-wrapper">

    <a href="${pageContext.request.contextPath}/index" class="local-back-link">
        <span class="material-icons">arrow_back</span> Torna alla Home
    </a>

    <c:if test="${segnalazione == null}">
        <div class="local-card empty-card" style="text-align:center; padding:60px 40px;">
            <h2>Segnalazione non trovata</h2>
            <a href="${pageContext.request.contextPath}/index" class="btn-primary">Vai alla Home</a>
        </div>
    </c:if>

    <c:if test="${segnalazione != null}">
        <div class="local-grid">

            <div class="main-content">
                <div class="local-card hero-card">
                    <div class="image-container">
                        <c:choose>
                            <c:when test="${not empty segnalazione.immagine}">
                                <img src="${pageContext.request.contextPath}/segnalazione-img?id=${segnalazione.id}" class="local-hero-img" alt="Foto">
                            </c:when>
                            <c:otherwise>
                                <div class="local-hero-placeholder"><span class="material-icons">image_not_supported</span></div>
                            </c:otherwise>
                        </c:choose>
                        <div class="status-overlay">
                            <span class="status-badge ${segnalazione.stato == 'CHIUSA' ? 'status-closed' : 'status-open'}">
                                    ${segnalazione.stato}
                            </span>
                        </div>
                    </div>

                    <div class="local-content">
                        <div class="header-row">
                            <div class="category-tags">
                                <span class="tag-pill">${segnalazione.tipoSegnalazione}</span>
                                <c:if test="${segnalazione.tipoSegnalazione == 'OGGETTO'}">
                                    <span class="tag-pill outline">${segnalazione.categoria}</span>
                                </c:if>
                                <c:if test="${segnalazione.tipoSegnalazione == 'ANIMALE'}">
                                    <span class="tag-pill outline">${segnalazione.specie}</span>
                                </c:if>
                            </div>
                            <span class="date-text">Pubblicato il <fmt:formatDate value="${segnalazione.dataPubblicazione}" pattern="dd MMM yyyy"/></span>
                        </div>

                        <h1 class="local-title">${segnalazione.titolo}</h1>
                        <p class="local-text">${segnalazione.descrizione}</p>

                        <div class="info-grid">
                            <div class="info-item">
                                <div class="icon-wrap"><span class="material-icons">calendar_today</span></div>
                                <div><span class="label">Data Ritrovamento</span><div class="value"><fmt:formatDate value="${segnalazione.dataRitrovamento}" pattern="dd MMM yyyy"/></div></div>
                            </div>
                            <div class="info-item">
                                <div class="icon-wrap"><span class="material-icons">location_on</span></div>
                                <div><span class="label">Luogo</span><div class="value">${segnalazione.luogoRitrovamento}</div></div>
                            </div>
                        </div>

                        <div id="itemMap" class="mini-map"></div>

                        <% if (isDropPoint) { %>
                        <div class="delivery-box purple">
                            <div class="icon-box"><span class="material-icons">store</span></div>
                            <div><h4>Drop-Point Partner</h4><p>L'oggetto è custodito presso un negozio autorizzato.</p></div>
                        </div>
                        <% } else { %>
                        <div class="delivery-box green">
                            <div class="icon-box"><span class="material-icons">handshake</span></div>
                            <div><h4>Scambio Diretto</h4><p>Incontro diretto tra Finder e Proprietario.</p></div>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>

            <aside class="sidebar">

                <c:if test="<%= !isOwner %>">
                    <c:choose>
                        <c:when test="${not empty mioReclamo && mioReclamo.stato == 'ACCETTATO'}">
                            <div class="winner-card">
                                <div class="confetti-icon">🎉</div>
                                <h3 class="winner-title">Congratulazioni!</h3>
                                <p class="winner-subtitle">L'oggetto è ufficialmente tuo.</p>

                                <c:if test="${not empty mioReclamo.codiceConsegna}">
                                    <div class="code-ticket">
                                        <span class="code-label">CODICE RITIRO DROP-POINT</span>
                                        <div class="the-code">${mioReclamo.codiceConsegna}</div>
                                    </div>
                                    <p class="winner-note">Mostra questo codice al negozio.</p>

                                    <c:if test="${not empty dropPointRitiro}">
                                        <div class="pickup-location-card">
                                            <div class="pickup-header">RITIRA PRESSO</div>
                                            <div class="pickup-name">${dropPointRitiro.nomeAttivita}</div>
                                            <div class="pickup-address">
                                                    ${dropPointRitiro.indirizzo}, ${dropPointRitiro.citta}
                                            </div>

                                            <div class="pickup-details-row">
                                                <span class="material-icons">schedule</span>
                                                <span>${dropPointRitiro.orariApertura}</span>
                                            </div>

                                            <c:if test="${not empty dropPointRitiro.telefono}">
                                                <div class="pickup-details-row">
                                                    <span class="material-icons">call</span>
                                                    <span>${dropPointRitiro.telefono}</span>
                                                </div>
                                            </c:if>
                                        </div>
                                    </c:if>
                                </c:if>

                                    <%-- Scambio diretto --%>
                                <% if (!isDropPoint && s.getStato() == StatoSegnalazione.APERTA && reclamoAccettato != null) { %>
                                <div style="margin-top:20px;"></div>
                                <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                    <input type="hidden" name="action" value="conferma_scambio">
                                    <input type="hidden" name="idReclamo" value="${mioReclamo.id}">
                                    <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                                    <% if (reclamoAccettato.isConfermaOwner()) { %>
                                    <div style="text-align:center; color:#2E7D32; font-weight:600; margin-bottom:5px;">Ricezione confermata!</div>
                                    <button type="button" class="btn-disabled status-pill done" style="width:100%;">In attesa del Finder...</button>
                                    <% } else { %>
                                    <button type="submit" class="btn-primary">Conferma Ricezione</button>
                                    <% } %>
                                </form>
                                <% } %>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div class="sidebar-card">
                                <h3 class="sidebar-title">È tuo questo oggetto?</h3>

                                <c:if test="${not empty mioReclamo}">
                                    <c:choose>
                                        <c:when test="${mioReclamo.stato == 'RIFIUTATO'}">
                                            <div style="background:#FFEBEE; color:#C62828; padding:20px; border-radius:12px; text-align:center; border:1px solid #FFCDD2;">
                                                <span class="material-icons" style="font-size:32px; display:block; margin-bottom:8px;">cancel</span>
                                                <strong>Richiesta Rifiutata</strong>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="user-status-card pending">
                                                <div class="st-icon-wrapper"><span class="material-icons">hourglass_top</span></div>
                                                <div class="st-info">
                                                    <span class="st-label">Stato Richiesta</span>
                                                    <span class="st-value">In Valutazione</span>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <c:if test="${empty mioReclamo && segnalazione.stato == 'APERTA'}">
                                    <p style="color:#666; font-size:0.9rem; margin-bottom:20px;">Rispondi alle domande di sicurezza.</p>
                                    <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                        <input type="hidden" name="action" value="invia">
                                        <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                        <label style="display:block; font-size:0.8rem; font-weight:700; color:#555; margin-bottom:5px;">1. ${segnalazione.domandaVerifica1}</label>
                                        <input type="text" name="risposta1" required class="form-input" style="margin-bottom:15px;">
                                        <label style="display:block; font-size:0.8rem; font-weight:700; color:#555; margin-bottom:5px;">2. ${segnalazione.domandaVerifica2}</label>
                                        <input type="text" name="risposta2" required class="form-input" style="margin-bottom:20px;">
                                        <button class="btn-primary full-width">Invia Reclamo</button>
                                    </form>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:if>

                <c:if test="<%= isOwner %>">
                    <div class="sidebar-card">
                        <h3 class="sidebar-title" style="display:flex; align-items:center; gap:8px;">
                            <span class="material-icons" style="color:var(--primary);">admin_panel_settings</span>
                            Gestione Reclami
                        </h3>

                        <c:forEach var="r" items="${reclamiRicevuti}">
                            <c:set var="userRichiedente" value="${mappaRichiedenti[r.idUtenteRichiedente]}" />

                            <div class="claim-card">
                                <div class="claim-header">
                                    <span style="display:flex; align-items:center; gap:6px; font-weight:700;">
                                        <span class="material-icons" style="font-size:18px;">person</span> ${userRichiedente.username}
                                    </span>
                                    <span style="font-size:0.8rem; color:#888;"><fmt:formatDate value="${r.dataRichiesta}" pattern="dd/MM HH:mm"/></span>
                                </div>
                                <div style="font-size:0.9rem; margin-bottom:10px;">
                                    <div><strong>R1:</strong> ${r.rispostaVerifica1}</div>
                                    <div><strong>R2:</strong> ${r.rispostaVerifica2}</div>
                                </div>

                                <c:if test="${r.stato == 'IN_ATTESA'}">
                                    <div style="display:grid; grid-template-columns: 1fr 1fr; gap:10px;">
                                        <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                            <input type="hidden" name="action" value="accetta">
                                            <input type="hidden" name="idReclamo" value="${r.id}">
                                            <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                            <button class="btn-accept" style="width:100%;">Accetta</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                            <input type="hidden" name="action" value="rifiuta">
                                            <input type="hidden" name="idReclamo" value="${r.id}">
                                            <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                            <button class="btn-reject" style="width:100%;">Rifiuta</button>
                                        </form>
                                    </div>
                                </c:if>

                                <c:if test="${r.stato == 'ACCETTATO'}">
                                    <div class="winner-card" style="margin-top:10px; padding:15px; border-radius:12px;">
                                        <div style="font-weight:800; color:#2E7D32; margin-bottom:10px; font-size:0.95rem;">
                                            <span class="material-icons" style="vertical-align:middle; font-size:18px;">check_circle</span> RECLAMO ACCETTATO
                                        </div>

                                        <c:if test="${not empty r.codiceConsegna}">
                                            <div style="background:#FFF; border:2px dashed #2E7D32; padding:10px; border-radius:8px; margin-bottom:10px;">
                                                <div style="font-size:0.7rem; color:#555; font-weight:700;">CODICE:</div>
                                                <div style="font-family:monospace; font-size:1.4rem; font-weight:700; color:#1B5E20; letter-spacing:2px;">${r.codiceConsegna}</div>
                                            </div>

                                            <c:if test="${not empty dropPointRitiro}">
                                                <div class="pickup-location-card" style="padding:12px; margin-top:0;">
                                                    <div class="pickup-header" style="margin-bottom:4px;">Porta l'oggetto qui:</div>
                                                    <div class="pickup-name" style="font-size:1rem;">${dropPointRitiro.nomeAttivita}</div>
                                                    <div class="pickup-address" style="font-size:0.85rem; margin-bottom:4px;">${dropPointRitiro.indirizzo}</div>
                                                    <div style="font-size:0.8rem; color:#555; font-style:italic;">Consegna l'oggetto e comunica il codice all'operatore.</div>
                                                </div>
                                            </c:if>
                                        </c:if>
                                    </div>
                                </c:if>

                                <c:if test="${r.stato == 'RIFIUTATO'}">
                                    <div style="background:#FFEBEE; color:#C62828; padding:8px; border-radius:8px; text-align:center; font-weight:700; margin-top:10px;">RIFIUTATO</div>
                                </c:if>
                            </div>
                        </c:forEach>

                        <div style="margin-top:20px; border-top:1px solid #eee; padding-top:15px;">
                            <form action="${pageContext.request.contextPath}/dettaglio-segnalazione" method="post" onsubmit="return confirm('Eliminare?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                <button class="btn-danger full-width" style="display:flex; justify-content:center; align-items:center; gap:8px;">
                                    <span class="material-icons">delete</span> Elimina Segnalazione
                                </button>
                            </form>
                        </div>
                    </div>
                </c:if>

            </aside>
        </div>
    </c:if>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var lat = ${segnalazione.latitudine != null ? segnalazione.latitudine : 'null'};
        var lon = ${segnalazione.longitudine != null ? segnalazione.longitudine : 'null'};
        if (lat && lon) {
            var map = L.map('itemMap', { zoomControl:false }).setView([lat, lon], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
            L.marker([lat, lon]).addTo(map).bindPopup("<b>${segnalazione.titolo}</b>").openPopup();
        } else {
            document.getElementById('itemMap').style.display = 'none';
        }
    });
</script>
</body>
</html>