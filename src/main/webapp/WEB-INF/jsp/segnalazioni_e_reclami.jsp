<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Segnalazioni e Reclami - Foundly</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/segnalazioni_e_reclami.css">
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
            <div class="cards-grid">
                <c:forEach var="s" items="${mieSegnalazioni}">
                    <article class="card">
                        <div class="card-badges">
                            <span class="badge ${s.stato == 'CHIUSA' ? 'badge-closed' : 'badge-active'}">
                                    ${s.stato}
                            </span>
                        </div>

                        <div class="card-image-placeholder"
                             style="${not empty s.immagine and s.immagine != 'default.png' ? 'background-image: url(' += pageContext.request.contextPath += '/' += s.immagine += '); background-size: cover;' : ''}">
                            <c:if test="${empty s.immagine or s.immagine == 'default.png'}">
                                <span class="material-icons">inventory_2</span>
                            </c:if>
                        </div>

                        <div class="card-footer">
                            <h3 class="card-title">${s.titolo}</h3>
                            <p class="card-info">
                                <span class="material-icons" style="font-size:14px; vertical-align:middle">place</span> ${s.citta} •
                                <fmt:formatDate value="${s.dataRitrovamento}" pattern="dd MMM" />
                            </p>
                            <a href="dettaglio-segnalazione?id=${s.id}" class="btn-secondary full-width" style="margin-top:10px; text-align:center;">
                                Gestisci
                            </a>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:if>
    </section>

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
            <div class="cards-grid">
                <c:forEach var="r" items="${mieiReclami}">
                    <article class="card">
                        <div class="card-badges">
                            <span class="badge" style="background-color: #E3F2FD; color: #1565C0;">
                                RECLAMO
                            </span>
                            <span class="badge ${r.stato == 'ACCETTATO' ? 'badge-active' : (r.stato == 'RIFIUTATO' ? 'badge-closed' : 'badge-type')}">
                                    ${r.stato}
                            </span>
                        </div>

                        <div class="card-image-placeholder"
                             style="${not empty r.immagineSegnalazione and r.immagineSegnalazione != 'default.png' ? 'background-image: url(' += pageContext.request.contextPath += '/' += r.immagineSegnalazione += '); background-size: cover;' : ''}">
                            <c:if test="${empty r.immagineSegnalazione or r.immagineSegnalazione == 'default.png'}">
                                <span class="material-icons">help_outline</span>
                            </c:if>
                        </div>

                        <div class="card-footer">
                            <h3 class="card-title">${r.titoloSegnalazione}</h3>

                            <p class="card-info">
                                Richiesto il:
                                <fmt:formatDate value="${r.dataRichiesta}" pattern="dd MMM yyyy" />
                            </p>

                            <a href="dettaglio-segnalazione?id=${r.idSegnalazione}" class="btn-secondary full-width" style="margin-top:10px; text-align:center;">
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
                // Rimuovi active da tutti
                buttons.forEach(b => b.classList.remove("active"));
                tabs.forEach(t => t.classList.remove("active"));

                // Aggiungi active al corrente
                btn.classList.add("active");
                const targetId = btn.dataset.target;
                document.getElementById(targetId).classList.add("active");
            });
        });
    });
</script>

</body>
</html>