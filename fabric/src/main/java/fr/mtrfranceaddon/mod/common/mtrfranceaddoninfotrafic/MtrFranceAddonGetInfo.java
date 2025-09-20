package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.core.data.SimplifiedRoute;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MtrFranceAddonGetInfo {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Long, CustomRouteInfo> customRoutes = new ConcurrentHashMap<>();
    private static File jsonFile = null;

    /**
     * Initialise le dossier et le fichier JSON. Crée un fichier initial si absent.
     */
    public static void init(File worldDir) {
        try {
            System.out.println("[MTR France Addon] init worldDir = " + worldDir.getAbsolutePath());
            File folder = new File(worldDir, "mtrfranceaddon");
            if (!folder.exists()) {
                boolean ok = folder.mkdirs();
                System.out.println("[MTR France Addon] mkdirs folder: " + ok + " -> " + folder.getAbsolutePath());
            }
            jsonFile = new File(folder, "routes.json");
            System.out.println("[MTR France Addon] jsonFile path = " + jsonFile.getAbsolutePath());

            if (!jsonFile.exists()) {
                // créer un JSON initial avec une route test pour vérifier le fonctionnement
                CustomRouteInfo sample = new CustomRouteInfo(1L, "Ligne test", 0x00FF00);
                customRoutes.clear();
                customRoutes.put(sample.id, sample);
                save(); // crée le fichier
                System.out.println("[MTR France Addon] Created initial JSON with sample route");
            } else {
                // charger si déjà présent
                try (FileReader reader = new FileReader(jsonFile)) {
                    Type type = new TypeToken<Map<Long, CustomRouteInfo>>() {}.getType();
                    Map<Long, CustomRouteInfo> loaded = gson.fromJson(reader, type);
                    if (loaded != null) {
                        customRoutes.clear();
                        customRoutes.putAll(loaded);
                    }
                    System.out.println("[MTR France Addon] loaded routes count = " + customRoutes.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne la map des routes. Tente de remplir à partir de MinecraftClientData si disponible.
     */
    public static Map<Long, CustomRouteInfo> getAllCustomRoutes() {
        try {
            Iterable<SimplifiedRoute> routes = MinecraftClientData.getInstance().simplifiedRoutes;
            if (routes != null) {
                int added = 0;
                for (SimplifiedRoute route : routes) {
                    long id = route.getId();
                    if (!customRoutes.containsKey(id)) {
                        customRoutes.put(id, new CustomRouteInfo(id, route.getName(), route.getColor()));
                        added++;
                    }
                }
                if (added > 0) {
                    System.out.println("[MTR France Addon] Added " + added + " routes from MinecraftClientData");
                    save();
                }
            } else {
                System.out.println("[MTR France Addon] MinecraftClientData.simplifiedRoutes is null");
            }
        } catch (Throwable t) {
            // ne laisse pas planter : on loggue seulement
            System.out.println("[MTR France Addon] Exception while reading simplifiedRoutes: " + t);
        }
        return customRoutes;
    }

    public static void updateCustomRoute(long id, CustomRouteInfo info) {
        if (info == null) return;
        customRoutes.put(id, info);
        save();
    }

    private static void save() {
        if (jsonFile == null) {
            System.out.println("[MTR France Addon] save() called but jsonFile is null!");
            return;
        }
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(customRoutes, writer);
            System.out.println("[MTR France Addon] Routes saved to " + jsonFile.getAbsolutePath() + " (count=" + customRoutes.size() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // accès direct si besoin
    public static Map<Long, CustomRouteInfo> getRoutesMap() {
        return customRoutes;
    }
}
