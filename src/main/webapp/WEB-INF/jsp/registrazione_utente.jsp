<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Registrazione Utente</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">

    <style>
        /* Gradiente specifico per la colonna info */
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
        }

        /* Stile per il badge attivo nella lista feature */
        .pill.active {
            background-color: #fff;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            border: 1px solid #FF9800;
        }

        /* --- NUOVO STILE PER IL FOOTER DEL FORM --- */
        .form-footer {
            margin-top: 30px;           /* Distanza dal pulsante di registrazione */
            padding-top: 20px;          /* Spazio interno sopra il testo */
            border-top: 1px solid #eee; /* Linea divisoria sottile ed elegante */
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;                   /* Spazio tra testo e link */
            color: #666;
            font-size: 0.95rem;
        }

        .link-login {
            color: #FB8C00;             /* Arancione Foundly */
            font-weight: 600;
            text-decoration: none;
            padding: 6px 12px;
            border-radius: 4px;
            transition: all 0.3s ease;
        }

        .link-login:hover {
            background-color: #FFF3E0;  /* Sfondo leggero all'hover */
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
            <h1>Entra nella Community</h1>
            <p>
                Registrati come utente per segnalare oggetti ritrovati o reclamare ciò che hai perso.
                Aiutaci a costruire una rete di cittadini onesti e collaborativi.
            </p>
            <div class="feature-pills">
                <span class="pill active"><span class="material-icons">security</span> Secure Claim</span>
                <span class="pill"><span class="material-icons">store</span> Drop-Points</span>
                <span class="pill"><span class="material-icons">emoji_events</span> Scoreboard</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Crea Account</h2>
            <p class="subtitle">Inserisci i tuoi dati personali</p>

            <%-- Gestione Errori --%>
            <% String errore = (String) request.getAttribute("errore"); %>
            <% if (errore != null) { %>
            <div style="background-color: #ffebee; color: #c62828; padding: 10px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9rem; text-align: center;">
                <span class="material-icons" style="font-size: 16px; vertical-align: middle;">error</span> <%= errore %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/registrazione-utente" method="post">

                <div class="input-row">
                    <div class="input-group">
                        <label for="nome">Nome</label>
                        <input type="text" id="nome" name="nome" placeholder="Es. Mario" required>
                    </div>
                    <div class="input-group">
                        <label for="cognome">Cognome</label>
                        <input type="text" id="cognome" name="cognome" placeholder="Es. Rossi" required>
                    </div>
                </div>

                <div class="input-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" placeholder="Scegli un username" required>
                </div>

                <div class="input-row">
                    <div class="input-group" style="flex: 2;">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" placeholder="mario.rossi@email.com" required>
                    </div>
                    <div class="input-group" style="flex: 1;">
                        <label for="telefono">Telefono</label>
                        <input type="tel" id="telefono" name="telefono" placeholder="342..." required>
                    </div>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Minimo 8 caratteri" required>
                </div>

                <button type="submit" class="btn-primary mt-2">Registrati</button>

                <div class="form-footer">
                    <span>Hai già un account?</span>
                    <a href="${pageContext.request.contextPath}/login" class="link-login">Accedi qui</a>
                </div>

            </form>

        </div>
    </div>
</div>

</body>
</html>