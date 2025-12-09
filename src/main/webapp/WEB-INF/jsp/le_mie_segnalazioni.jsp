<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="navbar.jsp" />
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Le mie segnalazioni</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
</head>
<body>

<div class="main-container" style="padding: 40px; max-width:1000px; margin:0 auto;">
    <h1>Le mie segnalazioni</h1>

    <div class="cards-grid">
        <c:forEach var="s" items="${mieSegnalazioni}">
            <div class="card" style="position:relative;">
                <div class="card-badges">
                    <span class="badge badge-active">${s.stato}</span>
                </div>

                <h3>${s.titolo}</h3>
                <p style="font-size:0.9rem; color:#666;">${s.citta} - ${s.dataRitrovamento}</p>

                <div style="margin-top:15px; display:flex; gap:10px;">
                    <a href="dettaglio-segnalazione?id=${s.id}" class="btn-primary" style="text-align:center; text-decoration:none;">Gestisci</a>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty mieSegnalazioni}">
            <p>Non hai ancora creato nessuna segnalazione.</p>
        </c:if>
    </div>
</div>
</body>
</html>