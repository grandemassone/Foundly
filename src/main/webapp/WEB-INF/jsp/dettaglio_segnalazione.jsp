<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.enums.ModalitaConsegna" %>
<%@ page import="model.bean.SegnalazioneOggetto" %>

<%-- Recupero dati sessione e request --%>
<%
    Utente utente = (Utente) session.getAttribute("utente");
    model.bean.Segnalazione s = (model.bean.Segnalazione) request.getAttribute("segnalazione");
    boolean isLogged = (utente != null);
    boolean isOwner = (isLogged && s != null && s.getIdUtente() == utente.getId());

    boolean isDropPoint = false;
    if (s instanceof SegnalazioneOggetto) {
        SegnalazioneOggetto so = (SegnalazioneOggetto) s;
        if (so.getModalitaConsegna() == ModalitaConsegna.DROP_POINT) {
            isDropPoint = true;
        }
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <title>${segnalazione.titolo != null ? segnalazione.titolo : "Dettaglio"} - Foundly</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">

    <style>
        /* Reset specifico per il wrapper locale */
        .ds-wrapper {
            max-width: 1100px;
            width: 90%;          /* Lascia un po' di margine sui lati su mobile */
            margin: 40px auto;   /* AUTO centra orizzontalmente */
            font-family: 'Roboto', sans-serif;
            color: #202124;
            display: block;      /* Assicura che sia un blocco */
        }

        /* Link indietro */
        .ds-back-link {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
            color: #5F6368;
            font-weight: 500;
            margin-bottom: 20px;
            font-size: 0.95rem;
            cursor: pointer;
        }
        .ds-back-link:hover { color: #FB8C00; }

        /* Griglia Layout */
        .ds-grid {
            display: grid;
            grid-template-columns: 2fr 1fr; /* 2 parti sinistra, 1 parte destra */
            gap: 40px;
            align-items: start;
        }

        /* Card Bianca Principale */
        .ds-card {
            background: #FFFFFF;
            border: 1px solid #E0E0E0;
            border-radius: 16px;
            overflow: hidden; /* Per l'immagine */
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }

        /* Immagine */
        .ds-hero-img {
            width: 100%;
            height: 400px;
            object-fit: cover;
            display: block;
            background-color: #eee;
        }

        .ds-placeholder {
            width: 100%;
            height: 300px;
            background: linear-gradient(135deg, #f5f5f5, #e0e0e0);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #bdbdbd;
        }

        /* Contenuto interno con padding */
        .ds-content { padding: 30px; }

        /* Badges */
        .ds-badges { display: flex; gap: 8px; margin-bottom: 15px; }
        .ds-badge {
            padding: 4px 12px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
        }
        .ds-badge-green { background: #E8F5E9; color: #2E7D32; }
        .ds-badge-grey { background: #F5F5F5; color: #616161; border: 1px solid #EEEEEE; }

        /* Titoli */
        .ds-title {
            font-size: 2rem;
            margin: 0 0 20px 0;
            line-height: 1.2;
            color: #202124;
        }

        .ds-label {
            display: block;
            font-size: 0.95rem;
            font-weight: 700;
            color: #202124;
            margin-bottom: 8px;
        }

        .ds-text {
            font-size: 1rem;
            line-height: 1.6;
            color: #5F6368;
            margin-bottom: 30px;
        }

        /* Griglia Dettagli (Data/Luogo) */
        .ds-meta-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            border-top: 1px solid #EEEEEE;
            border-bottom: 1px solid #EEEEEE;
            padding: 20px 0;
            margin-bottom: 30px;
        }

        .ds-meta-item { display: flex; flex-direction: column; gap: 5px; }
        .ds-meta-label { font-size: 0.8rem; color: #9E9E9E; }
        .ds-meta-val {
            display: flex; align-items: center; gap: 8px;
            font-size: 1rem; font-weight: 600; color: #333;
        }
        .ds-meta-val .material-icons { color: #757575; font-size: 20px; }

        /* Box Consegna */
        .ds-delivery {
            border-radius: 12px;
            padding: 20px;
            display: flex;
            gap: 15px;
            align-items: flex-start;
        }

        /* Tema Viola (DropPoint) */
        .theme-purple { background: #F3E5F5; border: 1px solid #E1BEE7; }
        .theme-purple .icon-box { background: #E1BEE7; color: #7B1FA2; }
        .theme-purple h4 { color: #4A148C; }
        .theme-purple p { color: #6A1B9A; }
        .theme-purple .sm-badge { border: 1px solid #E1BEE7; color: #7B1FA2; }

        /* Tema Verde (Diretta) */
        .theme-green { background: #E8F5E9; border: 1px solid #C8E6C9; }
        .theme-green .icon-box { background: #C8E6C9; color: #1B5E20; }
        .theme-green h4 { color: #1B5E20; }
        .theme-green p { color: #2E7D32; }
        .theme-green .sm-badge { border: 1px solid #C8E6C9; color: #2E7D32; }

        .icon-box {
            width: 40px; height: 40px; border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            flex-shrink: 0;
        }
        .ds-delivery h4 { margin: 0 0 5px 0; font-size: 1rem; }
        .ds-delivery p { margin: 0; font-size: 0.9rem; line-height: 1.4; }

        .sm-badge {
            display: inline-block; background: #fff; padding: 2px 8px;
            border-radius: 4px; font-size: 0.7rem; font-weight: 700;
            text-transform: uppercase; margin-bottom: 8px;
        }

        /* --- SIDEBAR --- */
        .ds-sidebar-card {
            background: #FFFFFF;
            border: 1px solid #E0E0E0;
            border-radius: 16px;
            padding: 24px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }

        .ds-side-title { font-size: 1rem; font-weight: 700; margin-bottom: 15px; color: #202124; }

        /* Bottoni */
        .ds-btn {
            display: block; width: 100%; text-align: center;
            padding: 12px; border-radius: 8px; font-weight: 600;
            cursor: pointer; border: none; font-size: 0.95rem;
            transition: 0.2s; text-decoration: none;
        }

        .ds-btn-orange { background: #E65100; color: white; }
        .ds-btn-orange:hover { background: #EF6C00; }

        .ds-btn-danger { background: white; border: 1px solid #D32F2F; color: #D32F2F; }
        .ds-btn-danger:hover { background: #FFEBEE; }

        /* Input reclamo */
        .ds-input {
            width: 100%; padding: 10px; border: 1px solid #ccc;
            border-radius: 6px; margin-bottom: 10px;
            font-size: 0.95rem; font-family: inherit;
        }

        /* Messaggio non trovato */
        .ds-not-found { text-align: center; padding: 60px 20px; }
        .ds-not-found h2 { margin-bottom: 10px; font-size: 2rem; color: #333; }

        /* Mobile */
        @media (max-width: 900px) {
            .ds-grid { grid-template-columns: 1fr; }
            .ds-hero-img { height: 250px; }
        }
    </style>
</head>
<body>

<jsp:include page="navbar.jsp" />

<div class="ds-wrapper">

    <a href="${pageContext.request.contextPath}/index" class="ds-back-link">
        <span class="material-icons" style="font-size:18px;">arrow_back</span>
        Torna alla Home
    </a>

    <c:if test="${segnalazione == null}">
        <div class="ds-card ds-not-found">
            <h2>Segnalazione non trovata</h2>
            <p style="color:#666;">La pagina che cerchi non esiste o è stata rimossa.</p>
            <a href="${pageContext.request.contextPath}/index" class="ds-btn ds-btn-orange" style="display:inline-block; width:auto; margin-top:20px; padding: 10px 30px;">
                Vai alla Home
            </a>
        </div>
    </c:if>

    <c:if test="${segnalazione != null}">
        <div class="ds-grid">

            <div class="ds-card">

                <c:choose>
                    <c:when test="${not empty segnalazione.immagine and segnalazione.immagine != 'default.png'}">
                        <img src="${pageContext.request.contextPath}/${segnalazione.immagine}" class="ds-hero-img">
                    </c:when>
                    <c:otherwise>
                        <div class="ds-placeholder">
                            <span class="material-icons" style="font-size: 64px;">inventory_2</span>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="ds-content">

                    <div class="ds-badges">
                        <span class="ds-badge ds-badge-green">${segnalazione.stato}</span>
                        <span class="ds-badge ds-badge-grey">${segnalazione.tipoSegnalazione}</span>
                        <span class="ds-badge ds-badge-grey" style="text-transform: capitalize;">
                            <c:choose>
                                <c:when test="${segnalazione.tipoSegnalazione == 'OGGETTO'}">${segnalazione.categoria}</c:when>
                                <c:otherwise>Animale: ${segnalazione.specie}</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <h1 class="ds-title">${segnalazione.titolo}</h1>

                    <span class="ds-label">Descrizione</span>
                    <p class="ds-text">${segnalazione.descrizione}</p>

                    <div class="ds-meta-grid">
                        <div class="ds-meta-item">
                            <span class="ds-meta-label">Data ritrovamento</span>
                            <div class="ds-meta-val">
                                <span class="material-icons">calendar_today</span>
                                <fmt:formatDate value="${segnalazione.dataRitrovamento}" pattern="dd MMMM yyyy"/>
                            </div>
                        </div>
                        <div class="ds-meta-item">
                            <span class="ds-meta-label">Luogo</span>
                            <div class="ds-meta-val">
                                <span class="material-icons">location_on</span>
                                <div>
                                        ${segnalazione.luogoRitrovamento}<br>
                                    <span style="font-weight:400; font-size:0.85rem; color:#757575;">${segnalazione.citta} (${segnalazione.provincia})</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <% if (isDropPoint) { %>
                    <div class="ds-delivery theme-purple">
                        <div class="icon-box"><span class="material-icons">store</span></div>
                        <div>
                            <span class="sm-badge">Tramite Drop-Point</span>
                            <h4>Modalità di Consegna: Drop-Point</h4>
                            <p>L'oggetto sarà depositato presso un punto autorizzato. Il proprietario potrà ritirarlo presentando il codice di consegna.</p>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="ds-delivery theme-green">
                        <div class="icon-box"><span class="material-icons">handshake</span></div>
                        <div>
                            <span class="sm-badge">Consegna Diretta</span>
                            <h4>Modalità di Consegna: A mano</h4>
                            <p>La restituzione avverrà tramite incontro diretto tra Finder e Proprietario. Accordatevi in un luogo sicuro.</p>
                        </div>
                    </div>
                    <% } %>

                </div>
            </div>

            <aside>

                <c:if test="<%= !isOwner %>">
                    <div class="ds-sidebar-card">
                        <h4 class="ds-side-title">È tuo questo oggetto?</h4>
                        <p class="ds-text" style="font-size:0.9rem; margin-bottom:15px;">Invia un reclamo per dimostrare di essere il proprietario.</p>

                        <c:if test="<%= isLogged %>">
                            <button onclick="document.getElementById('claimForm').style.display='block'; this.style.display='none'" class="ds-btn ds-btn-orange">
                                Invia Reclamo
                            </button>

                            <form id="claimForm" action="gestione-reclamo" method="post" style="display:none; margin-top:15px;">
                                <input type="hidden" name="action" value="invia">
                                <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">

                                <label class="ds-label" style="font-size:0.85rem;">1. ${segnalazione.domandaVerifica1}</label>
                                <input type="text" name="risposta1" required class="ds-input">

                                <label class="ds-label" style="font-size:0.85rem;">2. ${segnalazione.domandaVerifica2}</label>
                                <input type="text" name="risposta2" required class="ds-input">

                                <button class="ds-btn ds-btn-orange">Conferma Invio</button>
                            </form>
                        </c:if>

                        <c:if test="<%= !isLogged %>">
                            <a href="login" class="ds-btn ds-btn-orange">Accedi per Reclamare</a>
                        </c:if>
                    </div>
                </c:if>

                <c:if test="<%= isOwner %>">
                    <div class="ds-sidebar-card">
                        <h4 class="ds-side-title" style="color:#E65100;">Gestione Proprietario</h4>

                        <form action="dettaglio-segnalazione" method="post" onsubmit="return confirm('Eliminare definitivamente?');" style="margin-bottom:20px;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                            <button class="ds-btn ds-btn-danger">
                                <span class="material-icons" style="vertical-align:middle; font-size:18px;">delete</span> Elimina
                            </button>
                        </form>

                        <hr style="border:0; border-top:1px solid #eee; margin:15px 0;">

                        <h5 style="margin-bottom:10px; font-size:0.9rem; color:#5f6368;">Reclami Ricevuti</h5>
                        <c:if test="${empty reclamiRicevuti}">
                            <p style="font-size:0.85rem; color:#999; font-style:italic;">Nessun reclamo ricevuto.</p>
                        </c:if>

                        <c:forEach var="r" items="${reclamiRicevuti}">
                            <div style="background:#FAFAFA; border:1px solid #EEE; padding:10px; border-radius:8px; margin-bottom:10px;">
                                <strong>Utente #${r.idUtenteRichiedente}</strong>
                                <div style="font-size:0.85rem; color:#555; margin:5px 0;">
                                    R1: ${r.rispostaVerifica1}<br>
                                    R2: ${r.rispostaVerifica2}
                                </div>

                                <c:if test="${r.stato == 'IN_ATTESA'}">
                                    <form action="gestione-reclamo" method="post">
                                        <input type="hidden" name="action" value="accetta">
                                        <input type="hidden" name="idReclamo" value="${r.id}">
                                        <input type="hidden" name="idSegnalazione" value="${segnalazione.id}">
                                        <button style="width:100%; background:#2E7D32; color:white; border:none; padding:6px; border-radius:4px; cursor:pointer;">Accetta</button>
                                    </form>
                                </c:if>
                                <c:if test="${r.stato == 'ACCETTATO'}">
                                    <div style="margin-top:5px; color:#2E7D32; font-weight:bold; font-size:0.8rem;">
                                        <span class="material-icons" style="font-size:14px; vertical-align:middle;">check_circle</span> Accettato<br>
                                        Codice: ${r.codiceConsegna}
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>

            </aside>
        </div>
    </c:if>

</div>

</body>
</html>