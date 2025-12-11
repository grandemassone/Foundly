<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.enums.CategoriaOggetto" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%
    model.bean.DropPoint dp = (model.bean.DropPoint) session.getAttribute("dropPoint");
    model.bean.Utente u = (model.bean.Utente) session.getAttribute("utente");

    if (dp != null) {
        response.sendRedirect(request.getContextPath() + "/area-drop-point");
        return;
    }
    if (u == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    if (session.getAttribute("utente") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Crea Segnalazione - Foundly</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/crea_segnalazione.css">
</head>
<body>

<div class="main-container">

    <div class="info-section">
        <div class="brand-header">
            <div class="brand-icon">
                <a href="${pageContext.request.contextPath}/index">
                    <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="logo_foundly">
                </a>
            </div>
        </div>

        <div class="hero-text">
            <h1>Nuova Segnalazione</h1>
            <p>
                Hai trovato qualcosa? Compila il modulo dettagliato qui a fianco.
                Più informazioni fornisci, più facile sarà il ritrovamento.
            </p>

            <div style="margin-top: 40px;">
                <a href="${pageContext.request.contextPath}/index"
                   style="color: #E65100; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; font-weight: 500;">
                    <span class="material-icons">arrow_back</span> Torna alla Home
                </a>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card wide">

            <h2 class="form-page-title">Dettagli Ritrovamento</h2>
            <p class="form-page-subtitle">Inserisci le informazioni principali</p>

            <% if (request.getAttribute("errore") != null) { %>
            <div style="background:#ffebee; color:#c62828; padding:12px; border-radius:12px; margin-bottom:20px; display:flex; align-items:center; gap:10px;">
                <span class="material-icons">error_outline</span>
                <%= request.getAttribute("errore") %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/crea-segnalazione"
                  method="post"
                  enctype="multipart/form-data">

                <div class="form-grid">
                    <div>
                        <div class="form-group">
                            <label for="titolo" class="form-label">Titolo Annuncio</label>
                            <input type="text" id="titolo" name="titolo" class="form-input"
                                   required placeholder="Es. Trovato mazzo di chiavi">
                        </div>

                        <div class="form-group">
                            <label for="tipo_segnalazione" class="form-label">Cosa hai trovato?</label>
                            <select id="tipo_segnalazione" name="tipo_segnalazione"
                                    class="form-select" required onchange="toggleCampi()">
                                <option value="OGGETTO">Un Oggetto</option>
                                <option value="ANIMALE">Un Animale</option>
                            </select>
                        </div>

                        <div id="campi-oggetto">
                            <div class="form-group">
                                <label for="categoria" class="form-label">Categoria</label>
                                <select id="categoria" name="categoria" class="form-select">
                                    <% for(CategoriaOggetto c : CategoriaOggetto.values()) { %>
                                    <option value="<%= c %>"><%= c %></option>
                                    <% } %>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="modalita_consegna" class="form-label">Modalità Restituzione</label>
                                <select id="modalita_consegna" name="modalita_consegna"
                                        class="form-select" onchange="toggleCampi()">
                                    <option value="DIRETTA">Consegna a mano (Diretta)</option>
                                    <option value="DROP_POINT">Lascia a un Drop-Point (Negozio)</option>
                                </select>
                            </div>

                            <div id="container-drop-point" class="hidden">
                                <div class="form-group" style="background: #FFFDE7; padding: 15px; border-radius: 12px; border: 1px dashed #FBC02D;">
                                    <label for="idDropPoint" class="form-label" style="color: #F57F17;">Seleziona il Drop-Point</label>
                                    <select id="idDropPoint" name="idDropPoint" class="form-select"
                                            onchange="showDropPointDetails()">
                                        <option value="">-- Scegli un negozio --</option>
                                        <c:forEach var="dp" items="${listaDropPoint}">
                                            <option value="${dp.id}"
                                                    data-indirizzo="${dp.indirizzo}, ${dp.citta}"
                                                    data-orari="${dp.orariApertura}"
                                                    data-tel="${dp.telefono}">
                                                    ${dp.nomeAttivita} - ${dp.citta}
                                            </option>
                                        </c:forEach>
                                    </select>

                                    <div id="dp-details-box" class="dp-details-box hidden">
                                        <div class="dp-details-title">
                                            <span class="material-icons">store</span> Dettagli Punto
                                        </div>
                                        <div class="dp-info-row">
                                            <span class="material-icons dp-icon-small">place</span>
                                            <span id="dp-addr">Indirizzo...</span>
                                        </div>
                                        <div class="dp-info-row">
                                            <span class="material-icons dp-icon-small">schedule</span>
                                            <span id="dp-time">Orari...</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div id="campi-animale" class="hidden">
                            <div class="form-group">
                                <label for="specie" class="form-label">Specie</label>
                                <input type="text" id="specie" name="specie" class="form-input" placeholder="Es. Cane">
                            </div>
                            <div class="form-group">
                                <label for="razza" class="form-label">Razza (Opzionale)</label>
                                <input type="text" id="razza" name="razza" class="form-input" placeholder="Es. Labrador">
                            </div>
                        </div>
                    </div>

                    <div>
                        <div class="form-group">
                            <label for="descrizione" class="form-label">Descrizione Dettagliata</label>
                            <textarea id="descrizione" name="descrizione" class="form-textarea"
                                      required placeholder="Descrivi colore, marca, segni particolari..."></textarea>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Foto (Opzionale)</label>
                            <label for="immagine" class="file-upload-label">
                                <span class="material-icons file-upload-icon">cloud_upload</span>
                                <span>Carica un'immagine</span>
                            </label>
                            <input type="file" id="immagine" name="immagine"
                                   class="file-input-hidden" accept="image/*"
                                   onchange="showFileName(this)">
                            <div id="file-name-display"></div>
                        </div>
                    </div>
                </div>

                <hr style="margin: 25px 0;">

                <h3 style="font-size:1.1rem; color:#E65100; margin-bottom:15px;">📍 Dove e Quando</h3>

                <div class="form-grid">
                    <div class="form-group">
                        <label for="luogo_ritrovamento" class="form-label">Indirizzo / Luogo</label>
                        <input type="text" id="luogo_ritrovamento" name="luogo_ritrovamento"
                               class="form-input" required placeholder="Es. Via Roma 10">
                    </div>
                    <div class="form-group">
                        <label for="citta" class="form-label">Città</label>
                        <input type="text" id="citta" name="citta" class="form-input" required placeholder="Es. Milano">
                    </div>
                    <div class="form-group">
                        <label for="provincia" class="form-label">Provincia (Sigla)</label>
                        <input type="text" id="provincia" name="provincia"
                               class="form-input" maxlength="2" style="text-transform:uppercase;" required placeholder="MI">
                    </div>
                    <div class="form-group">
                        <label for="data_ritrovamento" class="form-label">Data Ritrovamento</label>
                        <input type="date" id="data_ritrovamento" name="dataRitrovamento"
                               class="form-input" required>
                    </div>
                </div>

                <hr style="margin: 25px 0;">

                <h3 style="font-size:1.1rem; color:#E65100; margin-bottom:10px;">🔒 Sicurezza (Secure Claim)</h3>
                <p style="font-size:0.85rem; color:#757575; margin-bottom:20px;">
                    Importa due domande a cui solo il vero proprietario può rispondere.
                </p>

                <div class="form-grid">
                    <div class="form-group">
                        <label for="domanda1" class="form-label">Domanda 1</label>
                        <input type="text" id="domanda1" name="domanda1" class="form-input"
                               required placeholder="Es. Di che colore è il portachiavi?">
                    </div>
                    <div class="form-group">
                        <label for="domanda2" class="form-label">Domanda 2</label>
                        <input type="text" id="domanda2" name="domanda2" class="form-input"
                               required placeholder="Es. C'è un graffio sul retro?">
                    </div>
                </div>

                <button type="submit" class="btn-submit-modern">Pubblica Segnalazione</button>

            </form>
        </div>
    </div>
</div>

<script>
    function toggleCampi() {
        const tipo = document.getElementById("tipo_segnalazione").value;
        const campiOggetto = document.getElementById("campi-oggetto");
        const campiAnimale = document.getElementById("campi-animale");

        const modalita = document.getElementById("modalita_consegna").value;
        const containerDP = document.getElementById("container-drop-point");
        const selectDP = document.getElementById("idDropPoint");

        if (tipo === "OGGETTO") {
            campiOggetto.classList.remove("hidden");
            campiAnimale.classList.add("hidden");

            if (modalita === "DROP_POINT") {
                containerDP.classList.remove("hidden");
                selectDP.setAttribute("required", "required");
            } else {
                containerDP.classList.add("hidden");
                selectDP.removeAttribute("required");
            }

        } else {
            campiOggetto.classList.add("hidden");
            campiAnimale.classList.remove("hidden");
            containerDP.classList.add("hidden");
            selectDP.removeAttribute("required");
        }
    }

    function showDropPointDetails() {
        const select = document.getElementById("idDropPoint");
        const detailsBox = document.getElementById("dp-details-box");

        if (select.value === "") {
            detailsBox.classList.add("hidden");
            return;
        }

        const selectedOpt = select.options[select.selectedIndex];
        const addr = selectedOpt.getAttribute("data-indirizzo");
        const time = selectedOpt.getAttribute("data-orari");

        document.getElementById("dp-addr").textContent = addr;
        document.getElementById("dp-time").textContent = time;

        detailsBox.classList.remove("hidden");
    }

    function showFileName(input) {
        const display = document.getElementById('file-name-display');
        if (input.files && input.files.length > 0) {
            display.textContent = input.files[0].name;
            display.style.color = "#2E7D32";
        } else {
            display.textContent = "";
        }
    }

    document.addEventListener("DOMContentLoaded", toggleCampi);
</script>

</body>
</html>
