package fr.mtrfranceaddon.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.CustomRouteInfo;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MTRFranceAddonServer {
    private static HttpServer server;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // stockage mémoire centralisé (id -> Alert)
    private static final Map<Long, Alert> alerts = new ConcurrentHashMap<>();
    private static long alertIdCounter = 1;
    private static File worldDir;

    public static synchronized void startServer() {
        if (server != null) return; // déjà démarré
        try {
            worldDir = new File(".minecraft/saves/MonMonde"); // TODO : pointer vers le vrai dossier monde
            loadAlerts();

            server = HttpServer.create(new InetSocketAddress(8889), 0);
            server.createContext("/alerts", new AlertHandler());
            server.createContext("/mtr-france-addon", new RouteHandler());
            server.createContext("/mtr-france-addon.html", new HtmlHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("[MTR France Addon] HTTP server started at http://localhost:8889/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------- HELPER ---------
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            return new String(baos.toByteArray(), "UTF-8");
        }
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --------- HANDLER ROUTES + ALERTES (JSON combiné) ---------
    private static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("routes", MtrFranceAddonGetInfo.getAllCustomRoutes());
                resp.put("alerts", alerts.values());
                sendJson(exchange, gson.toJson(resp));
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                try {
                    Map<String, Object> payload = gson.fromJson(body, Map.class);
                    if (payload == null) {
                        sendJson(exchange, "{\"status\":\"error\",\"reason\":\"empty payload\"}");
                        return;
                    }
                    String type = (payload.get("type") == null) ? "route" : payload.get("type").toString();

                    if ("route".equals(type)) {
                        long id = Long.parseLong(payload.get("id").toString());
                        String name = payload.getOrDefault("name", "").toString();
                        int color = (payload.get("color") != null) ? ((Double) payload.get("color")).intValue() : 0;
                        String description = payload.getOrDefault("description", "").toString();
                        int priority = (payload.get("priority") != null) ? ((Double) payload.get("priority")).intValue() : 0;

                        CustomRouteInfo info = new CustomRouteInfo(id, name, color);
                        info.setDescription(description);
                        info.setPriority(priority);
                        MtrFranceAddonGetInfo.updateCustomRoute(id, info);

                        sendJson(exchange, "{\"status\":\"ok\"}");
                        return;
                    } else if ("alert".equals(type)) {
                        long id = payload.containsKey("id")
                                ? Long.parseLong(payload.get("id").toString())
                                : alertIdCounter++;

                        String message = payload.getOrDefault("message", "").toString();
                        String severity = payload.getOrDefault("severity", "info").toString();
                        long routeId = payload.containsKey("routeId")
                                ? Long.parseLong(payload.get("routeId").toString())
                                : -1;

                        Alert a = new Alert(id, message, severity, routeId);
                        alerts.put(id, a);
                        saveAlerts();

                        sendJson(exchange, "{\"status\":\"ok\"}");
                        return;
                    }

                    sendJson(exchange, "{\"status\":\"error\",\"reason\":\"unknown type\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendJson(exchange, "{\"status\":\"error\",\"reason\":\"exception\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // --------- HANDLER ALERTES SEUL ---------
    private static class AlertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, gson.toJson(alerts.values()));
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // --------- HANDLER HTML ---------
    private static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<Long, CustomRouteInfo> routeMap = MtrFranceAddonGetInfo.getAllCustomRoutes();
            List<CustomRouteInfo> routes = new ArrayList<>(routeMap.values());


            byte[] out = html.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, out.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(out);
            }
        }
    }

    // --------- CLASSE ALERT ---------
    public static class Alert {
        long id;
        String message;
        String severity;
        long routeId;

        public Alert() {}
        public Alert(long id, String message, String severity, long routeId) {
            this.id = id;
            this.message = message;
            this.severity = severity;
            this.routeId = routeId;
        }
    }

    // --------- SAUVEGARDE ALERTES ---------
    private static void loadAlerts() {
        try {
            File dir = new File(worldDir, "mtrfranceaddon");
            File file = new File(dir, "alerts.json");

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Map<Long, Alert> loaded = gson.fromJson(reader, new TypeToken<Map<Long, Alert>>(){}.getType());
                    if (loaded != null) {
                        alerts.clear();
                        alerts.putAll(loaded);
                        alertIdCounter = alerts.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveAlerts() {
        try {
            File dir = new File(worldDir, "mtrfranceaddon");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "alerts.json");
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(alerts, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
