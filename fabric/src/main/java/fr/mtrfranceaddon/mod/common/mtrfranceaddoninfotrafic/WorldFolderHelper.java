package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;

import java.io.File;
public class WorldFolderHelper {   public static File getAddonFolder() {
    MinecraftClient client = MinecraftClient.getInstance();
    File baseFolder;

    // Solo
    if (client.isInSingleplayer() && client.getServer() instanceof IntegratedServer) {
        IntegratedServer server = (IntegratedServer) client.getServer();
        // On récupère le dossier "saves" puis le dossier du monde actif
        String worldName = server.getSaveProperties().getLevelName(); // fonctionne en 1.16.5
        baseFolder = new File(client.runDirectory, "saves/" + worldName);
    } else {
        // Multi : on stocke dans un dossier générique
        baseFolder = new File(client.runDirectory, "mtrfranceaddon");
    }

    File addonFolder = new File(baseFolder, "mtrfranceaddon");
    if (!addonFolder.exists()) {
        addonFolder.mkdirs();
    }

    return addonFolder;
}
}
