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

    <!-- Stile snackbar -->
    <style>
        #snackbar {
            visibility: hidden;
            min-width: 280px;
            max-width: 90%;
            background-color: #43a047;
            color: #fff;
            text-align: left;
            border-radius: 8px;
            padding: 12px 16px;
            position: fixed;
            left: 50%;
            bottom: 24px;
            transform: translateX(-50%) translateY(20px);
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.25);
            opacity: 0;
            transition: opacity 0.3s ease, transform 0.3s ease;
            z-index: 9999;
        }

        #snackbar.show {
            visibility: visible;
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }

        #snackbar .material-icons {
            font-size: 18px;
        }
    </style>
</head>

<body>

<div class="main-container">

    <div class="info-section">
        <div class="brand-header">
            <div class="brand-icon">
                <img src="<%= request.getContextPath() %>/assets/images/logo.png" alt="logo_foundly">
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

                    <div class="password-wrapper">
                        <input type="password" id="password" name="password"
                               placeholder="Inserisci la tua password" required>

                        <button type="button" class="toggle-password" aria-label="Mostra/Nascondi password"
                                onclick="togglePassword()">
                            <span class="material-icons" id="togglePasswordIcon">visibility</span>
                        </button>
                    </div>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/recupero-password" class="forgot-password">
                        Password dimenticata?
                    </a>
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
                    <a href="${pageContext.request.contextPath}/index" class="btn-guest">
                        <span class="material-icons">arrow_back</span>
                        <span>Continua come ospite</span>
                    </a>
                </div>
            </div>

        </div>
    </div>
</div>

<!-- Snackbar (notifica in basso) -->
<div id="snackbar">
    <span class="material-icons">check_circle</span>
    <span class="snackbar-text" id="snackbarText"></span>
</div>

<!-- Script toggle password -->
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

<!-- Script snackbar -->
<script>
    function showSnackbar(message) {
        const snackbar = document.getElementById('snackbar');
        const textElem = document.getElementById('snackbarText');
        if (!snackbar || !textElem) return;

        textElem.textContent = message;
        snackbar.classList.add('show');

        setTimeout(function () {
            snackbar.classList.remove('show');
        }, 4000); // 4 secondi
    }
</script>

<%
    // Parametri di ritorno dai servlet di registrazione
    String regOkParam = request.getParameter("regOk");  // registrazione utente
    String dpOkParam  = request.getParameter("dpOk");   // registrazione Drop-Point

    String snackbarMsg = null;

    if ("1".equals(regOkParam)) {
        snackbarMsg = "Registrazione completata! Ora puoi effettuare il login.";
    } else if ("1".equals(dpOkParam)) {
        snackbarMsg = "Richiesta Drop-Point inviata! Puoi effettuare il login, un Admin approverà o rifiuterà la tua richiesta.";
    }

    if (snackbarMsg != null) {
%>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        showSnackbar("<%= snackbarMsg %>");
    });
</script>
<%
    }
%>

</body>
</html>
