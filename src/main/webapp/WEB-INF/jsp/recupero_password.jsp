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
        /* Sfondo specifico per la sezione recupero (un gradiente leggermente diverso o uguale) */
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
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
            <h1>Problemi di accesso?</h1>
            <p>
                Non preoccuparti, succede a tutti.
                Inserisci l'email associata al tuo account e ti invieremo le istruzioni per reimpostare la password in sicurezza.
            </p>
            <div class="feature-pills">
                <span class="pill"><span class="material-icons">lock</span> Sicurezza garantita</span>
                <span class="pill"><span class="material-icons">mail</span> Link via Email</span>
            </div>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Recupero Password</h2>
            <p class="subtitle">Inserisci la tua email per continuare</p>

            <%-- Gestione Messaggi (Successo o Errore) --%>
            <% String errore = (String) request.getAttribute("errore"); %>
            <% String messaggio = (String) request.getAttribute("messaggio"); %>

            <% if (errore != null) { %>
            <div style="background-color: #ffebee; color: #c62828; padding: 10px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9rem; text-align: center;">
                <span class="material-icons" style="font-size: 16px; vertical-align: middle;">error</span> <%= errore %>
            </div>
            <% } %>

            <% if (messaggio != null) { %>
            <div style="background-color: #e8f5e9; color: #2e7d32; padding: 10px; border-radius: 8px; margin-bottom: 20px; font-size: 0.9rem; text-align: center;">
                <span class="material-icons" style="font-size: 16px; vertical-align: middle;">check_circle</span> <%= messaggio %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/recupero-password" method="post">

                <div class="input-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" placeholder="nome@esempio.com" required>
                </div>

                <button type="submit" class="btn-primary mt-2">Invia Istruzioni</button>

                <div class="form-footer">
                    <span>Hai ricordato la password?</span>
                    <a href="${pageContext.request.contextPath}/login" class="link-login">Torna al Login</a>
                </div>
            </form>

        </div>
    </div>
</div>

</body>
</html>