<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Registrazione Drop-Point</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">

    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin=""/>

    <style>
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
        }
        .pill.active {
            background-color: #fff;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            border: 1px solid #FF9800;
        }

        /* --- STILE PER LA MAPPA --- */
        #map {
            height: 250px; /* Altezza della mappa */
            width: 100%;
            border-radius: 8px;
            margin-bottom: 20px;
            border: 1px solid #dadce0;
            z-index: 1; /* Assicura che stia sotto header o dropdown se presenti */
        }

        .map-label {
            display: block;
            font-size: 0.9rem;
            color: var(--text-grey);
            margin-bottom: 8px;
            font-weight: 500;
        }

        /* Stile footer form */
        .form-footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;
            color: #666;
            font-size: 0.95rem;
        }
        .link-login {
            color: #FB8C00;
            font-weight: 600;
            text-decoration: none;
            padding: 6px 12px;
            border-radius: 4px;
            transition: all 0.3s ease;
        }
        .link-login:hover {
            background-color: #FFF3E0;
            color: #E65100;
        }
    </style>
</head>
<body>

<div class="main-container">

    <div class="info-section">
        <div class="brand-header">
            <div class="logo-placeholder">
                <span class="material-icons logo-icon">travel_explore</span> Foundly
            </div>
        </div>

        <div class="hero-text">
            <h1>Diventa un Partner</h1>
            <p>
                Trasforma la tua attività in un punto di riferimento per la community.
                Diventando un <strong>Drop-Point</strong>, offrirai un luogo sicuro per la restituzione di oggetti smarriti.
            </p>
            <div class="feature-pills">
                <span class="pill"><span class="material-icons">security</span> Secure Claim</span>
                <span class="pill active"><span class="material-icons">store</span> Drop-Points</span>
                <span class="pill"><span class="material-icons">emoji_events</span> Scoreboard</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Registra Attività</h2>
            <p class="subtitle">Unisciti alla rete Foundly</p>

            <% String errore = (String) request.getAttribute("errore"); %>
            <% if (errore != null) { %>
            <div style="background-color: #ffebee; color: #c62828; padding: 10px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9rem; text-align: center;">
                <span class="material-icons" style="font-size: 16px; vertical-align: middle;">error</span> <%= errore %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/registrazione-droppoint" method="post">

                <div class="input-group">
                    <label for="nomeAttivita">Nome Attività Commerciale</label>
                    <input type="text" id="nomeAttivita" name="nomeAttivita" placeholder="Es. Bar Centrale" required>
                </div>

                <div class="input-row">
                    <div class="input-group">
                        <label for="email">Email Aziendale</label>
                        <input type="email" id="email" name="email" placeholder="info@barcentrale.it" required>
                    </div>
                    <div class="input-group">
                        <label for="telefono">Telefono</label>
                        <input type="tel" id="telefono" name="telefono" placeholder="081 1234567" required>
                    </div>
                </div>

                <div class="input-group">
                    <label for="indirizzo">Indirizzo</label>
                    <input type="text" id="indirizzo" name="indirizzo" placeholder="Via Roma, 10" required>
                </div>

                <div class="input-row">
                    <div class="input-group" style="flex: 3;">
                        <label for="citta">Città</label>
                        <input type="text" id="citta" name="citta" placeholder="Milano" required>
                    </div>
                    <div class="input-group" style="flex: 1;">
                        <label for="provincia">Prov.</label>
                        <input type="text" id="provincia" name="provincia" maxlength="2" placeholder="MI" style="text-transform: uppercase;" required>
                    </div>
                </div>

                <span class="map-label">Posizione sulla mappa (Clicca per selezionare)</span>
                <div id="map"></div>

                <input type="hidden" id="latitudine" name="latitudine" required>
                <input type="hidden" id="longitudine" name="longitudine" required>

                <div class="input-group">
                    <label for="orari">Orari di Apertura</label>
                    <input type="text" id="orari" name="orari" placeholder="Es. Lun-Sab 07:00 - 20:00" required>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Crea una password sicura" required>
                </div>

                <button type="submit" class="btn-primary mt-2">Invia Richiesta</button>

                <div class="form-footer">
                    <span>La tua attività è già registrata?</span>
                    <a href="${pageContext.request.contextPath}/login" class="link-login">Accedi qui</a>
                </div>
            </form>

        </div>
    </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""></script>

<script>
    // 1. Inizializza la mappa su una posizione di default (es. Italia centrale o Roma)
    // Coordinate: [Latitudine, Longitudine], Zoom: 5 (vista Italia)
    var map = L.map('map').setView([41.9028, 12.4964], 6);

    // 2. Aggiungi il layer di OpenStreetMap (Il "disegno" della mappa)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    var marker;

    // 3. Gestisci il click sulla mappa
    map.on('click', function(e) {
        var lat = e.latlng.lat;
        var lng = e.latlng.lng;

        // Se c'è già un marker, rimuovilo (ne vogliamo solo uno)
        if (marker) {
            map.removeLayer(marker);
        }

        // Aggiungi il marker dove ha cliccato l'utente
        marker = L.marker([lat, lng]).addTo(map);

        // Aggiorna i campi input nascosti
        document.getElementById('latitudine').value = lat;
        document.getElementById('longitudine').value = lng;

        console.log("Posizione selezionata: " + lat + ", " + lng);
    });
</script>

</body>
</html>