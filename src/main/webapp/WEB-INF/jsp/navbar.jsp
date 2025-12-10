<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.enums.Ruolo" %>

<%
    // Recupero Utente dalla sessione
    Utente utenteLoggatoNav = (Utente) session.getAttribute("utente");

    // Logica Avatar
    String navAvatarPath = null;
    boolean navHasAvatar = false;

    if (utenteLoggatoNav != null) {
        navAvatarPath = utenteLoggatoNav.getImmagineProfilo();
        if (navAvatarPath != null && !navAvatarPath.trim().isEmpty()) {
            try {
                // Controllo se il file esiste fisicamente
                java.io.File navFile = new java.io.File(
                        application.getRealPath("/" + navAvatarPath)
                );
                navHasAvatar = navFile.exists();
                if (!navHasAvatar) {
                    navAvatarPath = null;
                }
            } catch (Exception e) {
                navHasAvatar = false;
                navAvatarPath = null;
            }
        }
    }
%>

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
            <span class="material-icons">inventory</span> Segnalazioni e Reclami
        </a>
        <a href="${pageContext.request.contextPath}/drop-point" class="nav-item">
            <span class="material-icons">place</span> Drop-Point
        </a>
        <a href="${pageContext.request.contextPath}/classifica" class="nav-item">
            <span class="material-icons">emoji_events</span> Classifica
        </a>
    </div>

    <% if (utenteLoggatoNav != null) { %>
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
                <div class="user-email"><%= utenteLoggatoNav.getEmail() %></div>
                <div class="user-points-row">
                    <span class="points-label">punti</span>
                    <span class="points-value"><%= utenteLoggatoNav.getPunteggio() %></span>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/profilo" class="user-dropdown-item">
                <span class="material-icons">person</span>
                <span>Profilo</span>
            </a>

            <% if (utenteLoggatoNav.getRuolo() == Ruolo.ADMIN) { %>
            <a href="${pageContext.request.contextPath}/admin" class="user-dropdown-item">
                <span class="material-icons">admin_panel_settings</span>
                <span>Area Admin</span>
            </a>
            <% } %>

            <a href="${pageContext.request.contextPath}/logout" class="user-dropdown-item user-dropdown-item-logout">
                <span class="material-icons">logout</span>
                <span>Logout</span>
            </a>
        </div>
    </div>
    <% } else { %>
    <a href="${pageContext.request.contextPath}/login" class="btn-login-nav">
        <span class="material-icons">login</span>
        <span>Accedi</span>
    </a>
    <% } %>
</nav>

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