<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.DropPoint" %>
<%@ page import="model.bean.enums.Ruolo" %>

<%
    String ctx = request.getContextPath();

    // Chi è loggato?
    Utente    utenteLoggatoNav   = (Utente)    session.getAttribute("utente");
    DropPoint dropPointLoggato   = (DropPoint) session.getAttribute("dropPoint");

    boolean isDropPoint = (dropPointLoggato != null);
    boolean isUtente    = (!isDropPoint && utenteLoggatoNav != null);

    // Avatar (unico flag per entrambi i casi)
    boolean navHasAvatar = false;
    String  navAvatarUrl = null;

    if (isUtente) {
        if (utenteLoggatoNav.getImmagineProfilo() != null &&
                utenteLoggatoNav.getImmagineProfilo().length > 0) {

            navHasAvatar = true;
            navAvatarUrl = ctx + "/avatar?userId=" + utenteLoggatoNav.getId();
        }
    } else if (isDropPoint) {
        if (dropPointLoggato.getImmagine() != null &&
                dropPointLoggato.getImmagine().length > 0) {

            navHasAvatar = true;
            navAvatarUrl = ctx + "/drop-point-avatar?dpId=" + dropPointLoggato.getId();
        }
    }
%>

<nav class="navbar">
    <!-- Logo -->
    <a href="<%= ctx %>/index" class="brand">
        <div class="brand-icon">
            <img src="<%= ctx %>/assets/images/logo.png" alt="logo_foundly">
        </div>
    </a>

    <!-- Link centrali -->
    <div class="nav-links">
        <% if (isDropPoint) { %>
        <a href="<%= ctx %>/area-drop-point" class="nav-item">
            <span class="material-icons">storefront</span> Area Drop-Point
        </a>
        <% } else { %>
        <a href="<%= ctx %>/index" class="nav-item">
            <span class="material-icons">home</span> Home
        </a>
        <a href="<%= ctx %>/crea-segnalazione" class="nav-item">
            <span class="material-icons">add_circle_outline</span> Crea Segnalazione
        </a>
        <a href="<%= ctx %>/le-mie-segnalazioni" class="nav-item">
            <span class="material-icons">inventory</span> Segnalazioni e Reclami
        </a>
        <a href="<%= ctx %>/drop-point" class="nav-item">
            <span class="material-icons">place</span> Drop-Point
        </a>
        <a href="<%= ctx %>/classifica" class="nav-item">
            <span class="material-icons">emoji_events</span> Classifica
        </a>
        <% } %>
    </div>

    <% if (isUtente || isDropPoint) { %>
    <div class="user-menu">
        <button type="button" class="user-avatar-btn">
            <% if (navHasAvatar) { %>
            <img src="<%= navAvatarUrl %>" alt="" class="user-avatar-img">
            <% } else { %>
            <div class="user-avatar-placeholder"></div>
            <% } %>
        </button>

        <div class="user-dropdown">
            <% if (isUtente) { %>
            <div class="user-dropdown-header">
                <div class="user-email"><%= utenteLoggatoNav.getEmail() %></div>
                <div class="user-points-row">
                    <span class="points-label">punti</span>
                    <span class="points-value"><%= utenteLoggatoNav.getPunteggio() %></span>
                </div>
            </div>

            <a href="<%= ctx %>/profilo" class="user-dropdown-item">
                <span class="material-icons">person</span><span>Profilo</span>
            </a>

            <% if (utenteLoggatoNav.getRuolo() == Ruolo.ADMIN) { %>
            <a href="<%= ctx %>/admin" class="user-dropdown-item">
                <span class="material-icons">admin_panel_settings</span><span>Area Admin</span>
            </a>
            <% } %>

            <% } else { %>
            <div class="user-dropdown-header">
                <div class="user-email"><%= dropPointLoggato.getEmail() %></div>
                <div class="user-dp-row">
                    <span class="user-dp-label">Drop-Point</span>
                    <span class="user-dp-name"><%= dropPointLoggato.getNomeAttivita() %></span>
                </div>
            </div>

            <a href="<%= ctx %>/profilo-drop-point" class="user-dropdown-item">
                <span class="material-icons">storefront</span><span>Profilo Drop-Point</span>
            </a>
            <% } %>

            <a href="<%= ctx %>/logout" class="user-dropdown-item user-dropdown-item-logout">
                <span class="material-icons">logout</span><span>Logout</span>
            </a>
        </div>
    </div>
    <% } else { %>
    <a href="<%= ctx %>/login" class="btn-login-nav">
        <span class="material-icons">login</span><span>Accedi</span>
    </a>
    <% } %>
</nav>

<script>
    (function () {
        // evita doppia inizializzazione (navbar incluso più volte o script duplicati)
        if (window.__FOUNDLY_NAVBAR_DROPDOWN__) return;
        window.__FOUNDLY_NAVBAR_DROPDOWN__ = true;

        document.addEventListener("click", function (e) {
            const userMenu = document.querySelector(".user-menu");
            if (!userMenu) return;

            const btn = userMenu.querySelector(".user-avatar-btn");
            if (btn && btn.contains(e.target)) {
                e.stopPropagation();
                userMenu.classList.toggle("open");
                return;
            }

            // click fuori -> chiudi
            userMenu.classList.remove("open");
        });

        // click dentro dropdown non deve chiudere prima di cliccare i link
        document.addEventListener("click", function (e) {
            const dropdown = document.querySelector(".user-menu .user-dropdown");
            if (dropdown && dropdown.contains(e.target)) {
                e.stopPropagation();
            }
        }, true);
    })();
</script>

