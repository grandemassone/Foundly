<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.bean.Utente" %>
<%@ page import="model.bean.DropPoint" %>

<%
    Utente utente = (Utente) session.getAttribute("utente");

    List<DropPoint> dropPoints = (List<DropPoint>) request.getAttribute("dropPoints");
    int countDropPoints = (dropPoints != null) ? dropPoints.size() : 0;
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Drop-Point - Foundly</title>

    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <!-- Leaflet -->
    <link rel="stylesheet"
          href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin=""/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/drop_point.css">
</head>
<body class="page-enter">

<jsp:include page="/WEB-INF/jsp/navbar.jsp" />

<!-- CONTENUTO -->
<main class="dp-main">

    <!-- Header pagina -->
    <section class="dp-header-card">
        <div class="dp-header-top">
            <div>
                <h1 class="dp-title">Drop-Point</h1>
                <p class="dp-subtitle">
                    Trova i punti di consegna autorizzati nella tua città
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/registrazione-droppoint"
               class="dp-register-btn">
                <span class="material-icons">add</span>
                Registra la Tua Attività
            </a>
        </div>

        <!-- barra ricerca -->
        <div class="dp-search-wrapper">
            <span class="material-icons dp-search-icon">search</span>
            <input type="text"
                   class="dp-search-input"
                   placeholder="Cerca per nome, città o indirizzo...">
        </div>
    </section>

    <!-- Card info + toggle -->
    <section class="dp-info-card">
        <div class="dp-info-left">
            <div class="dp-info-icon">
                <span class="material-icons">storefront</span>
            </div>
            <div class="dp-info-text">
                <span class="dp-info-label">Drop-Point Disponibili</span>
                <span class="dp-info-value"><%= countDropPoints %></span>
            </div>
        </div>

        <div class="dp-view-toggle">
            <button class="dp-toggle-btn dp-toggle-btn-active" id="btnViewMap" type="button">
                <span class="material-icons">map</span>
                <span>Mappa</span>
            </button>
            <button class="dp-toggle-btn" id="btnViewList" type="button">
                <span class="material-icons">list</span>
                <span>Elenco</span>
            </button>
        </div>
    </section>

    <!-- Contenitore mappa / lista -->
    <section class="dp-map-card">
        <div id="dpMapContainer" class="dp-map-container"></div>

        <div id="dpListContainer" class="dp-list-container">
            <%
                if (dropPoints == null || dropPoints.isEmpty()) {
            %>
            <p class="dp-empty">Nessun Drop-Point disponibile.</p>
            <%
            } else {
                for (DropPoint dp : dropPoints) {

                    boolean hasLogo = (dp.getImmagine() != null &&
                            dp.getImmagine().length > 0);
            %>
            <article class="dp-list-item">
                <!-- COLONNA IMMAGINE A SINISTRA -->
                <div class="dp-list-thumb">
                    <% if (hasLogo) { %>
                    <img
                            src="<%= request.getContextPath() %>/drop-point-avatar?dpId=<%= dp.getId() %>"
                            alt="Logo <%= dp.getNomeAttivita() %>"
                            class="dp-list-thumb-img">
                    <% } else { %>
                    <div class="dp-list-thumb-placeholder">
                        <span class="material-icons">storefront</span>
                    </div>
                    <% } %>
                </div>

                <!-- COLONNA TESTO A DESTRA -->
                <div class="dp-list-content">
                    <h3 class="dp-list-title"><%= dp.getNomeAttivita() %></h3>
                    <p class="dp-list-address">
                        <%= dp.getIndirizzo() %>, <%= dp.getCitta() %> (<%= dp.getProvincia() %>)
                    </p>
                    <p class="dp-list-phone">
                        <span class="material-icons">phone</span>
                        <span><%= dp.getTelefono() != null ? dp.getTelefono() : "-" %></span>
                    </p>
                    <p class="dp-list-hours">
                        <span class="material-icons">schedule</span>
                        <span><%= dp.getOrariApertura() != null ? dp.getOrariApertura() : "Orari non disponibili" %></span>
                    </p>
                </div>
            </article>
            <%
                    }
                }
            %>
        </div>
    </section>

