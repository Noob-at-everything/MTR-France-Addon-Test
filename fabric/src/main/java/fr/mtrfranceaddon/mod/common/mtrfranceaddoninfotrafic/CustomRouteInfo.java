package fr.mtrfranceaddon.mod.common.mtrfranceaddoninfotrafic;

public class CustomRouteInfo {
        private long id;
        private String name;
        private int color;
        private String description = "";
        private int priority = 0;

        public CustomRouteInfo(long id, String name, int color) {
                this.id = id;
                this.name = name;
                this.color = color;
        }

        // Getters & setters
        public long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
}
