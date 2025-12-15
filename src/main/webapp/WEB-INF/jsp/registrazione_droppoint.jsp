<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Registrazione Drop-Point</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin=""/>
</head>

<script>
    function togglePassword() {
        const pwd = document.getElementById("password");
        const icon = document.getElementById("togglePasswordIcon");
        if (!pwd || !icon) return;

        const isHidden = pwd.type === "password";
        pwd.type = isHidden ? "text" : "password";
        icon.textContent = isHidden ? "visibility_off" : "visibility";
    }
</script>

<body>

<div class="main-container">

    <div class="info-section">
        <div class="brand-header">
            <div class="brand-icon">
                <img src="<%= request.getContextPath() %>/assets/images/logo.png" alt="logo_foundly">
            </div>
        </div>

        <div class="hero-text">
            <h1>Diventa un Partner</h1>
            <p>
                Trasforma la tua attività in un punto di riferimento per la community.
                Diventando un <strong>Drop-Point</strong>, offrirai un luogo sicuro per la restituzione di oggetti
                smarriti.
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
                        <input type="text" id="provincia" name="provincia" maxlength="2" placeholder="MI"
                               style="text-transform: uppercase;" required>
                    </div>
                </div>

                <span class="map-label">Posizione sulla mappa (clicca per selezionare)</span>
                <div id="map"></div>

                <input type="hidden" id="latitudine" name="latitudine" required>
                <input type="hidden" id="longitudine" name="longitudine" required>

                <div class="input-group">
                    <label for="orari">Orari di Apertura</label>
                    <input type="text" id="orari" name="orari" placeholder="Es. Lun-Sab 07:00 - 20:00" required>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>

                    <div class="password-wrapper">
                        <input
                                type="password"
                                id="password"
                                name="password"
                                placeholder="Crea una password sicura"
                                required
                                minlength="8"
                                pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&._#-])[A-Za-z\d@$!%*?&._#-]{8,}$"
                                title="Almeno 8 caratteri, con una maiuscola, una minuscola, un numero e un carattere speciale (@$!%*?&._#-)"
                        >

                        <button type="button" class="toggle-password" aria-label="Mostra/Nascondi password"
                                onclick="togglePassword()">
                            <span class="material-icons" id="togglePasswordIcon">visibility</span>
                        </button>
                    </div>

                    <small id="passwordHelp" style="color:#777; font-size:0.8rem;">
                        Minimo 8 caratteri, almeno 1 maiuscola, 1 minuscola, 1 numero e 1 carattere speciale (@$!%*?&._#-).
                    </small>
                    <div id="passwordError" style="display:none; color:#c62828; font-size:0.8rem; margin-top:4px;">
                        La password non rispetta i requisiti indicati.
                    </div>
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
    // Inizializza la mappa su Italia / Roma
    var map = L.map('map').setView([41.9028, 12.4964], 6);

    // Layer OpenStreetMap
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    var marker = null;

    function setMarker(lat, lng) {
        if (marker) {
            marker.setLatLng([lat, lng]);
        } else {
            marker = L.marker([lat, lng], {draggable: true}).addTo(map);

            // Quando l'utente trascina il marker, aggiorna le coordinate
            marker.on('dragend', function (e) {
                var pos = e.target.getLatLng();
                document.getElementById('latitudine').value = pos.lat;
                document.getElementById('longitudine').value = pos.lng;
                console.log("Posizione selezionata (drag):", pos.lat, pos.lng);
            });
        }

        document.getElementById('latitudine').value = lat;
        document.getElementById('longitudine').value = lng;
    }

    // CLICK MANUALE SULLA MAPPA
    map.on('click', function (e) {
        var lat = e.latlng.lat;
        var lng = e.latlng.lng;
        setMarker(lat, lng);
        console.log("Posizione selezionata (click): " + lat + ", " + lng);
    });

    // ------- G E O C O D I N G  D A  I N D I R I Z Z O -------

    let geocodeTimeout = null;

    function geocodeAddress() {
        var indirizzoRaw = document.getElementById('indirizzo').value.trim();
        var citta = document.getElementById('citta').value.trim();
        var provincia = document.getElementById('provincia').value.trim();

        if (!indirizzoRaw || !citta || !provincia) {
            return;
        }

        // Prova a separare via e civico: "Via Cervinia, 38" -> "38 Via Cervinia"
        var street = indirizzoRaw;
        var match = indirizzoRaw.match(/(.+?)(?:,?\s+(\d+\w*\/?\w*))$/);
        if (match) {
            var via = match[1].trim();
            var civico = match[2].trim();
            street = civico + " " + via;
        }

        var params = new URLSearchParams({
            format: "json",
            limit: "1",
            addressdetails: "1",
            street: street,
            city: citta,
            county: provincia,
            country: "Italia"
        });

        var url = "https://nominatim.openstreetmap.org/search?" + params.toString();

        fetch(url, {
            method: "GET",
            headers: {
                "Accept-Language": "it"
            }
        })
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (!data || data.length === 0) {
                    console.warn("Indirizzo non trovato");
                    return;
                }

                var result = data[0];
                var lat = parseFloat(result.lat);
                var lng = parseFloat(result.lon);

                if (isNaN(lat) || isNaN(lng)) {
                    console.warn("Coordinate non valide dal geocoder");
                    return;
                }

                var addr = result.address || {};
                if (!addr.house_number) {
                    console.warn("Nessun house_number nei dati: posizione solo approssimativa sulla via.");
                }

                setMarker(lat, lng);
                map.setView([lat, lng], 16);

                console.log("Posizione selezionata (geocode): " + lat + ", " + lng,
                    "| street:", street, "| address:", addr);
            })
            .catch(function (error) {
                console.error("Errore nel geocoding:", error);
            });
    }

    function scheduleGeocode() {
        clearTimeout(geocodeTimeout);
        geocodeTimeout = setTimeout(geocodeAddress, 600);
    }

    // Lancia il geocoding quando l’utente compila indirizzo/città/provincia
    window.addEventListener('DOMContentLoaded', function () {
        ['indirizzo', 'citta', 'provincia'].forEach(function (id) {
            var el = document.getElementById(id);
            if (el) {
                el.addEventListener('blur', scheduleGeocode);
                el.addEventListener('keyup', function (e) {
                    if (!['Tab', 'Shift', 'Alt', 'Control', 'Meta'].includes(e.key)) {
                        scheduleGeocode();
                    }
                });
            }
        });
    });
</script>

<script>
    // Validazione password (stessi controlli della registrazione utente)
    (function () {
        const form = document.querySelector('.auth-card form');
        const passwordInput = document.getElementById('password');
        const passwordError = document.getElementById('passwordError');

        if (!form || !passwordInput || !passwordError) return;

        const passwordRegex =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&._#-])[A-Za-z\d@$!%*?&._#-]{8,}$/;

        function validatePassword() {
            const value = passwordInput.value || "";

            if (passwordRegex.test(value)) {
                passwordInput.style.borderColor = '#ccc';
                passwordError.style.display = 'none';
                return true;
            } else {
                if (value.length > 0) {
                    passwordInput.style.borderColor = '#c62828';
                    passwordError.style.display = 'block';
                } else {
                    passwordInput.style.borderColor = '#ccc';
                    passwordError.style.display = 'none';
                }
                return false;
            }
        }

        passwordInput.addEventListener('input', validatePassword);

        form.addEventListener('submit', function (e) {
            if (!validatePassword()) {
                e.preventDefault();
                passwordInput.focus();
            }
        });
    })();
</script>

</body>
</html>
