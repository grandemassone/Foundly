<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Login</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <style>
        /* --- BOTTONI REGISTRAZIONE SOTTO --- */
        .registration-area {
            margin-top: 24px;
            text-align: center;
        }

        .registration-area p {
            margin-bottom: 12px;
            color: #5f6368;
        }

        .reg-buttons {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .btn-register {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 10px 16px;
            border-radius: 999px;
            font-size: 0.95rem;
            font-weight: 500;
            text-decoration: none;
            border: 1px solid #FFB74D;
            background: #FFF7EC; /* colore richiesto */
            color: #E65100;
            transition: all 0.25s ease;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
        }

        .btn-register:hover {
            background: #FFE7CF; /* hover leggermente più caldo */
            border-color: #FFA726;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        }

        .btn-register .material-icons {
            font-size: 20px;
        }

        /* versione Drop Point, stesso colore base ma bordo un filo diverso */
        .btn-register.drop {
            border-color: #FFCC80;
        }

        .btn-register.drop:hover {
            background: #FFE0C0;
        }

        /* Affianca i bottoni su schermi medi/grandi */
        @media (min-width: 768px) {
            .reg-buttons {
                flex-direction: row;
                justify-content: center;
            }
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
            <h1>Hai perso qualcosa?</h1>
            <p>
                Foundly ti aiuta a ritrovare oggetti e animali smarriti grazie alla nostra community.
                Segnala uno smarrimento, cerca tra i ritrovamenti e restituisci in sicurezza tramite i nostri
                Drop-Point.
            </p>
            <div class="feature-pills">
                <span class="pill"><span class="material-icons">security</span> Secure Claim</span>
                <span class="pill"><span class="material-icons">store</span> Drop-Points</span>
                <span class="pill"><span class="material-icons">emoji_events</span> Scoreboard</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Accedi</h2>
            <p class="subtitle">Benvenuto su Foundly</p>

            <form action="${pageContext.request.contextPath}/login" method="post">

                <div class="input-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" placeholder="nome@esempio.com" required>
                </div>

                <div class="input-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Inserisci la tua password"
                           required>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/recupero-password" class="forgot-password">Password
                        dimenticata?</a>
                </div>

                <button type="submit" class="btn-primary">Accedi</button>
            </form>

            <div class="registration-area">
                <p>Non hai un account?</p>
                <div class="reg-buttons">
                    <a href="${pageContext.request.contextPath}/registrazione-utente" class="btn-register">
                        <span class="material-icons">person</span>
                        <span>Registrati come Utente</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/registrazione-droppoint" class="btn-register drop">
                        <span class="material-icons">storefront</span>
                        <span>Registrati come Drop Point</span>
                    </a>
                </div>
            </div>

        </div>
    </div>
</div>

</body>
</html>