</main>

<!-- Leaflet + JS -->
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""></script>

<script>
    // ====== DATI DAL SERVER ======
    const dropPointsData = [
        <% if (dropPoints != null) {
               for (int i = 0; i < dropPoints.size(); i++) {
                   DropPoint dp = dropPoints.get(i);
                   Double lat = dp.getLatitudine();
                   Double lng = dp.getLongitudine();
        %>
        {
            id: <%= dp.getId() %>,
            name: "<%= dp.getNomeAttivita().replace("\"", "\\\"") %>",
            address: "<%= dp.getIndirizzo().replace("\"", "\\\"") %>",
            city: "<%= dp.getCitta().replace("\"", "\\\"") %>",
            lat: <%= lat != null ? lat.toString() : "null" %>,
            lng: <%= lng != null ? lng.toString() : "null" %>
        }<%= (i < dropPoints.size() - 1) ? "," : "" %>
        <%     }
           } %>
    ];

    // ====== MAPPA LEAFLET ======
    const map = L.map('dpMapContainer');
    const defaultCenter = [41.9028, 12.4964]; // Roma
    map.setView(defaultCenter, 6); // fallback iniziale

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    const markersLayer = L.layerGroup().addTo(map);

    function renderMarkers(filterText) {
        const q = (filterText || "").toLowerCase();
        markersLayer.clearLayers();

        const bounds = L.latLngBounds([]);
        let added = 0;

        dropPointsData.forEach(dp => {
            if (dp.lat == null || dp.lng == null) return;

            const haystack = (dp.name + " " + dp.address + " " + dp.city).toLowerCase();
            if (!q || haystack.includes(q)) {
                const marker = L.marker([dp.lat, dp.lng])
                    .addTo(markersLayer)
                    .bindPopup(
                        '<strong>' + dp.name + '</strong><br>' +
                        dp.address + '<br>' +
                        dp.city
                    );

                bounds.extend(marker.getLatLng());
                added++;
            }
        });

        if (added > 0) {
            map.fitBounds(bounds, { padding: [30, 30] });
        } else {
            map.setView(defaultCenter, 6);
        }
    }

    // ====== TOGGLE MAPPA / ELENCO ======
    const btnViewMap = document.getElementById('btnViewMap');
    const btnViewList = document.getElementById('btnViewList');
    const mapContainer = document.getElementById('dpMapContainer');
    const listContainer = document.getElementById('dpListContainer');

    function setView(view) {
        if (view === 'map') {
            mapContainer.style.display = 'block';
            listContainer.style.display = 'none';
            btnViewMap.classList.add('dp-toggle-btn-active');
            btnViewList.classList.remove('dp-toggle-btn-active');
            map.invalidateSize();
        } else {
            mapContainer.style.display = 'none';
            listContainer.style.display = 'block';
            btnViewMap.classList.remove('dp-toggle-btn-active');
            btnViewList.classList.add('dp-toggle-btn-active');
        }
    }

    btnViewMap.addEventListener('click', () => setView('map'));
    btnViewList.addEventListener('click', () => setView('list'));

    // vista iniziale
    setView('map');
    renderMarkers("");

    // ====== RICERCA (mappa + elenco) ======
    const searchInput = document.querySelector(".dp-search-input");

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const q = this.value.trim().toLowerCase();

            // filtra elenco
            document.querySelectorAll(".dp-list-item").forEach(item => {
                const text = item.innerText.toLowerCase();
                item.style.display = (!q || text.includes(q)) ? "block" : "none";
            });

            // filtra marker + fit bounds
            renderMarkers(q);
        });
    }
</script>

</body>
</html>
