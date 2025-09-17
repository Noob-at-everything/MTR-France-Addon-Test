package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

import org.mtr.core.data.Route;
import org.mtr.core.generated.data.SimplifiedRouteSchema;

public class CustomRouteSchema extends SimplifiedRouteSchema {

    public CustomRouteSchema(long id, String name, long color, Route.CircularState circularState) {
        super(id, name, color, circularState);
    }

    public int getRouteId() {
        return Math.toIntExact(this.id); // accès possible car protected
    }

    public String getRouteName() {
        return this.name;
    }

    public int getRouteColor() {
        return Math.toIntExact(this.color);
    }

    public String getCircularState() {
        return this.circularState.toString();
    }
}
