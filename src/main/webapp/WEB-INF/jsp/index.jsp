<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Home</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
</head>
<body class="page-enter">

<nav class="navbar">
    <a href="${pageContext.request.contextPath}/index" class="brand">
        <div class="brand-icon">
            <img src="<%= request.getContextPath() %>/assets/images/logo.png" alt="logo_foundly">
        </div>
    </a>

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/index" class="nav-item">
            <span class="material-icons">home</span> Home
        </a>
        <a href="#" class="nav-item">
            <span class="material-icons">add_circle_outline</span> Crea Segnalazione
        </a>
        <a href="#" class="nav-item">
            <span class="material-icons">inventory</span> Segnalazioni
        </a>
        <a href="#" class="nav-item">
            <span class="material-icons">place</span> Drop-Point
        </a>
        <a href="#" class="nav-item">
            <span class="material-icons">emoji_events</span> Classifica
        </a>
    </div>

    <%
        Utente utenteLoggato = (Utente) session.getAttribute("utente");
        if (utenteLoggato != null) {
    %>
    <!-- Utente loggato: avatar + dropdown -->
    <div class="user-menu">
        <button type="button" class="user-avatar-btn">
            <div class="user-avatar"></div>
        </button>

        <div class="user-dropdown">
            <div class="user-dropdown-header">
                <div class="user-email"><%= utenteLoggato.getEmail() %></div>
                <div class="user-points-row">
                    <span class="points-label">punti</span>
                    <span class="points-value"><%= utenteLoggato.getPunteggio() %></span>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/profilo" class="user-dropdown-item">
                <span class="material-icons">person</span>
                <span>Profilo</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="user-dropdown-item user-dropdown-item-logout">
                <span class="material-icons">logout</span>
                <span>Logout</span>
            </a>
        </div>
    </div>
    <%
    } else {
    %>
    <!-- Utente NON loggato: mostra bottone Login/Registrati -->
    <a href="${pageContext.request.contextPath}/login" class="btn-login-nav">
        <span class="material-icons">login</span>
        <span>Accedi</span>
    </a>
    <%
        }
    %>
</nav>

<header class="hero">
    <h1>Hai perso qualcosa?</h1>
    <p>
        Foundly ti aiuta a ritrovare oggetti e animali smarriti grazie alla nostra
        community. Segnala, cerca e restituisci!
    </p>
    <a href="#" class="btn-cta-hero">
        <span class="material-icons">inventory_2</span>
        Crea Segnalazione
    </a>
</header>

<div class="search-wrapper">
    <div class="search-card">
        <form action="search" method="GET">
            <div class="search-top">
                <div class="input-box">
                    <span class="material-icons" style="color: #9e9e9e;">search</span>
                    <input type="text" name="q" placeholder="Cerca per titolo, descrizione o luogo...">
                </div>
                <button type="button" class="btn-cerca">
                    <span class="material-icons">search</span>
                    Cerca
                </button>
            </div>

            <div class="filters-row">
                <div class="filter-icon-container">
                    <span class="material-icons">filter_alt</span>
                </div>

                <select class="filter-select" name="tipo">
                    <option value="">Tutti i tipi</option>
                    <option value="oggetto">Oggetto</option>
                    <option value="animale">Animale</option>
                </select>

                <select class="filter-select" name="categoria">
                    <option value="">Tutte</option>
                    <option value="elettronica">Elettronica</option>
                    <option value="documenti">Documenti</option>
                    <option value="abbigliamento">Abbigliamento</option>
                    <option value="gioielli">Gioielli</option>
                    <option value="chiavi">Chiavi</option>
                    <option value="portafogli">Portafogli</option>
                    <option value="borse">Borse</option>
                    <option value="altro">Altro</option>
                </select>

                <select class="filter-select" name="citta">
                    <option value="">Tutte le città</option>
                    <option value="Roma">Roma</option>
                    <option value="Milano">Milano</option>
                    <option value="Napoli">Napoli</option>
                    <option value="Torino">Torino</option>
                    <option value="Firenze">Firenze</option>
                    <option value="Venezia">Venezia</option>
                    <option value="Bari">Bari</option>
                    <option value="Palermo">Palermo</option>
                </select>
            </div>
        </form>
    </div>
</div>

<section class="recent-section">
    <div class="section-header">
        <h2>Segnalazioni Recenti</h2>
        <span class="result-count">7 risultati</span>
    </div>

    <div class="cards-grid">

        <article class="card">
            <div class="card-badges">
                <span class="badge badge-active">Attiva</span>
                <span class="badge badge-type">Oggetto</span>
            </div>

            <div class="card-image-placeholder">
                <span class="material-icons">inventory_2</span>
            </div>

            <div class="card-footer">
                <div class="card-title">Telefono</div>
                <div class="card-info">
                    Salerno • 2 ore fa
                </div>
            </div>
        </article>

        <article class="card">
            <div class="card-badges">
                <span class="badge badge-active">Attiva</span>
                <span class="badge badge-type">Oggetto</span>
            </div>

            <div class="card-image-placeholder">
                <span class="material-icons">inventory_2</span>
            </div>

            <div class="card-footer">
                <div class="card-title">borraccia</div>
                <div class="card-info">
                    Fisciano • Ieri
                </div>
            </div>
        </article>

        <article class="card">
            <div class="card-badges">
                <span class="badge badge-active">Attiva</span>
                <span class="badge badge-type">Oggetto</span>
            </div>

            <div class="card-image-placeholder">
                <span class="material-icons">inventory_2</span>
            </div>

            <div class="card-footer">
                <div class="card-title">dad</div>
                <div class="card-info">
                    Baronissi • 3 giorni fa
                </div>
            </div>
        </article>

    </div>
</section>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const userMenu = document.querySelector(".user-menu");
        if (!userMenu) return;

        const btn = userMenu.querySelector(".user-avatar-btn");

        btn.addEventListener("click", function (e) {
            e.stopPropagation();
            userMenu.classList.toggle("open");
        });

        // chiude cliccando fuori
        document.addEventListener("click", function () {
            userMenu.classList.remove("open");
        });
    });
</script>

</body>
</html>