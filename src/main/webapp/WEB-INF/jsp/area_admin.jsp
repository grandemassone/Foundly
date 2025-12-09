<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.DropPoint" %>
<%@ page import="model.bean.enums.Ruolo" %>

<%
  Utente utente = (Utente) session.getAttribute("utente");
  if (utente == null || utente.getRuolo() != Ruolo.ADMIN) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  List<DropPoint> pendenti = (List<DropPoint>) request.getAttribute("dropPointsPendenti");
  List<Utente> utenti = (List<Utente>) request.getAttribute("listaUtenti");

  String navAvatarPath = utente.getImmagineProfilo();
  boolean navHasAvatar = navAvatarPath != null && !navAvatarPath.trim().isEmpty();
%>

<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <title>Area Admin - Foundly</title>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/area_admin.css">
</head>
<body class="page-enter">

<nav class="navbar">
  <a href="${pageContext.request.contextPath}/index" class="brand">
    <div class="brand-icon">
      <img src="<%= request.getContextPath() %>/assets/images/logo.png" alt="logo_foundly">
    </div>
  </a>

  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/index" class="nav-item">
      <span class="material-icons">home</span> Home
    </a>
    <a href="${pageContext.request.contextPath}/admin" class="nav-item">
      <span class="material-icons">admin_panel_settings</span> Area Admin
    </a>
  </div>

  <div class="user-menu">
    <button type="button" class="user-avatar-btn">
      <% if (navHasAvatar) { %>
      <img src="<%= request.getContextPath() + "/" + navAvatarPath %>"
           alt=""
           class="user-avatar-img">
      <% } else { %>
      <div class="user-avatar-placeholder"></div>
      <% } %>
    </button>

    <div class="user-dropdown">
      <div class="user-dropdown-header">
        <div class="user-email"><%= utente.getEmail() %></div>
        <div class="user-points-row">
          <span class="points-label">punti</span>
          <span class="points-value"><%= utente.getPunteggio() %></span>
        </div>
      </div>

      <a href="${pageContext.request.contextPath}/profilo" class="user-dropdown-item">
        <span class="material-icons">person</span>
        <span>Profilo</span>
      </a>
      <a href="${pageContext.request.contextPath}/admin" class="user-dropdown-item">
        <span class="material-icons">admin_panel_settings</span>
        <span>Area Admin</span>
      </a>
      <a href="${pageContext.request.contextPath}/logout" class="user-dropdown-item user-dropdown-item-logout">
        <span class="material-icons">logout</span>
        <span>Logout</span>
      </a>
    </div>
  </div>
</nav>

<main class="admin-main">

  <!-- Gestione Drop-Point -->
  <section class="admin-section">
    <h1 class="admin-title">Gestione Drop-Point</h1>

    <div class="admin-card">
      <h2 class="admin-subtitle">Richieste in attesa</h2>

      <%
        if (pendenti == null || pendenti.isEmpty()) {
      %>
      <p class="admin-empty">Nessun Drop-Point in attesa di approvazione.</p>
      <%
      } else {
      %>
      <table class="admin-table">
        <thead>
        <tr>
          <th>Nome attività</th>
          <th>Indirizzo</th>
          <th>Città</th>
          <th>Provincia</th>
          <th>Telefono</th>
          <th>Azioni</th>
        </tr>
        </thead>
        <tbody>
        <%
          for (DropPoint dp : pendenti) {
        %>
        <tr>
          <td><%= dp.getNomeAttivita() %></td>
          <td><%= dp.getIndirizzo() %></td>
          <td><%= dp.getCitta() %></td>
          <td><%= dp.getProvincia() %></td>
          <td><%= dp.getTelefono() != null ? dp.getTelefono() : "-" %></td>
          <td class="admin-actions">
            <form method="post"
                  action="${pageContext.request.contextPath}/admin"
                  class="inline-form">
              <input type="hidden" name="action" value="approveDropPoint">
              <input type="hidden" name="dropPointId" value="<%= dp.getId() %>">
              <button type="submit" class="btn-admin-approve">Approva</button>
            </form>

            <form method="post"
                  action="${pageContext.request.contextPath}/admin"
                  class="inline-form">
              <input type="hidden" name="action" value="rejectDropPoint">
              <input type="hidden" name="dropPointId" value="<%= dp.getId() %>">
              <button type="submit" class="btn-admin-reject">Rifiuta</button>
            </form>
          </td>
        </tr>
        <%
          }
        %>
        </tbody>
      </table>
      <%
        }
      %>
    </div>
  </section>

  <!-- Gestione Utenti -->
  <section class="admin-section">
    <h1 class="admin-title">Gestione Utenti</h1>

    <div class="admin-card">
      <h2 class="admin-subtitle">Lista utenti</h2>

      <table class="admin-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Username</th>
          <th>Nome</th>
          <th>Cognome</th>
          <th>Email</th>
          <th>Ruolo</th>
          <th>Azioni</th>
        </tr>
        </thead>
        <tbody>
        <%
          if (utenti != null) {
            for (Utente u : utenti) {
        %>
        <tr>
          <td><%= u.getId() %></td>
          <td><%= u.getUsername() %></td>
          <td><%= u.getNome() %></td>
          <td><%= u.getCognome() %></td>
          <td><%= u.getEmail() %></td>
          <td><%= u.getRuolo() %></td>
          <td class="admin-actions">
            <%
              // Non permetto di cancellare se stesso e, volendo, altri ADMIN
              if (u.getId() != utente.getId() && u.getRuolo() == Ruolo.UTENTE_BASE) {
            %>
            <form method="post"
                  action="${pageContext.request.contextPath}/admin"
                  class="inline-form"
                  onsubmit="return confirm('Confermi la rimozione di questo utente?');">
              <input type="hidden" name="action" value="deleteUser">
              <input type="hidden" name="userId" value="<%= u.getId() %>">
              <button type="submit" class="btn-admin-delete">Elimina</button>
            </form>
            <%
            } else {
            %>
            <span class="admin-note">N/D</span>
            <%
              }
            %>
          </td>
        </tr>
        <%
            }
          }
        %>
        </tbody>
      </table>
    </div>
  </section>

</main>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    const userMenu = document.querySelector(".user-menu");
    if (userMenu) {
      const btn = userMenu.querySelector(".user-avatar-btn");
      btn.addEventListener("click", function (e) {
        e.stopPropagation();
        userMenu.classList.toggle("open");
      });
      document.addEventListener("click", function () {
        userMenu.classList.remove("open");
      });
    }
  });
</script>

</body>
</html>
