package fr.mtrfranceaddon.fabric;

import fr.mtrfranceaddon.mod.common.Init;
import net.fabricmc.api.ClientModInitializer;
import fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic.MtrFranceAddonGetInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MTRFranceAddonFabricClient implements ClientModInitializer {

    // Déclaration du champ AU NIVEAU DE LA CLASSE (hors méthode)
    private MtrFranceAddonGetInfo info;

    @Override
    public void onInitializeClient() {
        Init.initClient();

        info = new MtrFranceAddonGetInfo();

        // Tick handler : s'exécute chaque tick (20 fois par seconde)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && info.canUpdate()) {
                info.logAllRoutes();
            }
        });
    }
}


