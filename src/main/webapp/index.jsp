<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*, model.utente.DbUtil" %>
<!DOCTYPE html>
<html>
<head>
    <title>Foundly</title>
</head>
<body>
<h1>Utenti registrati</h1>
<hr>

<%
    try {
        List<String> nomi = DbUtil.getNomiCompleti();
%>
<ul>
    <% for (String n : nomi) { %>
    <li><%= n %></li>
    <% } %>
</ul>
<%
} catch (Exception e) {
%>
<p style="color:red">Errore: <%= e.getMessage() %></p>
<%
        e.printStackTrace();
    }
%>

</body>
</html>
