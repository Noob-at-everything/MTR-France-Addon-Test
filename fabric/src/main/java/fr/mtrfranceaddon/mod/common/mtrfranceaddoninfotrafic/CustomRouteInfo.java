package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

public class CustomRouteInfo {
        public long id;
        public String name;
        public int color;
        public String description;
        public int priority;

        // constructeur no-arg nécessaire pour Gson
        public CustomRouteInfo() {}

        public CustomRouteInfo(long id, String name, int color) {
                this.id = id;
                this.name = name;
                this.color = color;
                this.description = "";
                this.priority = 0;
        }

        // getters / setters si besoin
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
}
