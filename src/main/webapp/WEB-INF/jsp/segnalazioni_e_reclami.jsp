<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Segnalazioni e Reclami - Foundly</title>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/segnalazioni_reclami.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
<body>

<jsp:include page="navbar.jsp" />

<div class="page-wrapper">

    <header class="page-header">
        <h1 class="page-title">Segnalazioni e Reclami</h1>
        <p class="page-subtitle">
            Gestisci tutte le segnalazioni di oggetti o animali smarriti e i reclami inviati a Foundly.
        </p>
    </header>

    <!-- Tabs -->
    <div class="tabs">
        <button class="tab-button active" data-target="tab-segnalazioni">
            <span class="material-icons tab-icon">report</span>
            Segnalazioni
        </button>
        <button class="tab-button" data-target="tab-reclami">
            <span class="material-icons tab-icon">feedback</span>
            Reclami
        </button>
    </div>

    <!-- TAB SEGNALAZIONI -->
    <section id="tab-segnalazioni" class="tab-content active">
        <div class="section-header">
            <h2>Le mie segnalazioni</h2>
            <a href="${pageContext.request.contextPath}/crea-segnalazione" class="btn-primary">
                <span class="material-icons">add</span>
                Nuova segnalazione
            </a>
        </div>

        <c:if test="${empty mieSegnalazioni}">
            <div class="empty-state">
                <span class="material-icons empty-icon">search_off</span>
                <h3>Nessuna segnalazione trovata</h3>
                <p>Inizia creando una nuova segnalazione per un oggetto o animale smarrito.</p>
                <a href="${pageContext.request.contextPath}/crea-segnalazione" class="btn-primary">
                    Crea segnalazione
                </a>
            </div>
        </c:if>

        <c:if test="${not empty mieSegnalazioni}">
            <div class="cards-grid">
                <c:forEach var="s" items="${mieSegnalazioni}">
                    <article class="card">
                        <div class="card-badges">
                            <c:choose>
                                <c:when test="${s.stato == 'CHIUSA'}">
                                    <span class="badge badge-chiusa">Chiusa</span>
                                </c:when>
                                <c:when test="${s.stato == 'APERTA'}">
                                    <span class="badge badge-aperta">Aperta</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-default">${s.stato}</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <h3 class="card-title">${s.titolo}</h3>
                        <p class="card-meta">
                            <span class="material-icons card-meta-icon">place</span>
                                ${s.citta}
                            <span class="dot-separator">•</span>
                            <span class="material-icons card-meta-icon">event</span>
                                ${s.dataRitrovamento}
                        </p>

                        <footer class="card-footer">
                            <a href="${pageContext.request.contextPath}/dettaglio-segnalazione?id=${s.id}"
                               class="btn-secondary">
                                Gestisci
                            </a>
                        </footer>
                    </article>
                </c:forEach>
            </div>
        </c:if>
    </section>

    <!-- TAB RECLAMI -->
    <section id="tab-reclami" class="tab-content">
        <div class="section-header">
            <h2>I miei reclami</h2>
            <a href="${pageContext.request.contextPath}/nuovo-reclamo" class="btn-primary">
                <span class="material-icons">add_comment</span>
                Nuovo reclamo
            </a>
        </div>

        <c:if test="${empty mieiReclami}">
            <div class="empty-state">
                <span class="material-icons empty-icon">support_agent</span>
                <h3>Nessun reclamo inviato</h3>
                <p>Se hai riscontrato un problema con il servizio, puoi aprire un nuovo reclamo.</p>
                <a href="${pageContext.request.contextPath}/nuovo-reclamo" class="btn-primary">
                    Invia reclamo
                </a>
            </div>
        </c:if>

        <c:if test="${not empty mieiReclami}">
            <div class="cards-grid">
                <c:forEach var="r" items="${mieiReclami}">
                    <article class="card">
                        <div class="card-badges">
                            <span class="badge badge-default">
                                    ${r.stato}
                            </span>
                        </div>

                        <h3 class="card-title">
                                ${r.oggetto}
                        </h3>

                        <p class="card-meta">
                            <span class="material-icons card-meta-icon">label</span>
                                ${r.categoria}
                            <span class="dot-separator">•</span>
                            <span class="material-icons card-meta-icon">event</span>
                                ${r.dataCreazione}
                        </p>

                        <footer class="card-footer">
                            <a href="${pageContext.request.contextPath}/dettaglio-reclamo?id=${r.id}"
                               class="btn-secondary">
                                Dettagli
                            </a>
                        </footer>
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
                const targetTab = document.getElementById(targetId);
                if (targetTab) {
                    targetTab.classList.add("active");
                }
            });
        });
    });
</script>

</body>
</html>
