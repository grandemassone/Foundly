<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Foundly - Home</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
</head>
<body class="page-enter">

<jsp:include page="navbar.jsp" />

<header class="hero">
    <h1>Hai perso qualcosa?</h1>
    <p>
        Foundly ti aiuta a ritrovare oggetti e animali smarriti grazie alla nostra
        community. Segnala, cerca e restituisci!
    </p>
    <a href="crea-segnalazione" class="btn-cta-hero">
        <span class="material-icons">inventory_2</span>
        Crea Segnalazione
    </a>
</header>

<div class="search-wrapper">
    <div class="search-card">
        <%-- FORM DI RICERCA: Punta alla SearchServlet --%>
        <form action="${pageContext.request.contextPath}/search" method="GET">

            <div class="search-main-row">
                <div class="input-group">
                    <span class="material-icons search-icon">search</span>
                    <input type="text" name="q"
                           placeholder="Cerca per titolo..."
                           value="${not empty paramQ ? paramQ : ''}">
                </div>
                <button type="submit" class="btn-search-submit">
                    Cerca
                </button>
            </div>

            <div class="search-divider"></div>

            <div class="filters-row">
                <div class="filter-item">
                    <span class="material-icons">category</span>
                    <select name="tipo">
                        <option value="">Tutti i tipi</option>
                        <option value="oggetto" ${paramTipo == 'oggetto' ? 'selected' : ''}>Oggetto</option>
                        <option value="animale" ${paramTipo == 'animale' ? 'selected' : ''}>Animale</option>
                    </select>
                </div>

                <div class="filter-item">
                    <span class="material-icons">sell</span>
                    <select name="categoria">
                        <option value="">Tutte le categorie</option>
                        <option value="ELETTRONICA" ${paramCat == 'ELETTRONICA' ? 'selected' : ''}>Elettronica</option>
                        <option value="DOCUMENTI" ${paramCat == 'DOCUMENTI' ? 'selected' : ''}>Documenti</option>
                        <option value="ABBIGLIAMENTO" ${paramCat == 'ABBIGLIAMENTO' ? 'selected' : ''}>Abbigliamento</option>
                        <option value="GIOIELLI" ${paramCat == 'GIOIELLI' ? 'selected' : ''}>Gioielli</option>
                        <option value="CHIAVI" ${paramCat == 'CHIAVI' ? 'selected' : ''}>Chiavi</option>
                        <option value="PORTAFOGLI" ${paramCat == 'PORTAFOGLI' ? 'selected' : ''}>Portafogli</option>
                        <option value="BORSE" ${paramCat == 'BORSE' ? 'selected' : ''}>Borse</option>
                        <option value="ALTRO" ${paramCat == 'ALTRO' ? 'selected' : ''}>Altro</option>
                    </select>
                </div>
            </div>
        </form>
    </div>
</div>

<section class="recent-section">
    <div class="section-header">
        <h2>
            <c:choose>
                <c:when test="${not empty paramQ or not empty paramTipo or not empty paramCat}">
                    Risultati Ricerca
                </c:when>
                <c:otherwise>Segnalazioni Recenti</c:otherwise>
            </c:choose>
        </h2>
        <span class="result-count">${segnalazioni != null ? segnalazioni.size() : 0} risultati</span>
    </div>

    <div class="cards-grid">
        <c:if test="${empty segnalazioni}">
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px;">
                <span class="material-icons" style="font-size: 48px; color: #e0e0e0;">search_off</span>
                <p style="color: #666; font-style: italic; margin-top: 10px;">Nessuna segnalazione trovata con questi criteri.</p>
                <a href="${pageContext.request.contextPath}/index" style="color: var(--primary-orange); text-decoration: none; font-weight: 500;">Mostra tutto</a>
            </div>
        </c:if>

        <c:forEach var="s" items="${segnalazioni}">
            <a href="dettaglio-segnalazione?id=${s.id}" class="card-link">
                <article class="card">
                    <div class="card-badges">
                        <span class="badge ${s.stato == 'CHIUSA' ? 'badge-closed' : 'badge-active'}">
                                ${s.stato}
                        </span>
                        <span class="badge badge-type">${s.tipoSegnalazione}</span>
                    </div>

                    <div class="card-image-placeholder">
                        <c:choose>
                            <c:when test="${not empty s.immagine and s.immagine != 'default.png'}">
                                <img src="${pageContext.request.contextPath}/${s.immagine}" alt="${s.titolo}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 12px;">
                            </c:when>
                            <c:otherwise>
                                <span class="material-icons" style="font-size: 42px; color: #9E9E9E;">inventory_2</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="card-footer">
                        <div class="card-title">${s.titolo}</div>
                        <div class="card-info">
                                ${s.citta} •
                            <fmt:formatDate value="${s.dataRitrovamento}" pattern="dd MMM" />
                        </div>
                    </div>
                </article>
            </a>
        </c:forEach>
    </div>
</section>

</body>
</html>