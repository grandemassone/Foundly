<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>

<%
    Utente utente = (Utente) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // Valore grezzo dal DB (ENUM)
    String rawBadge = utente.getBadge(); // OCCHIO_DI_FALCO / DETECTIVE / SHERLOCK_HOLMES

    // Nome “bello” da mostrare
    String badgeDisplay = "";
    if (rawBadge != null) {
        String[] parts = rawBadge.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        badgeDisplay = sb.toString();    // es. "Occhio Di Falco"
    }

    // Immagine del badge in base all'ENUM
    String badgeImg = "badge_default.png"; // fallback

    if ("OCCHIO_DI_FALCO".equals(rawBadge)) {
        badgeImg = "badge_occhio_di_falco.png";
    } else if ("DETECTIVE".equals(rawBadge)) {
        badgeImg = "badge_detective.png";
    } else if ("SHERLOCK_HOLMES".equals(rawBadge)) {
        badgeImg = "badge_sherlock_holmes.png";
    }
%>


<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profilo - Foundly</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profilo.css">
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
    <a href="${pageContext.request.contextPath}/login" class="btn-login-nav">
        <span class="material-icons">login</span>
        <span>Accedi</span>
    </a>
    <%
        }
    %>
</nav>

<main class="profile-main">
    <section class="profile-card">
        <!-- HEADER: avatar + nome + email -->
        <div class="profile-header">
            <div class="profile-avatar-large"></div>
            <div>
                <h1 class="profile-title">
                    <%= utente.getNome() %> <%= utente.getCognome() %>
                </h1>
                <p class="profile-subtitle">
                    <%= utente.getEmail() %>
                </p>
            </div>
        </div>

        <!-- BOX PUNTI -->
        <div class="profile-points-box">
            <div class="points-left">
                <span class="material-icons points-icon">star_rate</span>
                <span class="points-label-big">PUNTI TOTALI</span>
            </div>
            <div class="points-right">
                <span class="points-value-big">
                    <%= utente.getPunteggio() %>
                </span>
            </div>
        </div>

        <!-- BADGE -->
        <div class="profile-badge-card">
            <div class="badge-medal">
                <img src="<%= request.getContextPath() %>/assets/images/badges/<%= badgeImg %>"
                     alt="<%= badgeDisplay %>"
                     class="badge-medal-img">
            </div>
            <div class="badge-texts">
                <span class="badge-label">Badge</span>
                <span class="badge-name"><%= badgeDisplay %></span>
            </div>
        </div>

        <!-- BOX MODIFICA DATI (form) -->
        <form method="post"
              action="${pageContext.request.contextPath}/profilo"
              id="profileEditForm"
              class="profile-edit-form">

            <section class="profile-edit-card" id="profileEditCard">
                <div class="profile-edit-header">
                    <div class="profile-edit-header-left">
                        <span class="material-icons">manage_accounts</span>
                        <span>Dettagli account</span>
                    </div>
                    <button type="button" class="edit-main-button" id="editToggleBtn">
                        <span class="material-icons">edit</span>
                        <span>Modifica</span>
                    </button>
                </div>

                <!-- USERNAME -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Username</span>
                        <span class="field-value view-mode" id="usernameView">
                    <%= utente.getUsername() %>
                </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="username"
                               id="usernameInput"
                               value="<%= utente.getUsername() %>">
                    </div>
                </div>

                <!-- NOME -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Nome</span>
                        <span class="field-value view-mode" id="nomeView">
                    <%= utente.getNome() %>
                </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="nome"
                               id="nomeInput"
                               value="<%= utente.getNome() %>">
                    </div>
                </div>

                <!-- COGNOME -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Cognome</span>
                        <span class="field-value view-mode" id="cognomeView">
                    <%= utente.getCognome() %>
                </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="cognome"
                               id="cognomeInput"
                               value="<%= utente.getCognome() %>">
                    </div>
                </div>
            </section>

            <!-- PULSANTI SOTTO IL BOX, CENTRATI -->
            <div class="edit-actions">
                <button type="submit" class="btn-confirm">
                    Conferma Modifiche
                </button>
                <button type="button" class="btn-cancel" id="cancelEditBtn">
                    Annulla Modifiche
                </button>
            </div>
        </form>


        <div class="profile-grid">
            <!-- altri campi se servono -->
        </div>
    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        // dropdown utente
        const userMenu = document.querySelector(".user-menu");
        if (userMenu) {
            const btnAvatar = userMenu.querySelector(".user-avatar-btn");
            btnAvatar.addEventListener("click", function (e) {
                e.stopPropagation();
                userMenu.classList.toggle("open");
            });
            document.addEventListener("click", function () {
                userMenu.classList.remove("open");
            });
        }

        // logica modifica profilo
        const editForm = document.getElementById("profileEditForm");
        const editToggleBtn = document.getElementById("editToggleBtn");
        const cancelEditBtn = document.getElementById("cancelEditBtn");

        if (editForm && editToggleBtn && cancelEditBtn) {
            editToggleBtn.addEventListener("click", function () {
                editForm.classList.add("editing");
            });

            cancelEditBtn.addEventListener("click", function () {
                // torna ai valori originali
                window.location.reload();
            });
        }
    });
</script>


</body>
</html>
