package fr.mtrfranceaddon.fabric;

import fr.mtrfranceaddon.mod.common.Init;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.CustomRouteInfo;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;

import java.io.File;
import java.util.Map;

public class MTRFranceAddonFabricClient implements ClientModInitializer {

    private long lastUpdate = 0;

    @Override
    public void onInitializeClient() {
        Init.initClient();

        // Initialisation du dossier et JSON
        ClientWorld world = net.minecraft.client.MinecraftClient.getInstance().world;
        if (world != null) {
            File worldDir = world.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT).toFile();
            MtrFranceAddonGetInfo.init(worldDir);
        }

        // Lancer le serveur local pour l’UI
        new Thread(() -> {
            MTRFranceAddonServer server = new MTRFranceAddonServer();
            server.startServer();
        }).start();


        // Tick client pour mise à jour toutes les 10 secondes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && canUpdate()) {
                updateRoutes();
            }
        });
    }

    private boolean canUpdate() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate >= 10_000) {
            lastUpdate = now;
            return true;
        }
        return false;
    }

    private void updateRoutes() {
        Map<Long, CustomRouteInfo> routes = MtrFranceAddonGetInfo.getAllCustomRoutes();
        // Ici tu peux notifier le serveur/UI pour rafraîchir le tableau si nécessaire
    }
}
