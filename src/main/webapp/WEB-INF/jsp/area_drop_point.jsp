<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.DropPoint" %>
<%@ page import="model.bean.enums.StatoDropPoint" %>

<%
    DropPoint dropPoint = (DropPoint) request.getAttribute("dropPoint");

    boolean hasDp      = (dropPoint != null);
    boolean isApproved = hasDp && dropPoint.getStato() == StatoDropPoint.APPROVATO;
    boolean isPending  = hasDp && dropPoint.getStato() == StatoDropPoint.IN_ATTESA;

    int depositiAttivi      = (request.getAttribute("depositiAttivi")      != null) ? (Integer) request.getAttribute("depositiAttivi")      : 0;
    int consegneCompletate  = (request.getAttribute("consegneCompletate")  != null) ? (Integer) request.getAttribute("consegneCompletate")  : 0;

    String msgDeposito    = (String) request.getAttribute("msgDeposito");
    String msgRitiro      = (String) request.getAttribute("msgRitiro");
    String erroreGenerale = (String) request.getAttribute("erroreGenerale");
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Area Drop-Point - Foundly</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/area_drop_point.css?v=3">
</head>
<body class="page-enter">

<jsp:include page="navbar.jsp" />

<main class="dp-area-main">

    <!-- HEADER -->
    <section class="dp-area-header">
        <div class="dp-area-title-block">
            <div class="dp-area-icon">
                <span class="material-icons">storefront</span>
            </div>
            <div>
                <h1 class="dp-area-title">Area Drop-Point</h1>
                <p class="dp-area-subtitle">Gestisci i codici di deposito e ritiro dei clienti.</p>
            </div>
        </div>

        <div class="dp-area-status-badge
             <%= !hasDp ? "status-none" : (isApproved ? "status-ok" : "status-pending") %>">
            <span class="material-icons">
                <%= !hasDp ? "info" : (isApproved ? "check_circle" : "hourglass_top") %>
            </span>
            <span>
                <% if (!hasDp) { %>
                    Nessun Drop-Point associato
                <% } else if (isApproved) { %>
                    Drop-Point approvato:
                    <strong><%= dropPoint.getNomeAttivita() %></strong>
                <% } else { %>
                    In attesa di approvazione
                <% } %>
            </span>
        </div>
    </section>

    <!-- CARDS CONTATORI -->
    <section class="dp-stats-row">
        <article class="dp-stat-card">
            <div class="dp-stat-label">Depositi Attivi</div>
            <div class="dp-stat-value warning"><%= depositiAttivi %></div>
            <span class="material-icons dp-stat-icon">inventory_2</span>
        </article>

        <article class="dp-stat-card">
            <div class="dp-stat-label">Consegne Completate</div>
            <div class="dp-stat-value success"><%= consegneCompletate %></div>
            <span class="material-icons dp-stat-icon">check_circle</span>
        </article>

    </section>

    <% if (erroreGenerale != null) { %>
    <div class="dp-alert dp-alert-error">
        <span class="material-icons">error_outline</span>
        <span><%= erroreGenerale %></span>
    </div>
    <% } %>

    <!-- BOTTONI GRANDI AZIONE -->
    <section class="dp-actions-grid">
        <button type="button"
                class="dp-big-action dp-action-deposito active"
                data-panel="panel-deposito">
            <div class="dp-big-icon-wrapper">
                <span class="material-icons">north_east</span>
            </div>
            <h2>Registra Deposito</h2>
            <p>Quando il Finder lascia l’oggetto nel tuo negozio, usa il codice di consegna per registrare il deposito.</p>
        </button>

        <button type="button"
                class="dp-big-action dp-action-ritiro"
                data-panel="panel-ritiro">
            <div class="dp-big-icon-wrapper">
                <span class="material-icons">south_west</span>
            </div>
            <h2>Registra Ritiro</h2>
            <p>Quando il proprietario arriva con il codice, conferma il ritiro per chiudere l’operazione.</p>
        </button>
    </section>

    <!-- PANNELLI SOTTO I BOTTONI -->

    <!-- PANEL DEPOSITO -->
    <section class="dp-panel-card active" id="panel-deposito">
        <h2 class="dp-panel-title">Registra Deposito</h2>
        <p class="dp-panel-subtitle">
            Inserisci il codice di consegna a 6 cifre che il Finder ti mostra dall’app.
        </p>

        <% if (!hasDp) { %>
        <div class="dp-alert dp-alert-warning">
            <span class="material-icons">info</span>
            <span>Devi registrare un Drop-Point per utilizzare questa funzione.</span>
        </div>
        <% } else if (!isApproved) { %>
        <div class="dp-alert dp-alert-warning">
            <span class="material-icons">hourglass_empty</span>
            <span>Il tuo Drop-Point è ancora in attesa di approvazione.</span>
        </div>
        <% } %>

        <% if (msgDeposito != null) { %>
        <div class="dp-alert dp-alert-info">
            <span class="material-icons">info</span>
            <span><%= msgDeposito %></span>
        </div>
        <% } %>

        <form method="post"
              action="${pageContext.request.contextPath}/area-drop-point"
              class="dp-form">
            <input type="hidden" name="action" value="deposito">

            <label class="dp-label">Codice di Consegna (6 cifre)</label>
            <input type="text"
                   name="codice"
                   maxlength="6"
                   class="dp-input"
                   placeholder="123456"
                    <%= (!isApproved ? "disabled" : "") %> />

            <button type="submit"
                    class="dp-btn-primary <%= (!isApproved ? "disabled" : "") %>"
                    <%= (!isApproved ? "disabled" : "") %>>
                Conferma Deposito
            </button>
        </form>
    </section>

    <!-- PANEL RITIRO -->
    <section class="dp-panel-card" id="panel-ritiro">
        <h2 class="dp-panel-title">Registra Ritiro</h2>
        <p class="dp-panel-subtitle">
            Usa lo stesso codice di consegna per confermare che l’oggetto è stato ritirato dal proprietario.
        </p>

        <% if (!hasDp) { %>
        <div class="dp-alert dp-alert-warning">
            <span class="material-icons">info</span>
            <span>Devi registrare un Drop-Point per utilizzare questa funzione.</span>
        </div>
        <% } else if (!isApproved) { %>
        <div class="dp-alert dp-alert-warning">
            <span class="material-icons">hourglass_empty</span>
            <span>Il tuo Drop-Point è ancora in attesa di approvazione.</span>
        </div>
        <% } %>

        <% if (msgRitiro != null) { %>
        <div class="dp-alert dp-alert-info">
            <span class="material-icons">info</span>
            <span><%= msgRitiro %></span>
        </div>
        <% } %>

        <form method="post"
              action="${pageContext.request.contextPath}/area-drop-point"
              class="dp-form">
            <input type="hidden" name="action" value="ritiro">

            <label class="dp-label">Codice di Consegna (6 cifre)</label>
            <input type="text"
                   name="codice"
                   maxlength="6"
                   class="dp-input"
                   placeholder="123456"
                    <%= (!isApproved ? "disabled" : "") %> />

            <button type="submit"
                    class="dp-btn-primary <%= (!isApproved ? "disabled" : "") %>"
                    <%= (!isApproved ? "disabled" : "") %>>
                Conferma Ritiro
            </button>
        </form>
    </section>

</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        // Toggle pannelli tramite bottoni grandi
        const actionButtons = document.querySelectorAll(".dp-big-action");
        const panels = document.querySelectorAll(".dp-panel-card");

        actionButtons.forEach(btn => {
            btn.addEventListener("click", () => {
                const targetId = btn.getAttribute("data-panel");

                // reset bottoni
                actionButtons.forEach(b => b.classList.remove("active"));
                btn.classList.add("active");

                // reset pannelli
                panels.forEach(p => p.classList.remove("active"));
                const targetPanel = document.getElementById(targetId);
                if (targetPanel) {
                    targetPanel.classList.add("active");
                    targetPanel.scrollIntoView({ behavior: "smooth", block: "start" });
                }
            });
        });
    });
</script>


</body>
</html>
