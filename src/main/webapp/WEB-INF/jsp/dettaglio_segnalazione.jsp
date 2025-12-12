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
    // Recupero Sessione
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
    @SuppressWarnings("unchecked")
    List<Reclamo> reclami = (List<Reclamo>) request.getAttribute("reclamiRicevuti");
    Reclamo reclamoAccettato = null;

    if(reclami != null) {
        for(Reclamo r : reclami) {
            if(r.getStato() == StatoReclamo.ACCETTATO) { reclamoAccettato = r; break; }
        }
    }
    // Se sono il richiedente (non owner), controllo il mio reclamo
    if(reclamoAccettato == null) {
        Reclamo mio = (Reclamo) request.getAttribute("mioReclamo");
        if(mio != null && mio.getStato() == StatoReclamo.ACCETTATO) {
            reclamoAccettato = mio;
        }
    }

    // --- LOGICA RECUPERO CONTATTI CONTROPARTE ---
    Utente controparte = null;
    String etichettaControparte = "Utente";

    if (reclamoAccettato != null) {
        if (isOwner) {
            // Se sono il Finder -> Voglio i dati di chi ha fatto il reclamo (Owner dell'oggetto)
            @SuppressWarnings("unchecked")
            Map<Long, Utente> mappa = (Map<Long, Utente>) request.getAttribute("mappaRichiedenti");
            if (mappa != null) {
                controparte = mappa.get(reclamoAccettato.getIdUtenteRichiedente());
            }
            etichettaControparte = "Contatti Proprietario";
        } else {
            // Se sono il Richiedente -> Voglio i dati del Finder
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio_segnalazione.css?v=8">
</head>

<body>

<jsp:include page="/WEB-INF/jsp/navbar.jsp"/>

<div class="local-wrapper">

    <a href="${pageContext.request.contextPath}/index" class="local-back-link">
        <span class="material-icons">arrow_back</span> Torna alla Home
    </a>

    <%-- SEGNALAZIONE NON TROVATA --%>
    <c:if test="${segnalazione == null}">
        <div class="local-card empty-card" style="text-align:center; padding:60px 40px;">
            <span class="material-icons" style="font-size:60px; color:#ddd; margin-bottom:20px;">search_off</span>
            <h2>Segnalazione non trovata</h2>
            <p style="color:#666; margin-bottom:20px;">Potrebbe essere stata rimossa o il link non è corretto.</p>
            <a href="${pageContext.request.contextPath}/index" class="btn-primary" style="max-width:200px; margin:0 auto;">Vai alla Home</a>
        </div>
    </c:if>

    <%-- SEGNALAZIONE TROVATA --%>
    <c:if test="${segnalazione != null}">
    <div class="local-grid">

        <div class="main-content">
            <div class="local-card hero-card">
                <div class="image-container">
                    <c:choose>
                        <c:when test="${not empty segnalazione.immagine}">
                            <img src="${pageContext.request.contextPath}/segnalazione-img?id=${segnalazione.id}" class="local-hero-img" alt="Foto Oggetto">
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
                            <div class="icon-wrap"><span class="material-icons">calendar_today</span></div>
                            <div>
                                <span class="label">Data Ritrovamento</span>
                                <div class="value"><fmt:formatDate value="${segnalazione.dataRitrovamento}" pattern="dd MMMM yyyy"/></div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="icon-wrap"><span class="material-icons">location_on</span></div>
                            <div>
                                <span class="label">Luogo</span>
                                <div class="value">${segnalazione.luogoRitrovamento}</div>
                                <div class="sub-value">${segnalazione.citta} (${segnalazione.provincia})</div>
                            </div>
                        </div>
                    </div>

                    <div id="itemMap" class="mini-map"></div>

                    <% if (isDropPoint) { %>
                    <div class="delivery-box purple">
                        <div class="icon-box"><span class="material-icons">store</span></div>
                        <div>
                            <h4 style="margin:0; font-size:1.1rem; margin-bottom:4px;">Drop-Point Partner</h4>
                            <p style="margin:0; opacity:0.8;">L'oggetto è custodito presso un negozio autorizzato.</p>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="delivery-box green">
                        <div class="icon-box"><span class="material-icons">handshake</span></div>
                        <div>
                            <h4 style="margin:0; font-size:1.1rem; margin-bottom:4px;">Scambio Diretto</h4>
                            <p style="margin:0; opacity:0.8;">L'incontro avverrà direttamente tra Finder e Proprietario.</p>
                        </div>
                    </div>
                    <% } %>

                    <% if (reclamoAccettato != null && !isDropPoint && s.getStato() == StatoSegnalazione.APERTA) { %>
                    <div class="scambio-action-box">
                        <h3 class="scambio-title">
                            <span class="material-icons" style="color:#FB8C00;">published_with_changes</span>
                            Scambio in Corso
                        </h3>
                        <p style="color:#555; margin-bottom:20px; font-size:0.95rem;">
                            Il reclamo è stato accettato! Incontratevi nel luogo concordato.<br>
                            <strong>Per completare la restituzione e ricevere i punti, entrambi dovete confermare.</strong>
                        </p>

                        <div class="scambio-status">
                                <span class="status-pill <%= reclamoAccettato.isConfermaFinder() ? "done" : "" %>">
                                    <span class="material-icons" style="font-size:16px">
                                        <%= reclamoAccettato.isConfermaFinder() ? "check_circle" : "radio_button_unchecked" %>
                                    </span>
                                    Finder <%= reclamoAccettato.isConfermaFinder() ? "Pronto" : "In attesa" %>
                                </span>
                            <span class="status-pill <%= reclamoAccettato.isConfermaOwner() ? "done" : "" %>">
                                    <span class="material-icons" style="font-size:16px">
                                        <%= reclamoAccettato.isConfermaOwner() ? "check_circle" : "radio_button_unchecked" %>
                                    </span>
                                    Owner <%= reclamoAccettato.isConfermaOwner() ? "Pronto" : "In attesa" %>
                                </span>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>
        </div>

        <aside class="sidebar">

                <%-- VISTA RICHIEDENTE (Utente che cerca l'oggetto) --%>
            <c:if test="<%= !isOwner %>">
            <c:choose>

                <%-- CASO 1: RECLAMO ACCETTATO (Winner Box) --%>
            <c:when test="${not empty mioReclamo && mioReclamo.stato == 'ACCETTATO'}">
            <div class="winner-card">
                <div class="confetti-icon">🎉</div>
                <h3 style="color:#2E7D32; margin:0 0 8px 0;">Congratulazioni!</h3>
                <p style="color:#558B2F; margin:0; font-size:0.95rem;">L'oggetto è ufficialmente tuo.</p>

                    <%-- Drop Point Code --%>
                <c:if test="${not empty mioReclamo.codiceConsegna}">
                    <div class="code-box">
                        <div class="code-label">Codice Ritiro Drop-Point</div>
                        <div class="the-code">${mioReclamo.codiceConsegna}</div>
                    </div>
                    <p style="font-size:0.85rem; color:#666;">
                        Mostra questo codice al gestore del negozio per ritirare il tuo oggetto.
                    </p>
                </c:if>

                    <%-- AZIONE CONFERMA RICEZIONE (Solo se scambio diretto e aperto) --%>
                <% if (!isDropPoint && s.getStato() == StatoSegnalazione.APERTA && reclamoAccettato != null) { %>
                <div class="sidebar-action-separator"></div>
                <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                    <input type="hidden" name="action" value="conferma_scambio">
                    <input type="hidden" name="idReclamo" value="${mioReclamo.id}">
                    <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                    <% if (reclamoAccettato.isConfermaOwner()) { %>
                    <div style="margin-bottom:8px; font-size:0.95rem; color:#666; font-weight:600; text-align:center;">In attesa del Finder...</div>
                    <button type="button" class="btn-disabled status-pill done">
                        <span class="material-icons">check_circle</span> Ricezione Confermata
                    </button>
                    <% } else { %>
                    <div style="margin-bottom:10px; font-size:1rem; color:#333; font-weight:700; text-align:center;">
                        Clicca dopo aver ricevuto l'oggetto
                    </div>
                    <button type="submit" class="btn-primary btn-action-large">
                        <span class="material-icons">inventory</span> Conferma Ricezione
                    </button>
                    <% } %>
                </form>
                <% } %>

                    <%-- Contact Info (Dati Finder) --%>
                <% if (controparte != null) { %>
                <div class="contact-box-white">
                    <div style="font-size:0.8rem; text-transform:uppercase; color:#E65100; font-weight:700; margin-bottom:10px;">
                        <%= etichettaControparte %>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons">account_circle</span>
                        <strong><%= controparte.getNome() %> <%= controparte.getCognome() %></strong>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons">email</span>
                        <a href="mailto:<%= controparte.getEmail() %>"><%= controparte.getEmail() %></a>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons">phone</span>
                        <a href="tel:<%= controparte.getTelefono() %>"><%= controparte.getTelefono() %></a>
                    </div>
                </div>
                <% } %>
            </div>
            </c:when>

                <%-- CASO 2: NESSUN RECLAMO O IN ATTESA --%>
            <c:otherwise>
            <div class="sidebar-card">
                <h3 class="sidebar-title">È tuo questo oggetto?</h3>

                    <%-- BOX STATO RECLAMO MIGLIORATO --%>
                <c:if test="${not empty mioReclamo}">
                    <%-- Logica per classi CSS e Testi --%>
                    <c:set var="stClass" value="pending" />
                    <c:set var="stIcon" value="hourglass_top" />
                    <c:set var="stText" value="In Valutazione" />

                    <c:choose>
                        <c:when test="${mioReclamo.stato == 'ACCETTATO'}">
                            <c:set var="stClass" value="accepted" />
                            <c:set var="stIcon" value="check_circle" />
                            <c:set var="stText" value="Richiesta Accettata" />
                        </c:when>
                        <c:when test="${mioReclamo.stato == 'RIFIUTATO'}">
                            <c:set var="stClass" value="rejected" />
                            <c:set var="stIcon" value="cancel" />
                            <c:set var="stText" value="Richiesta Rifiutata" />
                        </c:when>
                    </c:choose>

                    <div class="user-status-card ${stClass}">
                        <div class="st-icon-wrapper">
                            <span class="material-icons">${stIcon}</span>
                        </div>
                        <div class="st-info">
                            <span class="st-label">Stato Richiesta</span>
                            <span class="st-value">${stText}</span>
                        </div>
                    </div>
                </c:if>

                <c:if test="${empty mioReclamo && segnalazione.stato == 'APERTA'}">
                    <p style="color:#666; font-size:0.9rem; margin-bottom:20px;">
                        Rispondi alle domande di sicurezza per dimostrare di essere il proprietario.
                    </p>
                    <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                        <input type="hidden" name="action" value="invia">
                        <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                        <label style="display:block; font-weight:600; font-size:0.85rem; margin-bottom:6px; color:#333;">
                            1. ${segnalazione.domandaVerifica1}
                        </label>
                        <input type="text" name="risposta1" required class="form-input" placeholder="La tua risposta...">

                        <label style="display:block; font-weight:600; font-size:0.85rem; margin-bottom:6px; color:#333;">
                            2. ${segnalazione.domandaVerifica2}
                        </label>
                        <input type="text" name="risposta2" required class="form-input" placeholder="La tua risposta...">

                        <button class="btn-primary">Invia Reclamo</button>
                    </form>
                </c:if>

                <c:if test="${segnalazione.stato == 'CHIUSA' && empty mioReclamo}">
                    <div style="text-align:center; color:#999; padding:20px 0;">
                        <span class="material-icons" style="font-size:40px;">lock</span>
                        <p>Segnalazione conclusa.</p>
                    </div>
                </c:if>
            </div>
            </c:otherwise>
            </c:choose>
            </c:if>

                <%-- VISTA FINDER (Proprietario Segnalazione) --%>
            <c:if test="<%= isOwner %>">
            <div class="sidebar-card">

                    <%-- AZIONE CONFERMA CONSEGNA (Per il finder, in alto) --%>
                <% if (!isDropPoint && s.getStato() == StatoSegnalazione.APERTA && reclamoAccettato != null) { %>
                <div class="action-highlight-box">
                    <h3 class="sidebar-title" style="color:#2E7D32;">Scambio in Corso</h3>
                    <p style="font-size:0.9rem; color:#555; margin-bottom:15px;">Hai accettato un reclamo. Incontra il proprietario per lo scambio.</p>

                    <form action="${pageContext.request.contextPath}/gestione-reclamo" method="post">
                        <input type="hidden" name="action" value="conferma_scambio">
                        <input type="hidden" name="idReclamo" value="<%= reclamoAccettato.getId() %>">
                        <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                                <% if (reclamoAccettato.isConfermaFinder()) { %>
                        <div style="margin-bottom:8px; font-size:0.95rem; color:#666; font-weight:600; text-align:center;">In attesa dell'Owner...</div>
                        <button type="button" class="btn-disabled status-pill done">
                            <span class="material-icons">check_circle</span> Consegna Confermata
                        </button>
                                <% } else { %>
                        <div style="margin-bottom:10px; font-size:1rem; color:#333; font-weight:700; text-align:center;">
                            Clicca dopo aver consegnato l'oggetto
                        </</div>
                <button type="submit" class="btn-primary btn-action-large" style="background: linear-gradient(135deg, #2E7D32, #1B5E20);">
                    <span class="material-icons">how_to_reg</span> Conferma Consegna
                </button>
                <% } %>
                </form>

                    <%-- Contact Info (Dati Owner/Richiedente) --%>
                <% if (controparte != null) { %>
                <div class="sidebar-action-separator"></div>
                <div class="contact-box-white" style="background: rgba(255,255,255,0.6);">
                    <div style="font-size:0.8rem; text-transform:uppercase; color:#2E7D32; font-weight:700; margin-bottom:10px;">
                        <%= etichettaControparte %>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons" style="color: #2E7D32;">account_circle</span>
                        <strong><%= controparte.getNome() %> <%= controparte.getCognome() %></strong>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons" style="color: #2E7D32;">email</span>
                        <a href="mailto:<%= controparte.getEmail() %>" style="color:#1B5E20;"><%= controparte.getEmail() %></a>
                    </div>
                    <div class="contact-row">
                        <span class="material-icons" style="color: #2E7D32;">phone</span>
                        <a href="tel:<%= controparte.getTelefono() %>" style="color:#1B5E20;"><%= controparte.getTelefono() %></a>
                    </div>
                </div>
                <% } %>

            </div>
                    <% } %>

            <h3 class="sidebar-title" style="display:flex; align-items:center; gap:8px;">
                <span class="material-icons" style="color:var(--primary);">admin_panel_settings</span>
                Gestione Reclami
            </h3>

            <c:if test="${empty reclamiRicevuti}">
            <div style="text-align:center; padding:30px 0; color:#999;">
                <span class="material-icons" style="font-size:40px; margin-bottom:10px;">inbox</span>
                <p>Non hai ancora ricevuto richieste.</p>
            </div>
            </c:if>

            <c:forEach var="r" items="${reclamiRicevuti}">
                <%-- MODIFICA QUI: Recupero Utente Richiedente dalla Mappa --%>
                <c:set var="userRichiedente" value="${mappaRichiedenti[r.idUtenteRichiedente]}" />

            <div class="claim-card">
                <div class="claim-header">
                                    <span style="display:flex; align-items:center; gap:6px;">
                                        <span class="material-icons" style="font-size:18px; color:#5F6368;">person</span>
                                        <span style="font-weight:700; color:#333; font-size:0.95rem;">
                                                ${userRichiedente.username}
                                        </span>
                                    </span>
                    <span style="font-size:0.8rem; color:#888;"><fmt:formatDate value="${r.dataRichiesta}" pattern="dd/MM HH:mm"/></span>
                </div>
                <div style="font-size:0.9rem; color:#555; margin-bottom:12px; line-height:1.5;">
                    <strong>R1:</strong> ${r.rispostaVerifica1}<br>
                    <strong>R2:</strong> ${r.rispostaVerifica2}
                </div>

                    <%-- Bottoni Azione --%>
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
                    <div style="background:#E8F5E9; color:#2E7D32; padding:8px; border-radius:8px; text-align:center; font-weight:700; font-size:0.9rem;">
                        <span class="material-icons" style="font-size:16px; vertical-align:middle;">check_circle</span> RECLAMO ACCETTATO
                        <c:if test="${not empty r.codiceConsegna}">
                            <div style="margin-top:4px; font-family:monospace; color:#1B5E20;">COD: ${r.codiceConsegna}</div>
                        </c:if>
                    </div>
                </c:if>

                <c:if test="${r.stato == 'RIFIUTATO'}">
                    <div style="background:#FFEBEE; color:#C62828; padding:8px; border-radius:8px; text-align:center; font-weight:700; font-size:0.9rem;">
                        RIFIUTATO
                    </div>
                </c:if>
            </div>
            </c:forEach>

            <div style="margin-top:24px; padding-top:24px; border-top:1px solid #eee;">
                <form action="${pageContext.request.contextPath}/dettaglio-segnalazione" method="post" onsubmit="return confirm('Sei sicuro di voler eliminare questa segnalazione? Questa azione è irreversibile.');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                    <button class="btn-danger">
                        <span class="material-icons">delete_forever</span> Elimina Segnalazione
                    </button>
                </form>
            </div>
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
            var map = L.map('itemMap', { zoomControl: false }).setView([lat, lon], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap'
            }).addTo(map);

            L.marker([lat, lon]).addTo(map)
                .bindPopup("<b>${segnalazione.titolo}</b><br>${segnalazione.luogoRitrovamento}")
                .openPopup();
        } else {
            var mapContainer = document.getElementById('itemMap');
            if(mapContainer) mapContainer.style.display = 'none';
        }
    });
</script>

</body>
</html>