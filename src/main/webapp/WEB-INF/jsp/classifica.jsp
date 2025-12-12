<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.DropPoint" %>
<%@ page import="java.util.List" %>

<%
    DropPoint dpSession = (DropPoint) session.getAttribute("dropPoint");
    Utente utenteLoggato = (Utente) session.getAttribute("utente");

    if (dpSession != null) {
        response.sendRedirect(request.getContextPath() + "/area-drop-point");
        return;
    }
    if (utenteLoggato == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Utente> classifica = (List<Utente>) request.getAttribute("classifica");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Classifica - Foundly</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/classifica.css?v=3">
</head>
<body class="page-enter">

<jsp:include page="/WEB-INF/jsp/navbar.jsp" />

<main class="classifica-main">

    <header class="classifica-header">
        <h1>I Campioni di Foundly</h1>
        <p>Grazie a voi la community cresce e gli oggetti tornano a casa.</p>
    </header>

    <% if (classifica == null || classifica.isEmpty()) { %>
    <div class="empty-state">
        <span class="material-icons empty-icon">emoji_events</span>
        <h3>La classifica è ancora vuota!</h3>
        <p>Sii il primo a completare una restituzione.</p>
    </div>
    <% } else { %>

    <section class="podium-container">
        <%
            Utente first  = classifica.size() > 0 ? classifica.get(0) : null;
            Utente second = classifica.size() > 1 ? classifica.get(1) : null;
            Utente third  = classifica.size() > 2 ? classifica.get(2) : null;
        %>

        <% if (second != null) {
            String[] b2 = getBadgeInfo(second.getBadge());
        %>
        <div class="podium-step step-silver">
            <div class="medal-icon">🥈</div>
            <div class="podium-avatar">
                <% boolean secondHasAvatar = second.getImmagineProfilo() != null && second.getImmagineProfilo().length > 0; %>
                <% if (secondHasAvatar) { %>
                <img src="<%= request.getContextPath() %>/avatar?userId=<%= second.getId() %>" class="podium-img" alt="">
                <% } else { %>
                <div class="podium-placeholder"><%= second.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= second.getNome() %> <%= second.getCognome() %></span>
                <span class="badge-tag <%= b2[1] %>" style="margin-top:4px;"><%= b2[0] %></span>
                <span class="podium-points"><%= second.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">2</div>
        </div>
        <% } %>

        <% if (first != null) {
            String[] b1 = getBadgeInfo(first.getBadge());
        %>
        <div class="podium-step step-gold">
            <div class="crown-icon">👑</div>
            <div class="podium-avatar">
                <% boolean firstHasAvatar = first.getImmagineProfilo() != null && first.getImmagineProfilo().length > 0; %>
                <% if (firstHasAvatar) { %>
                <img src="<%= request.getContextPath() %>/avatar?userId=<%= first.getId() %>" class="podium-img" alt="">
                <% } else { %>
                <div class="podium-placeholder"><%= first.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= first.getNome() %> <%= first.getCognome() %></span>
                <span class="podium-badge">🏆 Campione</span>
                <span class="badge-tag <%= b1[1] %>" style="margin-top:4px;"><%= b1[0] %></span>
                <span class="podium-points"><%= first.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">1</div>
        </div>
        <% } %>

        <% if (third != null) {
            String[] b3 = getBadgeInfo(third.getBadge());
        %>
        <div class="podium-step step-bronze">
            <div class="medal-icon">🥉</div>
            <div class="podium-avatar">
                <% boolean thirdHasAvatar = third.getImmagineProfilo() != null && third.getImmagineProfilo().length > 0; %>
                <% if (thirdHasAvatar) { %>
                <img src="<%= request.getContextPath() %>/avatar?userId=<%= third.getId() %>" class="podium-img" alt="">
                <% } else { %>
                <div class="podium-placeholder"><%= third.getNome().charAt(0) %></div>
                <% } %>
            </div>
            <div class="podium-info">
                <span class="podium-name"><%= third.getNome() %> <%= third.getCognome() %></span>
                <span class="badge-tag <%= b3[1] %>" style="margin-top:4px;"><%= b3[0] %></span>
                <span class="podium-points"><%= third.getPunteggio() %> pt</span>
            </div>
            <div class="podium-base">3</div>
        </div>
        <% } %>
    </section>

    <% if (classifica.size() > 3) { %>
    <section class="ranking-list-section">
        <h3>Top Contributors</h3>
        <div class="table-card">
            <table class="ranking-table">
                <thead>
                <tr>
                    <th class="col-rank">#</th>
                    <th class="col-user">Utente</th>
                    <th class="col-badge">Livello</th>
                    <th class="col-points">Punti</th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (int i = 3; i < Math.min(classifica.size(), 15); i++) {
                        Utente u = classifica.get(i);
                        int rank = i + 1;

                        String[] bInfo = getBadgeInfo(u.getBadge());
                        String badgeName = bInfo[0];
                        String badgeClass = bInfo[1];

                        boolean isMe = utenteLoggato.getId() == u.getId();
                        boolean uHasAvatar = u.getImmagineProfilo() != null && u.getImmagineProfilo().length > 0;
                %>
                <tr class="<%= isMe ? "highlight-me" : "" %>">
                    <td class="col-rank"><span class="rank-circle"><%= rank %></span></td>
                    <td class="col-user">
                        <div class="user-row-info">
                            <% if (uHasAvatar) { %>
                            <img src="<%= request.getContextPath() %>/avatar?userId=<%= u.getId() %>" class="mini-avatar-img" alt="">
                            <% } else { %>
                            <div class="mini-avatar-placeholder"><%= u.getNome().charAt(0) %></div>
                            <% } %>

                            <div class="user-data">
                                <span class="u-name"><%= u.getNome() %> <%= u.getCognome() %></span>
                                <span class="u-username">@<%= u.getUsername() %></span>
                            </div>
                        </div>
                    </td>
                    <td class="col-badge">
                        <span class="badge-tag <%= badgeClass %>"><%= badgeName %></span>
                    </td>
                    <td class="col-points"><%= u.getPunteggio() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
    <% } %>

    <% } %>

</main>
</body>
</html>

<%!
    private String[] getBadgeInfo(String rawBadge) {
        String badgeName = "Novizio";
        String badgeClass = "bg-gray";

        if ("OCCHIO_DI_FALCO".equals(rawBadge)) {
            badgeName = "Occhio di Falco";
            badgeClass = "bg-bronze";
        } else if ("DETECTIVE".equals(rawBadge)) {
            badgeName = "Detective";
            badgeClass = "bg-silver";
        } else if ("SHERLOCK_HOLMES".equals(rawBadge)) {
            badgeName = "Sherlock";
            badgeClass = "bg-gold";
        }
        return new String[]{badgeName, badgeClass};
    }
%>
