<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.DropPoint" %>
<%@ page import="model.bean.enums.StatoDropPoint" %>

<%
    DropPoint dropPoint = (DropPoint) session.getAttribute("dropPoint");
    if (dropPoint == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // Stato DP
    StatoDropPoint stato = dropPoint.getStato();
    boolean isApproved = (stato == StatoDropPoint.APPROVATO);
    boolean isPending  = (stato == StatoDropPoint.IN_ATTESA);
    boolean isRejected = (stato == StatoDropPoint.RIFIUTATO);

    String statoLabel = "In attesa di approvazione";
    String statoClass = "status-pending";
    if (isApproved) {
        statoLabel = "Drop-Point approvato";
        statoClass = "status-ok";
    } else if (isRejected) {
        statoLabel = "Drop-Point rifiutato";
        statoClass = "status-rejected";
    }

    // Logo / immagine attività (BLOB nel DB)
    boolean hasLogo = (dropPoint.getImmagine() != null &&
            dropPoint.getImmagine().length > 0);

    // URL della servlet che dovrà streammare il logo del Drop-Point (da implementare lato server)
    String logoUrl = request.getContextPath() + "/drop-point-avatar?dpId=" + dropPoint.getId();
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profilo Drop-Point - Foundly</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profilo.css">
</head>
<body class="page-enter">

<jsp:include page="navbar.jsp" />

<main class="profile-main">
    <section class="profile-card">
        <!-- HEADER: logo + nome attività + email + badge stato -->
        <div class="profile-header">
            <div class="profile-avatar-wrapper <%= hasLogo ? "has-avatar" : "" %>" id="avatarWrapper">
                <div class="profile-avatar-large">
                    <% if (hasLogo) { %>
                    <!-- Logo attività servito da servlet che legge il BLOB dal DB -->
                    <img
                            src="<%= logoUrl %>"
                            alt="Logo Drop-Point"
                            id="profileAvatarImg">
                    <% } else { %>
                    <!-- Nessun logo: img nascosta, cerchio arancione gestito via CSS -->
                    <img
                            src="<%= logoUrl %>"
                            alt="Logo Drop-Point"
                            id="profileAvatarImg"
                            style="display:none;">
                    <% } %>
                </div>

                <!-- overlay rosso con cestino -->
                <div class="avatar-remove-overlay" id="avatarRemoveOverlay">
                    <span class="material-icons">delete</span>
                </div>

                <button type="button" class="avatar-edit-btn" id="avatarEditBtn" title="Cambia logo attività">
                    <span class="material-icons">edit</span>
                </button>
            </div>

            <div>
                <h1 class="profile-title">
                    <%= dropPoint.getNomeAttivita() %>
                </h1>
                <p class="profile-subtitle">
                    <%= dropPoint.getEmail() %>
                </p>

                <div class="dp-area-status-badge <%= statoClass %>">
                    <span class="material-icons">
                        <%= isApproved ? "check_circle" : (isRejected ? "highlight_off" : "hourglass_top") %>
                    </span>
                    <span><%= statoLabel %></span>
                </div>
            </div>
        </div>

        <!-- BOX INFO VELOCI SU POSIZIONE E CONTATTI -->
        <div class="profile-badge-card">
            <div class="badge-medal">
                <span class="material-icons">location_on</span>
            </div>
            <div class="badge-texts">
                <span class="badge-label">Indirizzo</span>
                <span class="badge-name">
                    <%= dropPoint.getIndirizzo() %>,
                    <%= dropPoint.getCitta() %> (<%= dropPoint.getProvincia() %>)
                </span>
            </div>
        </div>

        <div class="profile-badge-card">
            <div class="badge-medal">
                <span class="material-icons">call</span>
            </div>
            <div class="badge-texts">
                <span class="badge-label">Telefono</span>
                <span class="badge-name">
                    <%= (dropPoint.getTelefono() != null && !dropPoint.getTelefono().isEmpty())
                            ? dropPoint.getTelefono()
                            : "Non specificato" %>
                </span>
            </div>
        </div>

        <div class="profile-badge-card">
            <div class="badge-medal">
                <span class="material-icons">schedule</span>
            </div>
            <div class="badge-texts">
                <span class="badge-label">Orari di apertura</span>
                <span class="badge-name">
                    <%= (dropPoint.getOrariApertura() != null && !dropPoint.getOrariApertura().isEmpty())
                            ? dropPoint.getOrariApertura()
                            : "Non specificati" %>
                </span>
            </div>
        </div>

        <!-- FORM PROFILO DROPPPOINT (dati anagrafici + logo) -->
        <form method="post"
              action="${pageContext.request.contextPath}/profilo-drop-point"
              id="profileEditForm"
              class="profile-edit-form"
              enctype="multipart/form-data">

            <!-- input file nascosto per il logo -->
            <input type="file"
                   name="logo"
                   id="avatarInput"
                   accept="image/*"
                   class="avatar-file-input">

            <!-- flag rimozione logo -->
            <input type="hidden"
                   name="removeLogo"
                   id="removeAvatarField"
                   value="false">

            <section class="profile-edit-card" id="profileEditCard">
                <div class="profile-edit-header">
                    <div class="profile-edit-header-left">
                        <span class="material-icons">storefront</span>
                        <span>Dati attività</span>
                    </div>
                    <button type="button" class="edit-main-button" id="editToggleBtn">
                        <span class="material-icons">edit</span>
                        <span>Modifica</span>
                    </button>
                </div>

                <!-- NOME ATTIVITÀ -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Nome Attività</span>
                        <span class="field-value view-mode" id="nomeAttivitaView">
                            <%= dropPoint.getNomeAttivita() %>
                        </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="nomeAttivita"
                               id="nomeAttivitaInput"
                               value="<%= dropPoint.getNomeAttivita() %>">
                    </div>
                </div>

                <!-- INDIRIZZO -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Indirizzo</span>
                        <span class="field-value view-mode" id="indirizzoView">
                            <%= dropPoint.getIndirizzo() %>
                        </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="indirizzo"
                               id="indirizzoInput"
                               value="<%= dropPoint.getIndirizzo() %>">
                    </div>
                </div>

                <!-- CITTÀ -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Città</span>
                        <span class="field-value view-mode" id="cittaView">
                            <%= dropPoint.getCitta() %>
                        </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="citta"
                               id="cittaInput"
                               value="<%= dropPoint.getCitta() %>">
                    </div>
                </div>

                <!-- PROVINCIA -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Provincia</span>
                        <span class="field-value view-mode" id="provinciaView">
                            <%= dropPoint.getProvincia() %>
                        </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="provincia"
                               id="provinciaInput"
                               value="<%= dropPoint.getProvincia() %>">
                    </div>
                </div>

                <!-- TELEFONO -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Telefono</span>
                        <span class="field-value view-mode" id="telefonoView">
                            <%= dropPoint.getTelefono() != null ? dropPoint.getTelefono() : "" %>
                        </span>
                        <input type="text"
                               class="field-input edit-mode"
                               name="telefono"
                               id="telefonoInput"
                               value="<%= dropPoint.getTelefono() != null ? dropPoint.getTelefono() : "" %>">
                    </div>
                </div>

                <!-- ORARI APERTURA -->
                <div class="profile-edit-row">
                    <div class="edit-field-text">
                        <span class="field-label">Orari di apertura</span>
                        <span class="field-value view-mode" id="orariView">
                            <%= dropPoint.getOrariApertura() != null ? dropPoint.getOrariApertura() : "" %>
                        </span>
                        <textarea
                                class="field-input edit-mode"
                                name="orariApertura"
                                id="orariInput"><%= dropPoint.getOrariApertura() != null ? dropPoint.getOrariApertura() : "" %></textarea>
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
            <!-- eventuali altri blocchi in futuro -->
        </div>
    </section>
</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {

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

        // click sulla matita -> selezione file logo
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

        // click sul cestino -> rimozione logo (UI + flag per il server)
        if (avatarRemoveOv && avatarWrapper && avatarImg && avatarInput && removeAvatarField) {
            avatarRemoveOv.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();

                if (!editForm.classList.contains("editing")) {
                    editForm.classList.add("editing");
                }

                removeAvatarField.value = "true";
                avatarInput.value = "";
                avatarImg.src = "";
                avatarImg.style.display = "none";
                avatarWrapper.classList.remove("has-avatar");
            });
        }
    });
</script>


</body>
</html>
