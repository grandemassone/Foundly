<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="java.util.List" %>
<%
    // Recupero dati dalla Servlet e dalla Sessione
    List<Utente> classifica = (List<Utente>) request.getAttribute("classifica");
    Utente utenteLoggato = (Utente) session.getAttribute("utente");

    // Logica Avatar Navbar (identica a index.jsp)
    String navAvatarPath = null;
    boolean navHasAvatar = false;
    if (utenteLoggato != null) {
        navAvatarPath = utenteLoggato.getImmagineProfilo();
        if (navAvatarPath != null && !navAvatarPath.trim().isEmpty()) {
            navHasAvatar = true; // Semplificazione per brevità, in prod controlla File.exists
        }
    }
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Classifica - Foundly</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/classifica.css">
</head>
<body class="page-enter">

<nav class="navbar">
    <a href="${pageContext.request.contextPath}/index" class="brand">
        <div class="brand-icon">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="logo_foundly">
        </div>
    </a>

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/index" class="nav-item">
            <span class="material-icons">home</span> Home
        </a>
        <a href="${pageContext.request.contextPath}/crea-segnalazione" class="nav-item">
            <span class="material-icons">add_circle_outline</span> Crea Segnalazione
        </a>
        <a href="${pageContext.request.contextPath}/le-mie-segnalazioni" class="nav-item">
            <span class="material-icons">inventory</span> Le mie Segnalazioni
        </a>
        <a href="${pageContext.request.contextPath}/drop-point" class="nav-item">
            <span class="material-icons">place</span> Drop-Point
        </a>
        <a href="${pageContext.request.contextPath}/classifica" class="nav-item active">
            <span class="material-icons">emoji_events</span> Classifica
        </a>
    </div>

    <% if (utenteLoggato != null) { %>
    <div class="user-menu">
        <button type="button" class="user-avatar-btn">
            <% if (navHasAvatar) { %>
            <img src="${pageContext.request.contextPath}/<%= navAvatarPath %>" alt="" class="user-avatar-img">
            <% } else { %>
            <div class="user-avatar-placeholder"></div>
            <% } %>
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
                <span class="material-icons">person</span><span>Profilo</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="user-dropdown-item user-dropdown-item-logout">
                <span class="material-icons">logout</span><span>Logout</span>
            </a>
        </div>
    </div>
    <% } else { %>
    <a href="${pageContext.request.contextPath}/login" class="btn-login-nav">
        <span class="material-icons">login</span><span>Accedi</span>
    </a>
    <% } %>
</nav>

<main class="classifica-main">

    <header class="classifica-header">
        <h1>Classifica Foundly</h1>
        <p>I migliori ritrovatori della community. Partecipa anche tu!</p>
    </header>

    <section class="classifica-card">
        <div class="table-responsive">
            <table class="ranking-table">
                <thead>
                <tr>
                    <th class="col-rank">#</th>
                    <th class="col-user">Utente</th>
                    <th class="col-badge">Badge</th>
                    <th class="col-points">Punti</th>
                </tr>
                </thead>
                <tbody>
                <% if (classifica == null || classifica.isEmpty()) { %>
                <tr>
                    <td colspan="4" class="empty-state">
                        Nessun utente presente in classifica.
                    </td>
                </tr>
                <% } else {
                    int rank = 1;
                    for (Utente u : classifica) {
                        // Logica Badge (Mapping Enum -> Testo/Classe)
                        String badgeName = "Novizio";
                        String badgeClass = "badge-base";
                        String rawBadge = u.getBadge();

                        if ("OCCHIO_DI_FALCO".equals(rawBadge)) {
                            badgeName = "Occhio di Falco"; badgeClass = "badge-bronze";
                        } else if ("DETECTIVE".equals(rawBadge)) {
                            badgeName = "Detective"; badgeClass = "badge-silver";
                        } else if ("SHERLOCK_HOLMES".equals(rawBadge)) {
                            badgeName = "Sherlock Holmes"; badgeClass = "badge-gold";
                        }

                        // Evidenzia la riga se è l'utente loggato
                        boolean isMe = (utenteLoggato != null && utenteLoggato.getId() == u.getId());                %>
                <tr class="<%= isMe ? "highlight-me" : "" %>">
                    <td class="col-rank">
                        <% if (rank == 1) { %>
                        <span class="material-icons rank-icon gold">emoji_events</span>
                        <% } else if (rank == 2) { %>
                        <span class="material-icons rank-icon silver">emoji_events</span>
                        <% } else if (rank == 3) { %>
                        <span class="material-icons rank-icon bronze">emoji_events</span>
                        <% } else { %>
                        <span class="rank-number"><%= rank %></span>
                        <% } %>
                    </td>

                    <td class="col-user">
                        <div class="user-info">
                            <% if (u.getImmagineProfilo() != null && !u.getImmagineProfilo().trim().isEmpty()) { %>
                            <img src="${pageContext.request.contextPath}/<%= u.getImmagineProfilo() %>" class="table-avatar-img">
                            <% } else { %>
                            <div class="table-avatar-placeholder"></div>
                            <% } %>

                            <div class="user-names">
                                <span class="user-fullname"><%= u.getNome() %> <%= u.getCognome() %></span>
                                <span class="user-username">@<%= u.getUsername() %></span>
                            </div>
                        </div>
                    </td>

                    <td class="col-badge">
                            <span class="badge-pill <%= badgeClass %>">
                                <%= badgeName %>
                            </span>
                    </td>

                    <td class="col-points">
                        <%= u.getPunteggio() %>
                    </td>
                </tr>
                <%
                            rank++;
                        }
                    } %>
                </tbody>
            </table>
        </div>
    </section>

    <section class="badges-legend">
        <h3>Sistema Badge</h3>
        <div class="badges-grid">
            <div class="badge-item">
                <span class="badge-pill badge-bronze">Occhio di Falco</span>
                <p>0 - 19 punti</p>
            </div>
            <div class="badge-item">
                <span class="badge-pill badge-silver">Detective</span>
                <p>20 - 49 punti</p>
            </div>
            <div class="badge-item">
                <span class="badge-pill badge-gold">Sherlock Holmes</span>
                <p>50+ punti</p>
            </div>
        </div>
    </section>

</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const userMenu = document.querySelector(".user-menu");
        if (!userMenu) return;
        const btn = userMenu.querySelector(".user-avatar-btn");
        if(btn){
            btn.addEventListener("click", function (e) {
                e.stopPropagation();
                userMenu.classList.toggle("open");
            });
        }
        document.addEventListener("click", function () {
            userMenu.classList.remove("open");
        });
    });
</script>

</body>
</html>