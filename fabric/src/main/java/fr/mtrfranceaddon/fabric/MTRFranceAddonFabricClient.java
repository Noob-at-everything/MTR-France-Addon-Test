package fr.mtrfranceaddon.fabric;

import fr.mtrfranceaddon.mod.common.Init;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.io.File;

public class MTRFranceAddonFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Init.initClient();

        MinecraftClient mc = MinecraftClient.getInstance();
        File worldDir = null;

        // 1) Essaye serveur intégré (solo)
        try {
            if (mc.getServer() != null) {
                worldDir = mc.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT).toFile();
            }
        } catch (Throwable ignored) {}

        // 2) fallback : dossier "saves" dans le runDirectory (toujours accessible)
        if (worldDir == null) {
            File run = mc.runDirectory;
            File saves = new File(run, "saves");
            worldDir = saves.exists() ? saves : run;
        }

        System.out.println("[MTR France Addon] using worldDir: " + worldDir.getAbsolutePath());
        MtrFranceAddonGetInfo.init(worldDir);
        ServerSaveResolver.register();
        // Démarrer serveur HTTP (statique)
        MTRFranceAddonServer.startServer();

        // Tick handler léger (ne change rien, mais peut servir pour chargements ultérieurs)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientWorld world = client.world;
            if (world != null) {
                // on peut appeler getAllCustomRoutes périodiquement pour détecter nouvelles routes
                MtrFranceAddonGetInfo.getAllCustomRoutes();
            }
        });
    }
}
