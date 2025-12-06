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

    <style>
        /* Piccola aggiunta specifica per evidenziare che siamo nella sezione Business */
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%); /* Stesso arancione */
        }
        /* Colore distintivo per il badge Drop-Point attivo */
        .pill.active {
            background-color: #fff;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            border: 1px solid #FF9800;
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
                Diventando un <strong>Drop-Point</strong>, offrirai un luogo sicuro per la restituzione di oggetti smarriti, aumentando la visibilità del tuo negozio.
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

                <div class="input-group">
                    <label for="orari">Orari di Apertura</label>
                    <input type="text" id="orari" name="orari" placeholder="Es. Lun-Sab 07:00 - 20:00" required>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Crea una password sicura" required>
                </div>

                <button type="submit" class="btn-primary mt-2">Invia Richiesta</button>

                <div class="back-link">
                    La tua attività è già registrata? <a href="${pageContext.request.contextPath}/login">Accedi qui</a>
                </div>
            </form>

        </div>
    </div>
</div>

</body>
</html>