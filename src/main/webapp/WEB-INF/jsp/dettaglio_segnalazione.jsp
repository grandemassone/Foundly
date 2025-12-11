<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.enums.ModalitaConsegna" %>
<%@ page import="model.bean.SegnalazioneOggetto" %>

<%
    Utente utente = (Utente) session.getAttribute("utente");
    model.bean.Segnalazione s = (model.bean.Segnalazione) request.getAttribute("segnalazione");
    boolean isLogged = (utente != null);
    boolean isOwner = (isLogged && s != null && s.getIdUtente() == utente.getId());

    boolean isDropPoint = false;
    if (s instanceof SegnalazioneOggetto) {
        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
        if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
            isDropPoint = true;
        }
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <title>${segnalazione.titolo != null ? segnalazione.titolo : "Dettaglio"} - Foundly</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dettaglio_segnalazione.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>

<body>

<jsp:include page="/WEB-INF/jsp/navbar.jsp"/>

<div class="local-wrapper">

    <a href="${pageContext.request.contextPath}/index" class="local-back-link">
        <span class="material-icons">arrow_back</span>
        Torna alla Home
    </a>

    <%-- Messaggio se non trovata --%>
    <c:if test="${segnalazione == null}">
        <div class="local-card empty-card">
            <h2>Segnalazione non trovata</h2>
            <p>Potrebbe essere stata rimossa o non esistere più.</p>
            <a href="${pageContext.request.contextPath}/index" class="btn-primary">Vai alla Home</a>
        </div>
    </c:if>

    <%-- Contenuto se trovata --%>
    <c:if test="${segnalazione != null}">
        <div class="local-grid">

            <div class="main-content">
                <div class="local-card hero-card">
                    <div class="image-container">
                        <c:choose>
                            <c:when test="${not empty segnalazione.immagine}">
                                <img src="${pageContext.request.contextPath}/segnalazione-img?id=${segnalazione.id}"
                                     alt="${segnalazione.titolo}"
                                     class="local-hero-img">
                            </c:when>
                            <c:otherwise>
                                <div class="local-hero-placeholder">
                                    <span class="material-icons">image_not_supported</span>
                                </div>
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
                                <span class="tag-pill outline">
                                    <c:choose>
                                        <c:when test="${segnalazione.tipoSegnalazione == 'OGGETTO'}">
                                            ${segnalazione.categoria}
                                        </c:when>
                                        <c:otherwise>
                                            ${segnalazione.specie}
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <span class="date-text">
                                Pubblicato il
                                <fmt:formatDate value="${segnalazione.dataPubblicazione}" pattern="dd MMM yyyy"/>
                            </span>
                        </div>

                        <h1 class="local-title">${segnalazione.titolo}</h1>

                        <div class="description-box">
                            <p class="local-text">${segnalazione.descrizione}</p>
                        </div>

                        <div class="info-grid">
                            <div class="info-item">
                                <span class="material-icons">event</span>
                                <div>
                                    <span class="label">Data Ritrovamento</span>
                                    <span class="value">
                                        <fmt:formatDate value="${segnalazione.dataRitrovamento}"
                                                        pattern="dd MMMM yyyy"/>
                                    </span>
                                </div>
                            </div>
                            <div class="info-item">
                                <span class="material-icons">location_on</span>
                                <div>
                                    <span class="label">Luogo</span>
                                    <span class="value">${segnalazione.luogoRitrovamento}</span>
                                    <span class="sub-value">${segnalazione.citta} (${segnalazione.provincia})</span>
                                </div>
                            </div>
                        </div>

                        <div id="itemMap" class="mini-map"></div>

                        <% if (isDropPoint) { %>
                        <div class="delivery-box purple">
                            <div class="icon-box"><span class="material-icons">store</span></div>
                            <div>
                                <h4>Consegna tramite Drop-Point</h4>
                                <p>L'oggetto si trova in un negozio partner. Richiedi il claim per ottenere il codice di
                                    ritiro.</p>
                            </div>
                        </div>
                        <% } else { %>
                        <div class="delivery-box green">
                            <div class="icon-box"><span class="material-icons">handshake</span></div>
                            <div>
                                <h4>Consegna Diretta</h4>
                                <p>Incontro diretto tra Finder e Proprietario. Accordatevi in un luogo sicuro.</p>
                            </div>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>

            <aside class="sidebar">

                    <%-- UTENTE NON OWNER (potenziale proprietario) --%>
                <c:if test="<%= !isOwner %>">
                    <div class="sidebar-card action-card">
                        <h3 class="sidebar-title">È tuo questo oggetto?</h3>

                            <%-- NON LOGGATO: invito al login --%>
                        <c:if test="<%= !isLogged %>">
                            <div class="login-prompt">
                                <p>Accedi per dimostrare di essere il proprietario.</p>
                                <a href="${pageContext.request.contextPath}/login" class="btn-primary full-width">
                                    Accedi per Reclamare
                                </a>
                            </div>
                        </c:if>

                            <%-- LOGGATO --%>
                        <c:if test="<%= isLogged %>">
                            <c:choose>
                                <%-- ESISTE GIÀ UN MIO RECLAMO --%>
                                <c:when test="${not empty mioReclamo}">

                                    <c:if test="${mioReclamo.stato == 'IN_ATTESA'}">
                                        <div class="status-box pending">
                                            <span class="material-icons">hourglass_top</span>
                                            <h4>Richiesta Inviata</h4>
                                            <p>Il finder sta valutando le tue risposte.</p>
                                        </div>
                                    </c:if>

                                    <c:if test="${mioReclamo.stato == 'RIFIUTATO'}">
                                        <div class="status-box rejected">
                                            <span class="material-icons">error_outline</span>
                                            <h4>Richiesta Rifiutata</h4>
                                            <p>Le risposte non erano corrette.</p>
                                        </div>
                                    </c:if>

                                    <c:if test="${mioReclamo.stato == 'ACCETTATO'}">
                                        <div class="status-box winner">
                                            <div class="confetti-icon">🎉</div>
                                            <h4>Congratulazioni!</h4>
                                            <p>L'oggetto è tuo.</p>

                                            <c:if test="${not empty mioReclamo.codiceConsegna}">
                                                <div class="code-display">
                                                    <small>CODICE DI RITIRO</small>
                                                    <div class="the-code">${mioReclamo.codiceConsegna}</div>
                                                </div>

                                                <c:if test="${not empty dropPointRitiro}">
                                                    <div class="dp-pickup-info">
                                                        <div class="pickup-header">
                                                            <span class="material-icons">store</span>
                                                            <strong>Ritira presso:</strong>
                                                        </div>
                                                        <div class="dp-name">${dropPointRitiro.nomeAttivita}</div>
                                                        <div class="dp-address">
                                                                ${dropPointRitiro.indirizzo}, ${dropPointRitiro.citta}
                                                        </div>
                                                        <div class="dp-hours">
                                                            <span class="material-icons">schedule</span>
                                                                ${dropPointRitiro.orariApertura}
                                                        </div>
                                                        <a href="https://www.google.com/maps/search/?api=1&query=${dropPointRitiro.latitudine},${dropPointRitiro.longitudine}"
                                                           target="_blank" class="btn-map-nav">
                                                            <span class="material-icons">directions</span>
                                                            Naviga al Drop-Point
                                                        </a>
                                                    </div>
                                                </c:if>
                                            </c:if>

                                            <c:if test="${empty mioReclamo.codiceConsegna}">
                                                <div class="contact-card">
                                                    <div class="contact-header">
                                                        <span class="material-icons">person</span>
                                                        Contatti del Finder
                                                    </div>
                                                    <div class="contact-row">
                                                        <strong>Nome:</strong>
                                                            ${proprietarioSegnalazione.nome} ${proprietarioSegnalazione.cognome}
                                                    </div>
                                                    <div class="contact-row">
                                                        <strong>Email:</strong>
                                                        <a href="mailto:${proprietarioSegnalazione.email}">
                                                                ${proprietarioSegnalazione.email}
                                                        </a>
                                                    </div>
                                                    <div class="contact-row">
                                                        <strong>Tel:</strong>
                                                        <a href="tel:${proprietarioSegnalazione.telefono}">
                                                                ${proprietarioSegnalazione.telefono}
                                                        </a>
                                                    </div>
                                                    <p class="contact-note">
                                                        Contattalo per accordarvi sulla restituzione.
                                                    </p>
                                                </div>
                                            </c:if>
                                        </div>
                                    </c:if>

                                </c:when>

                                <%-- NESSUN MIO RECLAMO ANCORA --%>
                                <c:otherwise>
                                    <c:choose>
                                        <%-- SEGNALAZIONE APERTA: mostra form reclamo --%>
                                        <c:when test="${segnalazione.stato == 'APERTA'}">
                                            <p class="claim-intro">
                                                Rispondi alle domande di sicurezza impostate dal Finder.
                                            </p>
                                            <form action="${pageContext.request.contextPath}/gestione-reclamo"
                                                  method="post" class="claim-form">
                                                <input type="hidden" name="action" value="invia">
                                                <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                                                <div class="form-group">
                                                    <label class="q-label">
                                                        1. ${segnalazione.domandaVerifica1}
                                                    </label>
                                                    <input type="text" name="risposta1" required
                                                           class="form-input" placeholder="La tua risposta...">
                                                </div>
                                                <div class="form-group">
                                                    <label class="q-label">
                                                        2. ${segnalazione.domandaVerifica2}
                                                    </label>
                                                    <input type="text" name="risposta2" required
                                                           class="form-input" placeholder="La tua risposta...">
                                                </div>
                                                <button class="btn-primary full-width">Invia Reclamo</button>
                                            </form>
                                        </c:when>

                                        <%-- SEGNALAZIONE CHIUSA: avviso --%>
                                        <c:otherwise>
                                            <div class="status-box closed-public">
                                                <span class="material-icons">lock</span>
                                                <h4>Segnalazione Chiusa</h4>
                                                <p>Questo oggetto è stato già assegnato al legittimo proprietario.</p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </c:if>

                    <%-- OWNER (FINDER): pannello gestione reclami --%>
                <c:if test="<%= isOwner %>">
                    <div class="sidebar-card owner-card">
                        <div class="owner-header">
                            <span class="material-icons">admin_panel_settings</span>
                            <h3>Gestione Finder</h3>
                        </div>

                        <div class="claims-section">
                            <h4>Reclami Ricevuti (${reclamiRicevuti != null ? reclamiRicevuti.size() : 0})</h4>

                            <c:if test="${empty reclamiRicevuti}">
                                <div class="empty-claims">
                                    <span class="material-icons">inbox</span>
                                    <p>Nessun reclamo ricevuto ancora.</p>
                                </div>
                            </c:if>

                            <div class="claims-list">
                                <c:forEach var="r" items="${reclamiRicevuti}">
                                    <div class="claim-card">
                                        <div class="claim-header">
                                                <%-- MODIFICA QUI: Uso l'oggetto Utente dalla mappa --%>
                                            <c:set var="richiedente" value="${mappaRichiedenti[r.idUtenteRichiedente]}" />
                                            <strong>@${richiedente.username}</strong>

                                            <span class="date">
                                                <fmt:formatDate value="${r.dataRichiesta}" pattern="dd/MM"/>
                                            </span>
                                        </div>

                                        <div class="claim-answers">
                                            <div class="qa-pair">
                                                <small>Risposta 1:</small>
                                                <span>${r.rispostaVerifica1}</span>
                                            </div>
                                            <div class="qa-pair">
                                                <small>Risposta 2:</small>
                                                <span>${r.rispostaVerifica2}</span>
                                            </div>
                                        </div>

                                        <c:if test="${r.stato == 'IN_ATTESA'}">
                                            <div class="action-buttons-row">
                                                <form action="${pageContext.request.contextPath}/gestione-reclamo"
                                                      method="post" class="action-form">
                                                    <input type="hidden" name="action" value="accetta">
                                                    <input type="hidden" name="idReclamo" value="${r.id}">
                                                    <input type="hidden" name="idSegnalazione"
                                                           value="${segnalazione.id}">
                                                    <button class="btn-accept">Accetta</button>
                                                </form>

                                                <form action="${pageContext.request.contextPath}/gestione-reclamo"
                                                      method="post" class="action-form"
                                                      onsubmit="return confirm('Sei sicuro di voler rifiutare questa richiesta?');">
                                                    <input type="hidden" name="action" value="rifiuta">
                                                    <input type="hidden" name="idReclamo" value="${r.id}">
                                                    <input type="hidden" name="idSegnalazione"
                                                           value="${segnalazione.id}">
                                                    <button class="btn-reject">Rifiuta</button>
                                                </form>
                                            </div>
                                        </c:if>

                                            <%-- BOX VINCITORE PER IL FINDER --%>
                                        <c:if test="${r.stato == 'ACCETTATO'}">
                                            <div class="status-box winner" style="margin-top: 15px;">
                                                <div class="confetti-icon">🎉</div>
                                                <h4>Accettato!</h4>

                                                <c:if test="${not empty r.codiceConsegna}">
                                                    <div class="code-display">
                                                        <small>CODICE DI RITIRO</small>
                                                        <div class="the-code">${r.codiceConsegna}</div>
                                                    </div>
                                                </c:if>

                                                    <%-- DATI DEL PROPRIETARIO (richiedente vincente) --%>
                                                <c:if test="${not empty richiedente}">
                                                    <div class="contact-card owner-view">
                                                        <div class="contact-header">
                                                            <span class="material-icons">person</span>
                                                            Contatti Proprietario
                                                        </div>
                                                        <div class="contact-row">
                                                            <strong>Nome:</strong>
                                                                ${richiedente.nome} ${richiedente.cognome}
                                                        </div>
                                                        <div class="contact-row">
                                                            <strong>Email:</strong>
                                                            <a href="mailto:${richiedente.email}">
                                                                    ${richiedente.email}
                                                            </a>
                                                        </div>
                                                        <div class="contact-row">
                                                            <strong>Tel:</strong>
                                                            <a href="tel:${richiedente.telefono}">
                                                                    ${richiedente.telefono}
                                                            </a>
                                                        </div>
                                                        <p class="contact-note">
                                                            Contattalo per accordarvi sulla restituzione.
                                                        </p>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </c:if>

                                        <c:if test="${r.stato == 'RIFIUTATO'}">
                                            <div class="rejected-badge"
                                                 style="margin-top: 8px; color: #C62828; font-weight: 700; font-size: 0.9rem; text-align: center;">
                                                <span class="material-icons"
                                                      style="font-size: 16px; vertical-align: middle;">cancel</span>
                                                Rifiutato
                                            </div>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="danger-zone">
                            <form action="${pageContext.request.contextPath}/dettaglio-segnalazione"
                                  method="post"
                                  onsubmit="return confirm('Eliminare definitivamente questa segnalazione?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                <button class="btn-danger full-width">
                                    <span class="material-icons">delete</span>
                                    Elimina Segnalazione
                                </button>
                            </form>
                        </div>
                    </div>
                </c:if>

            </aside>

        </div>
    </c:if>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        var lat = ${segnalazione.latitudine != null ? segnalazione.latitudine : 'null'};
        var lon = ${segnalazione.longitudine != null ? segnalazione.longitudine : 'null'};

        if (lat && lon) {
            var map = L.map('itemMap').setView([lat, lon], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);

            L.marker([lat, lon]).addTo(map)
                .bindPopup("<b>${segnalazione.titolo}</b><br>${segnalazione.luogoRitrovamento}")
                .openPopup();
        } else {
            document.getElementById('itemMap').style.display = 'none';
        }
    });
</script>

</body>
</html>