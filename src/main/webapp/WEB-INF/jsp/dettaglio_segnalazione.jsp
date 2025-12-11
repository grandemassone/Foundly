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

<%
    model.bean.DropPoint dp = (model.bean.DropPoint) session.getAttribute("dropPoint");
    model.bean.Utente u = (model.bean.Utente) session.getAttribute("utente");

    if (dp != null) { response.sendRedirect(request.getContextPath() + "/area-drop-point"); return; }
    if (u == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }

    Utente utente = (Utente) session.getAttribute("utente");
    model.bean.Segnalazione s = (model.bean.Segnalazione) request.getAttribute("segnalazione");
    boolean isLogged = (utente != null);
    boolean isOwner = (isLogged && s != null && s.getIdUtente() == utente.getId());

    // Controllo Modalità Consegna
    boolean isDropPoint = false;
    if (s instanceof SegnalazioneOggetto) {
        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
        if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
            isDropPoint = true;
        }
    }

    // Recupero Reclamo Accettato
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
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <title>${segnalazione.titolo != null ? segnalazione.titolo : "Dettaglio"} - Foundly</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio_segnalazione.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>

<body>

<jsp:include page="/WEB-INF/jsp/navbar.jsp"/>

<div class="local-wrapper">

    <a href="${pageContext.request.contextPath}/index" class="local-back-link">
        <span class="material-icons">arrow_back</span> Torna alla Home
    </a>

    <c:if test="${segnalazione == null}">
        <div class="local-card empty-card" style="text-align:center; padding:40px;">
            <h2>Segnalazione non trovata</h2>
            <a href="${pageContext.request.contextPath}/index" class="btn-primary" style="display:inline-block; width:auto; margin-top:10px;">Vai alla Home</a>
        </div>
    </c:if>

    <c:if test="${segnalazione != null}">
        <div class="local-grid">

            <div class="main-content">
                <div class="local-card hero-card">
                    <div class="image-container">
                        <c:choose>
                            <c:when test="${not empty segnalazione.immagine}">
                                <img src="${pageContext.request.contextPath}/segnalazione-img?id=${segnalazione.id}" class="local-hero-img">
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
                            <span class="date-text">
                                Pubblicato il <fmt:formatDate value="${segnalazione.dataPubblicazione}" pattern="dd MMM yyyy"/>
                            </span>
                        </div>

                        <h1 class="local-title">${segnalazione.titolo}</h1>
                        <p class="local-text">${segnalazione.descrizione}</p>

                        <div class="info-grid">
                            <div class="info-item">
                                <span class="material-icons">event</span>
                                <div><span class="label">Data Ritrovamento</span><span class="value"><fmt:formatDate value="${segnalazione.dataRitrovamento}" pattern="dd MMMM yyyy"/></span></div>
                            </div>
                            <div class="info-item">
                                <span class="material-icons">location_on</span>
                                <div><span class="label">Luogo</span><span class="value">${segnalazione.luogoRitrovamento}</span><span class="sub-value">${segnalazione.citta} (${segnalazione.provincia})</span></div>
                            </div>
                        </div>

                        <div id="itemMap" class="mini-map"></div>

                        <% if (isDropPoint) { %>
                        <div class="delivery-box purple">
                            <div class="icon-box"><span class="material-icons">store</span></div>
                            <div><h4>Drop-Point</h4><p>Ritiro presso negozio partner autorizzato.</p></div>
                        </div>
                        <% } else { %>
                        <div class="delivery-box green">
                            <div class="icon-box"><span class="material-icons">handshake</span></div>
                            <div><h4>Consegna Diretta</h4><p>Accordo diretto tra Finder e Proprietario.</p></div>
                        </div>
                        <% } %>

                        <% if (reclamoAccettato != null && !isDropPoint && s.getStato() == StatoSegnalazione.APERTA) { %>
                        <div class="scambio-action-box">
                            <h3 class="scambio-title"><span class="material-icons">published_with_changes</span> Scambio in Corso</h3>
                            <p style="color:#5D4037; margin-bottom:20px;">
                                Ottimo! Il reclamo è stato accettato. Incontratevi per lo scambio.<br>
                                Per chiudere la pratica e assegnare i punti, <strong>entrambi dovete confermare</strong>.
                            </p>
                            <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                <input type="hidden" name="action" value="conferma_scambio">
                                <input type="hidden" name="idReclamo" value="<%= reclamoAccettato.getId() %>">
                                <input type="hidden" name="idSegnalazione" value="<%= s.getId() %>">

                                <% if (isOwner) { %>
                                <% if (reclamoAccettato.isConfermaFinder()) { %>
                                <button type="button" class="btn-disabled"><span class="material-icons" style="vertical-align: middle;">check_circle</span> Hai confermato</button>
                                <% } else { %>
                                <button type="submit" class="btn-primary" style="background: #2E7D32;"><span class="material-icons" style="vertical-align: middle;">send</span> Conferma Consegna</button>
                                <% } %>
                                <% } %>

                                <% if (utente.getId() == reclamoAccettato.getIdUtenteRichiedente()) { %>
                                <% if (reclamoAccettato.isConfermaOwner()) { %>
                                <button type="button" class="btn-disabled"><span class="material-icons" style="vertical-align: middle;">check_circle</span> Hai confermato</button>
                                <% } else { %>
                                <button type="submit" class="btn-primary" style="background: #1565C0;"><span class="material-icons" style="vertical-align: middle;">inventory</span> Conferma Ricezione</button>
                                <% } %>
                                <% } %>
                            </form>
                            <div class="scambio-status">
                                    <span class="status-pill <%= reclamoAccettato.isConfermaFinder() ? "done" : "" %>">
                                        <span class="material-icons" style="font-size:16px"><%= reclamoAccettato.isConfermaFinder() ? "check" : "hourglass_empty" %></span> Finder
                                    </span>
                                <span class="status-pill <%= reclamoAccettato.isConfermaOwner() ? "done" : "" %>">
                                        <span class="material-icons" style="font-size:16px"><%= reclamoAccettato.isConfermaOwner() ? "check" : "hourglass_empty" %></span> Owner
                                    </span>
                            </div>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>

            <aside class="sidebar">
                    <%-- UTENTE RICHIEDENTE (NON FINDER) --%>
                <c:if test="<%= !isOwner %>">
                    <c:choose>
                        <%-- RECLAMO ACCETTATO (WINNER BOX) --%>
                        <c:when test="${not empty mioReclamo && mioReclamo.stato == 'ACCETTATO'}">
                            <div class="winner-card">
                                <div class="confetti-icon">🎉</div>
                                <h4>Congratulazioni!</h4>
                                <p>Il tuo reclamo è stato accettato.</p>

                                    <%-- SE C'È CODICE (DROP POINT) --%>
                                <c:if test="${not empty mioReclamo.codiceConsegna}">
                                    <div style="background:rgba(255,255,255,0.2); padding:10px; border-radius:8px; margin-bottom:15px;">
                                        <small>CODICE RITIRO:</small>
                                        <div style="font-size:1.8rem; font-weight:800; letter-spacing:2px; margin-top:5px;">${mioReclamo.codiceConsegna}</div>
                                    </div>
                                    <p style="font-size:0.9rem;">Mostra questo codice al Drop-Point per ritirare l'oggetto.</p>
                                </c:if>

                                <div class="contact-box-white">
                                    <div style="margin-bottom:10px; font-weight:bold; color:#E65100;">Contatti del Finder</div>
                                    <div class="contact-row"><span class="material-icons">person</span><span>${proprietarioSegnalazione.nome} ${proprietarioSegnalazione.cognome}</span></div>
                                    <div class="contact-row"><span class="material-icons">email</span><a href="mailto:${proprietarioSegnalazione.email}">${proprietarioSegnalazione.email}</a></div>
                                    <div class="contact-row"><span class="material-icons">phone</span><a href="tel:${proprietarioSegnalazione.telefono}">${proprietarioSegnalazione.telefono}</a></div>
                                </div>
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div class="sidebar-card action-card">
                                <h3 class="sidebar-title">È tuo questo oggetto?</h3>
                                <c:if test="${not empty mioReclamo}">
                                    <div style="padding:15px; background:#f5f5f5; border-radius:8px; text-align:center;">Stato Richiesta: <strong>${mioReclamo.stato}</strong></div>
                                </c:if>
                                <c:if test="${empty mioReclamo && segnalazione.stato == 'APERTA'}">

                                    <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                                        <input type="hidden" name="action" value="invia">
                                        <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                                        <label style="font-size:0.9rem; margin-bottom:5px;">1. ${segnalazione.domandaVerifica1}</label>
                                        <input type="text" name="risposta1" required class="form-input">

                                        <label style="font-size:0.9rem; margin-bottom:5px;">2. ${segnalazione.domandaVerifica2}</label>
                                        <input type="text" name="risposta2" required class="form-input">

                                        <button class="btn-primary full-width">Invia Reclamo</button>
                                    </form>

                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:if>

                    <%-- UTENTE FINDER (Gestione) --%>
                <c:if test="<%= isOwner %>">
                    <div class="sidebar-card owner-card">
                        <h3 class="sidebar-title">Gestione Reclami</h3>
                        <c:if test="${empty reclamiRicevuti}"><p style="color:#999; font-style:italic;">Nessuna richiesta ricevuta.</p></c:if>
                        <c:forEach var="r" items="${reclamiRicevuti}">
                            <div class="claim-card">
                                <div class="claim-header">
                                    <strong>Richiesta #${r.id}</strong>
                                    <span style="font-size:0.8rem; color:#666;"><fmt:formatDate value="${r.dataRichiesta}" pattern="dd/MM HH:mm"/></span>
                                </div>
                                <div style="font-size:0.9rem; margin-bottom:10px;">
                                    R1: <i>${r.rispostaVerifica1}</i><br>R2: <i>${r.rispostaVerifica2}</i>
                                </div>
                                <c:if test="${r.stato == 'IN_ATTESA'}">
                                    <div style="display:flex; gap:5px;">
                                        <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post" style="flex:1;">
                                            <input type="hidden" name="action" value="accetta"><input type="hidden" name="idReclamo" value="${r.id}"><input type="hidden" name="idSegnalazione" value="${segnalazione.id}"><button class="btn-accept" style="width:100%;">Accetta</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post" style="flex:1;">
                                            <input type="hidden" name="action" value="rifiuta"><input type="hidden" name="idReclamo" value="${r.id}"><input type="hidden" name="idSegnalazione" value="${segnalazione.id}"><button class="btn-reject" style="width:100%;">Rifiuta</button>
                                        </form>
                                    </div>
                                </c:if>
                                <c:if test="${r.stato == 'ACCETTATO'}">
                                    <div style="text-align:center; margin-top:10px;">
                                        <span style="color:green; font-weight:bold;">ACCETTATO</span>
                                        <c:if test="${not empty r.codiceConsegna}">
                                            <div style="background:#E8F5E9; padding:5px; border-radius:4px; margin-top:5px; font-family:monospace; font-weight:bold;">COD: ${r.codiceConsegna}</div>
                                        </c:if>
                                    </div>
                                </c:if>
                                <c:if test="${r.stato == 'RIFIUTATO'}">
                                    <div style="text-align:center; color:red; font-weight:bold;">RIFIUTATO</div>
                                </c:if>
                            </div>
                        </c:forEach>
                        <form action="${pageContext.request.contextPath}/dettaglio-segnalazione" method="post" onsubmit="return confirm('Eliminare definitivamente questa segnalazione?');">
                            <input type="hidden" name="action" value="delete"><input type="hidden" name="idSegnalazione" value="${segnalazione.id}"><button class="btn-danger"><span class="material-icons">delete</span> Elimina Segnalazione</button>
                        </form>
                    </div>
                </c:if>
            </aside>

        </div>
    </c:if>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        var lat = ${segnalazione.latitudine != null ? segnalazione.latitudine : 'null'};
        var lon = ${segnalazione.longitudine != null ? segnalazione.longitudine : 'null'};
        if (lat && lon) {
            var map = L.map('itemMap').setView([lat, lon], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap contributors' }).addTo(map);
            L.marker([lat, lon]).addTo(map).bindPopup("<b>${segnalazione.titolo}</b><br>${segnalazione.luogoRitrovamento}").openPopup();
        } else { document.getElementById('itemMap').style.display = 'none'; }
    });
</script>

</body>
</html>