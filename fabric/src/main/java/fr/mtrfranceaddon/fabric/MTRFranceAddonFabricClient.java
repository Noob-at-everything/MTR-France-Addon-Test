package fr.mtrfranceaddon.fabric;

import fr.mtrfranceaddon.mod.common.Init;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.CustomRouteInfo;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.util.Map;

public class MTRFranceAddonFabricClient implements ClientModInitializer {


    private long lastUpdate = 0;
    private MtrFranceAddonGetInfo info;    @Override
    public void onInitializeClient() {
        Init.initClient();
        info = new MtrFranceAddonGetInfo();
        // Initialisation du dossier et JSON
        ClientWorld initialWorld = net.minecraft.client.MinecraftClient.getInstance().world;
        if (initialWorld != null) {
            File worldDir = initialWorld.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT).toFile();
            MtrFranceAddonGetInfo.init(worldDir);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            File worldDir = client.getServer().getSavePath(WorldSavePath.ROOT).toFile();
            MtrFranceAddonGetInfo.init(worldDir); // ← crée le dossier + JSON si absent
        }

        // Lancer le serveur local pour l’UI
        new Thread(() -> {
            MTRFranceAddonServer server = new MTRFranceAddonServer();
            server.startServer();
        }).start();


        // Tick client pour mise à jour toutes les 10 secondes
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            ClientWorld world = c.world; // déclaration unique

            if (world != null) { // utilise la variable existante
                // ton code ici
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
