package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import util.Json;
import util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PacketLayout {

    public static final float CARD_WIDTH = 119f;
    public static final float CARD_HEIGHT = 75f;
    public static final float INNER_LEFT = 5f;
    public static final float INNER_BOTTOM = 8f;

    public static final String PATH = "assets/packet-layout.json";

    public static final class Placement {
        private float x;
        private float y;
        private float scale;

        public Placement(float x, float y, float scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getScale() {
            return scale;
        }

        public void move(float dx, float dy) {
            x += dx;
            y += dy;
        }

        public void zoom(float delta) {
            scale = Math.max(0.1f, Math.min(4f, scale + delta));
        }
    }

    private static Map<Plants, Placement> cache;

    private PacketLayout() {}

    public static synchronized Map<Plants, Placement> all() {
        if (cache == null) {
            cache = new LinkedHashMap<Plants, Placement>();
            load();
        }
        return cache;
    }

    public static Placement of(Plants plant) {
        Placement saved = all().get(plant);
        return saved == null ? fallback(plant) : saved;
    }

    public static Placement fallback(Plants plant) {
        PlantRecord record = PlantData.record(plant);
        if (record == null || record.getIconWidth() == 0) {
            return new Placement(0f, 0f, 1f);
        }
        float x = (CARD_WIDTH - record.getIconWidth()) / 2f;
        float y = CARD_HEIGHT - INNER_BOTTOM - record.getIconHeight();
        return new Placement(x, y, 1f);
    }

    private static void load() {
        FileHandle file = Gdx.files == null ? null : Gdx.files.local(PATH);
        if (file == null || !file.exists()) {
            return;
        }
        Object parsed = Json.parse(file.readString("UTF-8"));
        if (!(parsed instanceof Map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
            Plants plant = plantOf(String.valueOf(entry.getKey()));
            if (plant == null || !(entry.getValue() instanceof Map)) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) entry.getValue();
            cache.put(plant, new Placement((float) Json.doubleOf(row, "x"),
                    (float) Json.doubleOf(row, "y"), (float) Json.doubleOf(row, "scale")));
        }
        Log.debug("gui", "Packet layout loaded for " + cache.size() + " plants");
    }

    private static Plants plantOf(String name) {
        try {
            return Plants.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean first = true;
        for (Plants plant : Plants.values()) {
            Placement p = all().get(plant);
            if (p == null) {
                continue;
            }
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("  \"").append(plant.name()).append("\": {\"x\": ")
                    .append(round(p.getX())).append(", \"y\": ").append(round(p.getY()))
                    .append(", \"scale\": ").append(round(p.getScale())).append('}');
        }
        sb.append("\n}\n");
        Gdx.files.local(PATH).writeString(sb.toString(), false, "UTF-8");
        Log.info("gui", "Packet layout saved to " + PATH);
    }

    private static String round(float value) {
        float snapped = Math.round(value * 100f) / 100f;
        if (snapped == Math.rint(snapped)) {
            return String.valueOf((int) snapped);
        }
        return String.valueOf(snapped);
    }
}
