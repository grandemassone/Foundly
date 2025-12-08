<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Recupero Password</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <style>
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
        }

        .pill.active {
            background-color: #fff;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            border: 1px solid #FF9800;
        }

        /* Stile footer form come nello screenshot */
        .form-footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee;   /* linea orizzontale */
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
            padding: 0 4px;
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
            <div class="brand-icon">
                <img src="<%= request.getContextPath() %>/assets/images/logo.png" alt="logo_foundly">
            </div>
        </div>

        <div class="hero-text">
            <h1>Problemi di accesso?</h1>
            <p>
                Non preoccuparti, succede a tutti.
                Inserisci l'email associata al tuo account e ti invieremo le istruzioni
                per reimpostare la password in sicurezza.
            </p>
            <div class="feature-pills">
                <span class="pill"><span class="material-icons">lock</span> Sicurezza garantita</span>
                <span class="pill"><span class="material-icons">mail</span> Codice via Email</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Recupero Password</h2>
            <p class="subtitle">Inserisci la tua email per continuare</p>

            <%-- Messaggi --%>
            <% String errore = (String) request.getAttribute("errore"); %>
            <% String messaggio = (String) request.getAttribute("messaggio"); %>

            <% if (errore != null) { %>
            <div style="background-color:#ffebee;color:#c62828;padding:10px;border-radius:8px;margin-bottom:20px;font-size:0.9rem;text-align:center;">
                <span class="material-icons" style="font-size:16px;vertical-align:middle;">error</span>
                <%= errore %>
            </div>
            <% } %>

            <% if (messaggio != null) { %>
            <div style="background-color:#e8f5e9;color:#2e7d32;padding:10px;border-radius:8px;margin-bottom:20px;font-size:0.9rem;text-align:center;">
                <span class="material-icons" style="font-size:16px;vertical-align:middle;">check_circle</span>
                <%= messaggio %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/recupero-password" method="post">
                <!-- IMPORTANTE per il servlet a due step -->
                <input type="hidden" name="step" value="email">

                <div class="input-group">
                    <label for="email">Email</label>
                    <input type="email"
                           id="email"
                           name="email"
                           placeholder="nome@esempio.com"
                           required>
                </div>

                <button type="submit" class="btn-primary mt-2">Invia codice</button>

                <div class="form-footer">
                    <span>Hai ricordato la password?</span>
                    <a href="${pageContext.request.contextPath}/login" class="link-login">Accedi qui</a>
                </div>
            </form>

        </div>
    </div>
</div>

</body>
</html>
