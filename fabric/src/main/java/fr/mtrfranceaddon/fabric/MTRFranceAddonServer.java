package fr.mtrfranceaddon.fabric;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.CustomRouteInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class MTRFranceAddonServer {

    private HttpServer server;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8889), 0);
            server.createContext("/mtr-france-addon", new RouteHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Le serveur fonctionne ✔ sur http://localhost:8889/mtr-france-addon");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            InputStream htmlStream = null;

            if ("GET".equalsIgnoreCase(method)) {
                String path = exchange.getRequestURI().getPath();

                if (path.endsWith(".html")) {
                    htmlStream = getClass().getResourceAsStream("/mtr-france-addon.html");
                    if (htmlStream == null) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] tmp = new byte[1024];
                    int read;
                    while ((read = htmlStream.read(tmp)) != -1) {
                        buffer.write(tmp, 0, read);
                    }
                    byte[] htmlBytes = buffer.toByteArray();

                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, htmlBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(htmlBytes);
                    }
                } else if (path.endsWith("/mtr-france-addon")) {
                    // GET JSON pour les routes
                    Map<Long, CustomRouteInfo> routes = MtrFranceAddonGetInfo.getAllCustomRoutes();
                    String response = new GsonBuilder().setPrettyPrinting().create().toJson(routes);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else if ("POST".equalsIgnoreCase(method) && exchange.getRequestURI().getPath().endsWith("/mtr-france-addon")) {
                // POST JSON pour mettre à jour une route
                InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024];
                int read;
                while ((read = is.read(tmp)) != -1) {
                    buffer.write(tmp, 0, read);
                }
                String json = new String(buffer.toByteArray(), "UTF-8");
                CustomRouteInfo info = new GsonBuilder().create().fromJson(json, CustomRouteInfo.class);
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
                }




