<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>

<%
    Utente utente = (Utente) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // ===== BADGE =====
    String rawBadge = utente.getBadge(); // OCCHIO_DI_FALCO / DETECTIVE / SHERLOCK_HOLMES

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
        badgeDisplay = sb.toString();
    }

    String badgeImg = "badge_default.png";
    if ("OCCHIO_DI_FALCO".equals(rawBadge)) {
        badgeImg = "badge_occhio_di_falco.png";
    } else if ("DETECTIVE".equals(rawBadge)) {
        badgeImg = "badge_detective.png";
    } else if ("SHERLOCK_HOLMES".equals(rawBadge)) {
        badgeImg = "badge_sherlock_holmes.png";
    }

    // ===== AVATAR ===== (di default nessuna immagine)
    String avatarPath = utente.getImmagineProfilo();
    boolean hasAvatar = false;

    if (avatarPath != null && !avatarPath.trim().isEmpty()) {
        try {
            java.io.File avatarFile = new java.io.File(
                    application.getRealPath("/" + avatarPath)
            );
            hasAvatar = avatarFile.exists();
            if (!hasAvatar) {
                avatarPath = null;
            }
        } catch (Exception e) {
            hasAvatar = false;
            avatarPath = null;
        }
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

        String navAvatarPath = null;
        boolean navHasAvatar = false;

        if (utenteLoggato != null) {
            navAvatarPath = utenteLoggato.getImmagineProfilo();
            if (navAvatarPath != null && !navAvatarPath.trim().isEmpty()) {
                try {
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

        if (utenteLoggato != null) {
    %>
    <div class="user-menu">
        <button type="button" class="user-avatar-btn">
            <% if (navHasAvatar) { %>
            <img src="<%= request.getContextPath() + "/" + navAvatarPath %>"
                 alt=""
                 class="user-avatar-img">
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
            <div class="profile-avatar-wrapper <%= hasAvatar ? "has-avatar" : "" %>" id="avatarWrapper">
                <div class="profile-avatar-large">
                    <% if (hasAvatar) { %>
                    <img
                            src="<%= request.getContextPath() + "/" + avatarPath %>"
                            alt=""
                            id="profileAvatarImg">
                    <% } else { %>
                    <img
                            alt=""
                            id="profileAvatarImg"
                            style="display:none;">
                    <% } %>
                </div>

                <!-- overlay rosso con cestino -->
                <div class="avatar-remove-overlay" id="avatarRemoveOverlay">
                    <span class="material-icons">delete</span>
                </div>

                <button type="button" class="avatar-edit-btn" id="avatarEditBtn" title="Cambia foto profilo">
                    <span class="material-icons">edit</span>
                </button>
            </div>

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

        <!-- FORM PROFILO (testo + avatar) -->
        <form method="post"
              action="${pageContext.request.contextPath}/profilo"
              id="profileEditForm"
              class="profile-edit-form"
              enctype="multipart/form-data">

            <!-- input file nascosto per l'avatar -->
            <input type="file"
                   name="avatar"
                   id="avatarInput"
                   accept="image/*"
                   class="avatar-file-input">

            <!-- flag rimozione avatar -->
            <input type="hidden"
                   name="removeAvatar"
                   id="removeAvatarField"
                   value="false">

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
            <!-- altri campi in futuro -->
        </div>
    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        // ===== dropdown utente in navbar =====
        const userMenu = document.querySelector(".user-menu");
        if (userMenu) {
            const btnAvatarMenu = userMenu.querySelector(".user-avatar-btn");
            btnAvatarMenu.addEventListener("click", function (e) {
                e.stopPropagation();
                userMenu.classList.toggle("open");
            });
            document.addEventListener("click", function () {
                userMenu.classList.remove("open");
            });
        }

        const editForm          = document.getElementById("profileEditForm");
        const editToggleBtn     = document.getElementById("editToggleBtn");
        const cancelEditBtn     = document.getElementById("cancelEditBtn");

        const avatarWrapper     = document.getElementById("avatarWrapper");
        const avatarEditBtn     = document.getElementById("avatarEditBtn");
        const avatarRemoveOv    = document.getElementById("avatarRemoveOverlay");
        const avatarInput       = document.getElementById("avatarInput");
        const avatarImg         = document.getElementById("profileAvatarImg");
        const removeAvatarField = document.getElementById("removeAvatarField");

        // attiva/disattiva modalità editing per i campi testo
        if (editForm && editToggleBtn && cancelEditBtn) {
            editToggleBtn.addEventListener("click", function () {
                editForm.classList.add("editing");
            });

            cancelEditBtn.addEventListener("click", function () {
                window.location.reload();
            });
        }

        // click sulla matita -> selezione file
        if (avatarEditBtn && avatarInput && avatarImg) {
            avatarEditBtn.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();

                if (!editForm.classList.contains("editing")) {
                    editForm.classList.add("editing");
                }
                avatarInput.click();
            });

            avatarInput.addEventListener("change", function () {
                const file = avatarInput.files[0];
                if (!file) return;

                const reader = new FileReader();
                reader.onload = function (ev) {
                    avatarImg.src = ev.target.result;
                    avatarImg.style.display = "block";
                    if (avatarWrapper) {
                        avatarWrapper.classList.add("has-avatar");
                    }
                    if (removeAvatarField) {
                        removeAvatarField.value = "false"; // stiamo caricando una nuova immagine
                    }
                };
                reader.readAsDataURL(file);
            });
        }

        // click sul cestino -> rimozione avatar (UI + flag per il server)
        if (avatarRemoveOv && avatarWrapper && avatarImg && avatarInput && removeAvatarField) {
            avatarRemoveOv.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();

                if (!editForm.classList.contains("editing")) {
                    editForm.classList.add("editing");
                }

                // segna la rimozione per il backend
                removeAvatarField.value = "true";

                // svuota eventuale file scelto
                avatarInput.value = "";

                // nasconde subito l'immagine
                avatarImg.src = "";
                avatarImg.style.display = "none";
                avatarWrapper.classList.remove("has-avatar");
            });
        }
    });
</script>

</body>
</html>