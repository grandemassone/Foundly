<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.enums.CategoriaOggetto" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crea Segnalazione - Foundly</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <style>
        /* Stili extra per il form dinamico */
        .hidden { display: none; }
        .form-section-title {
            font-size: 0.9rem;
            color: #E65100;
            text-transform: uppercase;
            font-weight: bold;
            margin: 20px 0 10px;
            border-bottom: 1px solid #FFE0B2;
            padding-bottom: 5px;
        }
    </style>
</head>
<body>

<div class="main-container">
    <div class="info-section">
        <div class="brand-header">
            <div class="brand-icon">
                <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="logo_foundly">
            </div>
        </div>
        <div class="hero-text">
            <h1>Nuova Segnalazione</h1>
            <p>Hai trovato qualcosa? Compila il modulo con i dettagli per aiutare il proprietario a ritrovarlo.</p>
            <div class="feature-pills">
                <span class="pill"><span class="material-icons">photo_camera</span> Foto</span>
                <span class="pill"><span class="material-icons">location_on</span> Geolocalizzazione</span>
                <span class="pill"><span class="material-icons">verified_user</span> Domande di Verifica</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card" style="max-width: 600px;">
            <h2>Dettagli Ritrovamento</h2>

            <% if (request.getAttribute("errore") != null) { %>
            <div style="background:#ffebee; color:#c62828; padding:10px; border-radius:8px; margin-bottom:15px;">
                <%= request.getAttribute("errore") %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/crea-segnalazione" method="post" enctype="multipart/form-data">

                <div class="input-group">
                    <label>Cosa hai trovato?</label>
                    <select name="tipo" id="tipoSelect" onchange="toggleFormFields()" required style="width:100%; padding:12px; border-radius:8px; border:1px solid #ccc;">
                        <option value="OGGETTO">Un Oggetto</option>
                        <option value="ANIMALE">Un Animale</option>
                    </select>
                </div>

                <div id="fieldsOggetto">
                    <div class="input-group">
                        <label>Categoria</label>
                        <select name="categoria" style="width:100%; padding:12px; border-radius:8px; border:1px solid #ccc;">
                            <% for(CategoriaOggetto c : CategoriaOggetto.values()) { %>
                            <option value="<%= c %>"><%= c %></option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <div id="fieldsAnimale" class="hidden">
                    <div class="input-row">
                        <div class="input-group">
                            <label>Specie</label>
                            <input type="text" name="specie" placeholder="es. Cane, Gatto">
                        </div>
                        <div class="input-group">
                            <label>Razza (Opzionale)</label>
                            <input type="text" name="razza" placeholder="es. Labrador">
                        </div>
                    </div>
                </div>

                <div id="deliverySection" class="hidden">
                    <div class="form-section-title">Modalità di Restituzione</div>

                    <div class="input-group">
                        <label>Come vuoi restituirlo?</label>

                        <div class="radio-container">
                            <label class="radio-label">
                                <input type="radio" name="modalita" value="DIRETTA" checked onclick="toggleDropPointList(false)">
                                <span>Consegna Diretta (A mano)</span>
                            </label>

                            <label class="radio-label">
                                <input type="radio" name="modalita" value="DROP_POINT" onclick="toggleDropPointList(true)">
                                <span>Tramite Drop-Point (Negozio)</span>
                            </label>
                        </div>
                    </div>

                    <div id="dropPointSelection" class="hidden" style="margin-top: 15px;">
                        <div class="input-group">
                            <label>Scegli il Drop-Point più vicino</label>
                            <select name="idDropPoint" id="selectDropPoint" style="width:100%; padding:12px; border-radius:8px; border:1px solid #ccc;" onchange="showDropPointDetails()">
                                <option value="">-- Seleziona un negozio --</option>
                                <c:forEach var="dp" items="${listaDropPoint}">
                                    <option value="${dp.id}"
                                            data-indirizzo="${dp.indirizzo}, ${dp.citta} (${dp.provincia})"
                                            data-orari="${dp.orariApertura}"
                                            data-tel="${dp.telefono}">
                                            ${dp.nomeAttivita} - ${dp.citta}
                                    </option>
                                </c:forEach>
                            </select>

                            <div id="dropPointDetails" style="display:none; margin-top:15px; padding:15px; background:#f9f9f9; border-radius:8px; border-left: 4px solid #FB8C00;">
                                <h4 style="margin:0 0 5px 0; color:#E65100;">Dettagli Punto di Ritiro</h4>
                                <p style="margin:5px 0; font-size:0.9rem;"><strong>📍 Indirizzo:</strong> <span id="dpAddr"></span></p>
                                <p style="margin:5px 0; font-size:0.9rem;"><strong>🕒 Orari:</strong> <span id="dpTime"></span></p>
                                <p style="margin:5px 0; font-size:0.9rem;"><strong>📞 Contatto:</strong> <span id="dpTel"></span></p>
                            </div>

                            <small style="color: #666;">Lasciando l'oggetto qui, il proprietario potrà ritirarlo con un codice sicuro.</small>
                        </div>
                    </div>
                </div>

                <div class="form-section-title">Informazioni Generali</div>

                <div class="input-group">
                    <label>Titolo Annuncio</label>
                    <input type="text" name="titolo" placeholder="es. Mazzo di chiavi con portachiavi rosso" required>
                </div>

                <div class="input-group">
                    <label>Descrizione Dettagliata</label>
                    <textarea name="descrizione" rows="4" style="width:100%; padding:10px; border-radius:8px; border:1px solid #ccc;" required></textarea>
                </div>

                <div class="input-group">
                    <label>Data Ritrovamento</label>
                    <input type="date" name="dataRitrovamento" required>
                </div>

                <div class="form-section-title">Posizione</div>

                <div class="input-group">
                    <label>Via / Piazza (Luogo esatto)</label>
                    <input type="text" name="luogo" placeholder="es. Via Roma 10" required>
                </div>

                <div class="input-row">
                    <div class="input-group">
                        <label>Città</label>
                        <input type="text" name="citta" required>
                    </div>
                    <div class="input-group">
                        <label>Provincia (Sigla)</label>
                        <input type="text" name="provincia" maxlength="2" placeholder="NA" required>
                    </div>
                </div>

                <div class="form-section-title">Sicurezza (Secure Claim)</div>
                <p style="font-size:0.8rem; color:#666; margin-bottom:10px;">
                    Imposta due domande a cui solo il vero proprietario può rispondere.
                </p>

                <div class="input-group">
                    <label>Domanda 1</label>
                    <input type="text" name="domanda1" placeholder="es. Di che colore è il portachiavi?" required>
                </div>

                <div class="input-group">
                    <label>Domanda 2</label>
                    <input type="text" name="domanda2" placeholder="es. Quante chiavi ci sono?" required>
                </div>

                <div class="input-group">
                    <label>Foto (Opzionale)</label>
                    <input type="file" name="immagine" accept="image/*">
                </div>

                <button type="submit" class="btn-primary mt-2">Pubblica Segnalazione</button>
            </form>

            <div style="text-align:center; margin-top:20px;">
                <a href="${pageContext.request.contextPath}/index" style="color:#666; text-decoration:none;">Annulla</a>
            </div>
        </div>
    </div>
</div>

<script>
    function toggleFormFields() {
        const tipo = document.getElementById("tipoSelect").value;
        const fieldsOggetto = document.getElementById("fieldsOggetto");
        const fieldsAnimale = document.getElementById("fieldsAnimale");
        const deliverySection = document.getElementById("deliverySection"); // Recupera la sezione consegna

        if (tipo === "OGGETTO") {
            fieldsOggetto.classList.remove("hidden");
            fieldsAnimale.classList.add("hidden");
            // MOSTRA la scelta consegna solo se è un oggetto
            if (deliverySection) deliverySection.classList.remove("hidden");
        } else {
            fieldsOggetto.classList.add("hidden");
            fieldsAnimale.classList.remove("hidden");
            // NASCONDI la scelta consegna se è un animale (sempre diretta)
            if (deliverySection) deliverySection.classList.add("hidden");
        }
    }

    // Nuova funzione per mostrare la tendina solo se si sceglie Drop-Point
    function toggleDropPointList(show) {
        const dpDiv = document.getElementById("dropPointSelection");
        if (show) {
            dpDiv.classList.remove("hidden");
            // Rendiamo la select obbligatoria se visibile
            const select = dpDiv.querySelector("select");
            if(select) select.setAttribute("required", "required");
        } else {
            dpDiv.classList.add("hidden");
            // Togliamo l'obbligo se nascosta
            const select = dpDiv.querySelector("select");
            if(select) select.removeAttribute("required");
        }
    }

    // Eseguiamo al caricamento per impostare lo stato corretto se la pagina viene ricaricata
    document.addEventListener("DOMContentLoaded", function() {
        toggleFormFields();
        // Controlla anche lo stato iniziale dei radio button
        const radioDrop = document.querySelector('input[name="modalita"][value="DROP_POINT"]');
        if (radioDrop && radioDrop.checked) {
            toggleDropPointList(true);
        } else {
            toggleDropPointList(false);
        }
    });

    function showDropPointDetails() {
        const select = document.getElementById("selectDropPoint");
        const detailsDiv = document.getElementById("dropPointDetails");

        // Prendi l'opzione selezionata
        const selectedOption = select.options[select.selectedIndex];

        // Se non ha valore (es. "-- Seleziona --"), nascondi tutto
        if (!select.value) {
            detailsDiv.style.display = "none";
            return;
        }

        // Recupera i dati dagli attributi data-...
        const indirizzo = selectedOption.getAttribute("data-indirizzo");
        const orari = selectedOption.getAttribute("data-orari");
        const tel = selectedOption.getAttribute("data-tel");

        // Riempi il box e mostralo
        document.getElementById("dpAddr").textContent = indirizzo;
        document.getElementById("dpTime").textContent = orari;
        document.getElementById("dpTel").textContent = tel;

        detailsDiv.style.display = "block";
    }
</script>

</body>
</html>