<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Foundly - Conferma Recupero</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <style>
        .info-section {
            background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
        }

        /* Stile footer form come nello screenshot */
        .form-footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eee; /* linea orizzontale */
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

<script>
    function togglePwd(inputId, iconId) {
        const input = document.getElementById(inputId);
        const icon = document.getElementById(iconId);
        if (!input || !icon) return;

        const hidden = input.type === "password";
        input.type = hidden ? "text" : "password";
        icon.textContent = hidden ? "visibility_off" : "visibility";
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
            <h1>Inserisci il codice</h1>
            <p>
                Abbiamo inviato un codice di verifica all'indirizzo email specificato.
                Inserisci il codice ricevuto e scegli una nuova password.
            </p>
            <% String msg = (String) request.getAttribute("messaggio"); %>
            <% if (msg != null) { %>
            <p style="margin-top:10px;color:#2e7d32;font-size:0.9rem;"><%= msg %>
            </p>
            <% } %>
        </div>
    </div>

    <div class="auth-section">
        <div class="auth-card">
            <h2>Verifica Codice</h2>
            <p class="subtitle">Inserisci il codice e la nuova password</p>

            <% String errore = (String) request.getAttribute("errore"); %>
            <% if (errore != null) { %>
            <div style="background-color:#ffebee;color:#c62828;padding:10px;border-radius:8px;margin-bottom:20px;font-size:0.9rem;text-align:center;">
                <span class="material-icons" style="font-size:16px;vertical-align:middle;">error</span>
                <%= errore %>
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/recupero-password" method="post">
                <input type="hidden" name="step" value="codice">

                <div class="input-group">
                    <label for="codice">Codice di verifica</label>
                    <input type="text"
                           id="codice"
                           name="codice"
                           placeholder="es. 123456"
                           required
                           minlength="6"
                           maxlength="6">
                </div>

                <div class="input-group">
                    <label for="nuovaPassword">Nuova password</label>

                    <div class="password-wrapper">
                        <input type="password"
                               id="nuovaPassword"
                               name="nuovaPassword"
                               placeholder="Minimo 8 caratteri, con maiuscole, minuscole, numeri e simboli"
                               required>
                        <button type="button" class="toggle-password" aria-label="Mostra/Nascondi password"
                                onclick="togglePwd('nuovaPassword','iconNuovaPwd')">
                            <span class="material-icons" id="iconNuovaPwd">visibility</span>
                        </button>
                    </div>
                </div>

                <div class="input-group">
                    <label for="confermaPassword">Conferma password</label>

                    <div class="password-wrapper">
                        <input type="password"
                               id="confermaPassword"
                               name="confermaPassword"
                               placeholder="Ripeti la nuova password"
                               required>
                        <button type="button" class="toggle-password" aria-label="Mostra/Nascondi password"
                                onclick="togglePwd('confermaPassword','iconConfermaPwd')">
                            <span class="material-icons" id="iconConfermaPwd">visibility</span>
                        </button>
                    </div>
                </div>


                <button type="submit" class="btn-primary mt-2">Conferma</button>
            </form>

            <div class="form-footer">
                <span>Hai ricordato la password?</span>
                <a href="${pageContext.request.contextPath}/login" class="link-login">Accedi qui</a>
            </div>
        </div>
    </div>

</div>

</body>
</html>
