package fr.mtrfranceaddon.fabric;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.CustomRouteInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MTRFranceAddonServer {
    // Ajoute cette classe interne dans MTRFranceAddonServer, à côté de AlertHandler
    private static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                Map<Long, CustomRouteInfo> routes = MtrFranceAddonGetInfo.getAllCustomRoutes();
                String response = gson.toJson(routes);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } else if ("POST".equalsIgnoreCase(method)) {
                // Pour mise à jour depuis le HTML
                InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024];
                int read;
                while ((read = is.read(tmp)) != -1) buffer.write(tmp, 0, read);

                String json = new String(buffer.toByteArray(), "UTF-8");
                CustomRouteInfo info = gson.fromJson(json, CustomRouteInfo.class);

                MtrFranceAddonGetInfo.updateCustomRoute(info.getId(), info);

                String response = "{\"status\":\"ok\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }


    private HttpServer server;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // stockage mémoire des alertes
    private static final Map<Long, Alert> alerts = new ConcurrentHashMap<>();
    private static long alertIdCounter = 1;
    private static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Charger le HTML depuis les resources
            InputStream htmlStream = getClass().getResourceAsStream("/mtr-france-addon.html");
            if (htmlStream == null) {}


            if (htmlStream == null) {
                String notFound = "Fichier HTML introuvable";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            // Lire le fichier manuellement (compatible Java 8)
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int read;
            while ((read = htmlStream.read(tmp)) != -1) {
                buffer.write(tmp, 0, read);
            }
            byte[] htmlBytes = buffer.toByteArray();

            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlBytes);
            }
        }
    }

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8889), 0);

            // JSON endpoints
            server.createContext("/mtr-france-addon", new RouteHandler());
            server.createContext("/alerts", new AlertHandler());
            server.createContext("/mtr-france-addon.html", new HtmlHandler()); // juste le fichier HTML


            // Page HTML
            server.createContext("/", new HtmlHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("Le serveur fonctionne ✔ sur http://localhost:8889/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // --- Handler HTML unique ---
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // On force vers ton fichier HTML principal
            if (path.equals("/") || path.equals("/index.html") || path.equals("/mtr-france-addon.html")) {
                try (InputStream htmlStream = getClass().getResourceAsStream("/mtr-france-addon.html")) {
                    if (htmlStream == null) {
                        String msg = "404 Not Found: /mtr-france-addon.html";
                        exchange.sendResponseHeaders(404, msg.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(msg.getBytes());
                        }
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = htmlStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    byte[] response = baos.toByteArray();

                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                }
            } else {
                String msg = "404 Not Found: " + path;
                exchange.sendResponseHeaders(404, msg.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(msg.getBytes());
                }
            }
        }
    }


    // --------- HANDLER ALERTES -------------
    private static class AlertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                String response = gson.toJson(alerts);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } else if ("POST".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024];
                int read;
                while ((read = is.read(tmp)) != -1) {
                    buffer.write(tmp, 0, read);
                }
                String json = new String(buffer.toByteArray(), "UTF-8");

                Alert alert = gson.fromJson(json, Alert.class);

                if (alert.id == 0) {
                    alert.id = alertIdCounter++;
                }
                alerts.put(alert.id, alert);

                String response = "{\"status\":\"ok\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // --------- CLASSE ALERT -------------
    private static class Alert {
        long id;
        String message;
        String severity; // "info", "warning", "critical", "stop"

        public Alert() {}
        public Alert(long id, String message, String severity) {
            this.id = id;
            this.message = message;
            this.severity = severity;
        }
    }
}
