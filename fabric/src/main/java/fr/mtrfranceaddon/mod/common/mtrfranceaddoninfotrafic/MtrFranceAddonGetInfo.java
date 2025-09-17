package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.mtr.core.data.SimplifiedRoute;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MtrFranceAddonGetInfo {

    private long lastUpdate = 0; // Timer pour mise à jour toutes les 10 sec
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Vérifie si 10 secondes se sont écoulées
    public boolean canUpdate() {
        return System.currentTimeMillis() - lastUpdate >= 10000;
    }

    // Enregistre toutes les routes dans un fichier JSON
    public void logAllRoutes() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        // Déterminer le nom du dossier du monde
        String worldFolderName;
        if (client.getServer() != null) { // Solo
            worldFolderName = client.getServer().getSaveProperties().getLevelName();
        } else { // Multi
            worldFolderName = world.getRegistryKey().getValue().getPath();
        }

        // Créer le dossier mtrfranceaddon dans le dossier du monde
        File logDir = new File(client.runDirectory, "saves/" + worldFolderName + "/mtrfranceaddon");
        if (!logDir.exists()) logDir.mkdirs();

        File logFile = new File(logDir, "routes.json");

        ObjectAVLTreeSet<SimplifiedRoute> routesSet = MinecraftClientData.getInstance().simplifiedRoutes;

        try (FileWriter writer = new FileWriter(logFile)) {
            gson.toJson(routesSet, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastUpdate = System.currentTimeMillis();
    }
}

