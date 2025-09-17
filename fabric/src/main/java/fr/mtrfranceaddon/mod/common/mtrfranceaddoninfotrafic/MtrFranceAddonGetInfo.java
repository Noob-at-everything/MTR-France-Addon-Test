package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.core.data.SimplifiedRoute;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MtrFranceAddonGetInfo {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Long, CustomRouteInfo> customRoutes = new HashMap<>();
    private static File jsonFile;

    // Initialisation avec le dossier du monde
    public static void init(File worldDir) {
        File folder = new File(worldDir, "mtrfranceaddon");
        if (!folder.exists()) folder.mkdirs();
        jsonFile = new File(folder, "routes.json");

        if (jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type type = new TypeToken<Map<Long, CustomRouteInfo>>(){}.getType();
                Map<Long, CustomRouteInfo> loaded = gson.fromJson(reader, type);
                if (loaded != null) customRoutes.putAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Récupérer toutes les routes avec infos personnalisées
    public static Map<Long, CustomRouteInfo> getAllCustomRoutes() {
        MinecraftClientData.getInstance().simplifiedRoutes.forEach(route -> {
            customRoutes.putIfAbsent(route.getId(),
                    new CustomRouteInfo(route.getId(), route.getName(), route.getColor()));
        });
        return customRoutes;
    }

    // Ajouter/modifier une route personnalisée
    public static void updateCustomRoute(long id, CustomRouteInfo info) {
        customRoutes.put(id, info);
        save();
    }

    // Sauvegarder sur disque
    private static void save() {
        if (jsonFile == null) return;
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(customRoutes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
