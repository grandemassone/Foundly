<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%
    model.bean.DropPoint dp = (model.bean.DropPoint) session.getAttribute("dropPoint");
    model.bean.Utente u = (model.bean.Utente) session.getAttribute("utente");

    if (dp != null) {
        response.sendRedirect(request.getContextPath() + "/area-drop-point");
        return;
    }
    if (u == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Segnalazioni e Reclami - Foundly</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/segnalazioni_e_reclami.css?v=2">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
<body>

<jsp:include page="navbar.jsp" />

<div class="page-wrapper">

    <header class="page-header">
        <h1 class="page-title">Gestione Attività</h1>
        <p class="page-subtitle">
            Qui trovi le segnalazioni che hai pubblicato e i reclami che hai inviato.
        </p>
    </header>

    <div class="tabs">
        <button class="tab-button active" data-target="tab-segnalazioni">
            <span class="material-icons tab-icon">campaign</span>
            Le Mie Segnalazioni
        </button>
        <button class="tab-button" data-target="tab-reclami">
            <span class="material-icons tab-icon">back_hand</span>
            I Miei Reclami
        </button>
    </div>

    <!-- TAB SEGNALAZIONI -->
    <section id="tab-segnalazioni" class="tab-content active">
        <div class="section-header">
            <h2>Oggetti che hai trovato</h2>
            <a href="${pageContext.request.contextPath}/crea-segnalazione" class="btn-primary">
                <span class="material-icons">add</span> Nuova segnalazione
            </a>
        </div>

        <c:if test="${empty mieSegnalazioni}">
            <div class="empty-state">
                <span class="material-icons empty-icon">search_off</span>
                <h3>Nessuna segnalazione pubblicata</h3>
                <p>Non hai ancora segnalato oggetti ritrovati.</p>
            </div>
        </c:if>

        <c:if test="${not empty mieSegnalazioni}">
            <div class="cards-column">
                <c:forEach var="s" items="${mieSegnalazioni}">
                    <article class="card-row">

                        <!-- IMMAGINE A SINISTRA (SEGNALAZIONE) -->
                        <div class="card-thumb">
                            <c:choose>
                                <c:when test="${not empty s.immagine}">
                                    <div class="card-thumb-img-wrapper">
                                        <img
                                                src="${pageContext.request.contextPath}/segnalazione-img?id=${s.id}"
                                                alt="Immagine segnalazione ${s.titolo}"
                                                class="card-thumb-img">
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="card-thumb-placeholder">
                                        <span class="material-icons">inventory_2</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- CONTENUTO CENTRALE -->
                        <div class="card-main">
                            <div class="card-badges">
                                <span class="badge ${s.stato == 'CHIUSA' ? 'badge-closed' : 'badge-active'}">
                                        ${s.stato}
                                </span>
                            </div>

                            <h3 class="card-title">${s.titolo}</h3>

                            <p class="card-info">
                                <span class="material-icons card-info-icon">place</span>
                                    ${s.citta}
                                &nbsp;•&nbsp;
                                <fmt:formatDate value="${s.dataRitrovamento}" pattern="dd MMM" />
                            </p>
                        </div>

                        <!-- AZIONI A DESTRA -->
                        <div class="card-actions">
                            <a href="${pageContext.request.contextPath}/dettaglio-segnalazione?id=${s.id}"
                               class="btn-secondary">
                                Gestisci
                            </a>

                            <form method="post"
                                  action="${pageContext.request.contextPath}/elimina-segnalazione"
                                  class="delete-form">
                                <input type="hidden" name="id" value="${s.id}">
                                <button type="submit"
                                        class="icon-button"
                                        title="Elimina segnalazione"
                                        onclick="return confirm('Vuoi davvero eliminare questa segnalazione?');">
                                    <span class="material-icons">delete</span>
                                </button>
                            </form>
                        </div>

                    </article>
                </c:forEach>
            </div>
        </c:if>
    </section>

    <!-- TAB RECLAMI -->
    <section id="tab-reclami" class="tab-content">
        <div class="section-header">
            <h2>Oggetti che hai reclamato</h2>
        </div>

        <c:if test="${empty mieiReclami}">
            <div class="empty-state">
                <span class="material-icons empty-icon">support_agent</span>
                <h3>Nessun reclamo attivo</h3>
                <p>Non hai inviato richieste di restituzione per oggetti smarriti.</p>
            </div>
        </c:if>

        <c:if test="${not empty mieiReclami}">
            <div class="cards-column">
                <c:forEach var="r" items="${mieiReclami}">
                    <article class="card-row">

                        <!-- IMMAGINE DELLA SEGNALAZIONE COLLEGATA AL RECLAMO -->
                        <div class="card-thumb">
                            <c:choose>
                                <c:when test="${not empty r.immagineSegnalazione}">
                                    <div class="card-thumb-img-wrapper">
                                        <img
                                                src="${pageContext.request.contextPath}/segnalazione-img?id=${r.idSegnalazione}"
                                                alt="Immagine segnalazione reclamata"
                                                class="card-thumb-img">
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="card-thumb-placeholder">
                                        <span class="material-icons">help_outline</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- TESTO -->
                        <div class="card-main">
                            <div class="card-badges">
                                <span class="badge badge-reclamo">RECLAMO</span>
                                <span class="badge
                                    ${r.stato == 'ACCETTATO' ? 'badge-active' :
                                      (r.stato == 'RIFIUTATO' ? 'badge-closed' : 'badge-type')}">
                                        ${r.stato}
                                </span>
                            </div>

                            <h3 class="card-title">${r.titoloSegnalazione}</h3>

                            <p class="card-info">
                                Richiesto il:
                                <fmt:formatDate value="${r.dataRichiesta}" pattern="dd MMM yyyy" />
                            </p>
                        </div>

                        <div class="card-actions">
                            <a href="${pageContext.request.contextPath}/dettaglio-segnalazione?id=${r.idSegnalazione}"
                               class="btn-secondary">
                                Vedi Stato
                            </a>
                        </div>

                    </article>
                </c:forEach>
            </div>
        </c:if>
    </section>

</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const buttons = document.querySelectorAll(".tab-button");
        const tabs = document.querySelectorAll(".tab-content");

        buttons.forEach(btn => {
            btn.addEventListener("click", () => {
                buttons.forEach(b => b.classList.remove("active"));
                tabs.forEach(t => t.classList.remove("active"));

                btn.classList.add("active");
                const targetId = btn.dataset.target;
                document.getElementById(targetId).classList.add("active");
            });
        });
    });
</script>

</body>
</html>
