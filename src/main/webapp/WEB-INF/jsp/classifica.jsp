<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="java.util.List" %>
<%
    // Recupero dati dalla Servlet e dalla Sessione
    List<Utente> classifica = (List<Utente>) request.getAttribute("classifica");
    Utente utenteLoggato = (Utente) session.getAttribute("utente");

    // Helper per Avatar Navbar
    String navAvatarPath = null;
    boolean navHasAvatar = false;
    if (utenteLoggato != null) {
        navAvatarPath = utenteLoggato.getImmagineProfilo();
        if (navAvatarPath != null && !navAvatarPath.trim().isEmpty()) {
            navHasAvatar = true;
        }
    }
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Classifica - Foundly</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/classifica.css?v=3">
</head>
<body class="page-enter">

<nav class="navbar">
    <a href="${pageContext.request.contextPath}/index" class="brand">
        <div class="brand-icon">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="Foundly">
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
            <img src="${pageContext.request.contextPath}/<%= navAvatarPath %>" class="user-avatar-img">
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
        <h1>I Campioni di Foundly</h1>
        <p>Grazie a voi la community cresce e gli oggetti tornano a casa.</p>
    </header>

    <% if (classifica == null || classifica.isEmpty()) { %>
    <div class="empty-state">
        <span class="material-icons empty-icon">emoji_events</span>
        <h3>La classifica è ancora vuota!</h3>
        <p>Sii il primo a completare una restituzione.</p>
    </div>
    <% } else { %>

    <section class="podium-container">
        <%
            Utente first = classifica.size() > 0 ? classifica.get(0) : null;
            Utente second = classifica.size() > 1 ? classifica.get(1) : null;
            Utente third = classifica.size() > 2 ? classifica.get(2) : null;
        %>

        <% if (second != null) { %>
        <div class="podium-step step-silver">
            <div class="medal-icon">🥈</div>
            <div class="podium-avatar">
                <% if (second.getImmagineProfilo() != null && !second.getImmagineProfilo().isEmpty()) { %>
                <img src="${pageContext.request.contextPath}/<%= second.getImmagineProfilo() %>" class="podium-img">
                <% } else { %>
                <div class="podium-placeholder"><%= second.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= second.getNome() %> <%= second.getCognome() %></span>
                <span class="podium-points"><%= second.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">2</div>
        </div>
        <% } %>

        <% if (first != null) { %>
        <div class="podium-step step-gold">
            <div class="crown-icon">👑</div>
            <div class="podium-avatar">
                <% if (first.getImmagineProfilo() != null && !first.getImmagineProfilo().isEmpty()) { %>
                <img src="${pageContext.request.contextPath}/<%= first.getImmagineProfilo() %>" class="podium-img">
                <% } else { %>
                <div class="podium-placeholder"><%= first.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= first.getNome() %> <%= first.getCognome() %></span>
                <span class="podium-badge">🏆 Campione</span>
                <span class="podium-points"><%= first.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">1</div>
        </div>
        <% } %>

        <% if (third != null) { %>
        <div class="podium-step step-bronze">
            <div class="medal-icon">🥉</div>
            <div class="podium-avatar">
                <% if (third.getImmagineProfilo() != null && !third.getImmagineProfilo().isEmpty()) { %>
                <img src="${pageContext.request.contextPath}/<%= third.getImmagineProfilo() %>" class="podium-img">
                <% } else { %>
                <div class="podium-placeholder"><%= third.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= third.getNome() %> <%= third.getCognome() %></span>
                <span class="podium-points"><%= third.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">3</div>
        </div>
        <% } %>
    </section>

    <% if (classifica.size() > 3) { %>
    <section class="ranking-list-section">
        <h3>Top Contributors</h3>
        <div class="table-card">
            <table class="ranking-table">
                <thead>
                <tr>
                    <th class="col-rank">#</th>
                    <th class="col-user">Utente</th>
                    <th class="col-badge">Livello</th>
                    <th class="col-points">Punti</th>
                </tr>
                </thead>
                <tbody>
                <%
                    // Ciclo dal 4° elemento (index 3) fino al 15° o fine lista
                    for (int i = 3; i < Math.min(classifica.size(), 15); i++) {
                        Utente u = classifica.get(i);
                        int rank = i + 1;

                        // Badge Logic
                        String rawBadge = u.getBadge();
                        String badgeName = "Novizio";
                        String badgeClass = "bg-gray";

                        if ("OCCHIO_DI_FALCO".equals(rawBadge)) { badgeName = "Occhio di Falco"; badgeClass = "bg-bronze"; }
                        else if ("DETECTIVE".equals(rawBadge)) { badgeName = "Detective"; badgeClass = "bg-silver"; }
                        else if ("SHERLOCK_HOLMES".equals(rawBadge)) { badgeName = "Sherlock"; badgeClass = "bg-gold"; }

                        boolean isMe = (utenteLoggato != null && utenteLoggato.getId() == u.getId());
                %>
                <tr class="<%= isMe ? "highlight-me" : "" %>">
                    <td class="col-rank"><span class="rank-circle"><%= rank %></span></td>

                    <td class="col-user">
                        <div class="user-row-info">
                            <% if (u.getImmagineProfilo() != null && !u.getImmagineProfilo().isEmpty()) { %>
                            <img src="${pageContext.request.contextPath}/<%= u.getImmagineProfilo() %>" class="mini-avatar-img">
                            <% } else { %>
                            <div class="mini-avatar-placeholder"><%= u.getNome().charAt(0) %></div>
                            <% } %>

                            <div class="user-data">
                                <span class="u-name"><%= u.getNome() %> <%= u.getCognome() %></span>
                                <span class="u-username">@<%= u.getUsername() %></span>
                            </div>
                        </div>
                    </td>

                    <td class="col-badge">
                        <span class="badge-tag <%= badgeClass %>"><%= badgeName %></span>
                    </td>
                    <td class="col-points">
                        <%= u.getPunteggio() %>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
    <% } %>

    <% } %>

</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const userMenu = document.querySelector(".user-menu");
        if (userMenu) {
            const btn = userMenu.querySelector(".user-avatar-btn");
            btn.addEventListener("click", (e) => {
                e.stopPropagation();
                userMenu.classList.toggle("open");
            });
            document.addEventListener("click", () => userMenu.classList.remove("open"));
        }
    });
</script>

</body>
</html>