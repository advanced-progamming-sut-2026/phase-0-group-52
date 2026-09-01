package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import util.Log;

import java.util.HashMap;
import java.util.Map;

public final class EntityTuning {

    public static final String FILE = "entity-tuning.json";

    public static final class Tune {
        public float dx;
        public float dy;
        public float scale = 1f;
        public float speed = 1f;
        public float dw;
        public float dh;

        public boolean isDefault() {
            return dx == 0f && dy == 0f && scale == 1f && speed == 1f
                    && dw == 0f && dh == 0f;
        }
    }

    private static final Map<String, Tune> TUNES = new HashMap<String, Tune>();
    private static boolean loaded;
    private static boolean dirty;

    private EntityTuning() {}

    public static synchronized Tune of(String key) {
        return edit(key);
    }

    public static synchronized Tune edit(String key) {
        load();
        Tune found = TUNES.get(key);
        if (found == null) {
            found = new Tune();
            TUNES.put(key, found);
        }
        return found;
    }

    public static synchronized void touch() {
        dirty = true;
        save();
    }

    public static String plantKey(String enumName) {
        return "plant|" + enumName;
    }

    public static String gridKey(String chapter) {
        return "grid|" + (chapter == null ? "ANY" : chapter);
    }

    public static void applyGrid(String chapter) {
        Tune tune = of(gridKey(chapter));
        LawnGeometry.setPlayArea(LawnGeometry.defaultX() + tune.dx,
                LawnGeometry.defaultY() + tune.dy,
                LawnGeometry.defaultWidth() + tune.dw,
                LawnGeometry.defaultHeight() + tune.dh);
    }

    public static String zombieKey(String enumName) {
        return "zombie|" + enumName;
    }

    public static final float PLANT_SCALE = 1.35f;
    public static final float ZOMBIE_SCALE = 1.55f;

    public static void place(com.badlogic.gdx.scenes.scene2d.Actor actor, Tune tune,
            float column, int row, boolean zombie) {
        float base = zombie ? ZOMBIE_SCALE : PLANT_SCALE;
        float width = LawnGeometry.cellWidth() * base * tune.scale;
        float height = LawnGeometry.cellHeight() * base * tune.scale;
        actor.setBounds(LawnGeometry.areaX() + (column + 0.5f) * LawnGeometry.cellWidth()
                        - width / 2f + tune.dx,
                LawnGeometry.rowFeet(row) + tune.dy, width, height);
    }

    private static void load() {
        if (loaded || Gdx.files == null) {
            return;
        }
        loaded = true;
        FileHandle handle = Gdx.files.local("assets/" + FILE);
        if (!handle.exists()) {
            handle = Gdx.files.internal(FILE);
        }
        if (!handle.exists()) {
            return;
        }
        try {
            JsonValue root = new JsonReader().parse(handle);
            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                Tune tune = new Tune();
                tune.dx = entry.getFloat("dx", 0f);
                tune.dy = entry.getFloat("dy", 0f);
                tune.scale = entry.getFloat("scale", 1f);
                tune.speed = entry.getFloat("speed", 1f);
                tune.dw = entry.getFloat("dw", 0f);
                tune.dh = entry.getFloat("dh", 0f);
                TUNES.put(collapse(entry.name), merge(TUNES.get(collapse(entry.name)), tune));
            }
            Log.info("gui", "Loaded " + TUNES.size() + " entity tunings");
            dirty = true;
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not read " + FILE + ": " + e.getMessage());
        }
    }

    private static String collapse(String key) {
        if (key == null || key.startsWith("grid|")) {
            return key;
        }
        int first = key.indexOf('|');
        int second = key.indexOf('|', first + 1);
        return second < 0 ? key : key.substring(0, second);
    }

    private static Tune merge(Tune existing, Tune candidate) {
        if (existing == null) {
            return candidate;
        }
        return weight(candidate) > weight(existing) ? candidate : existing;
    }

    private static float weight(Tune tune) {
        return Math.abs(tune.dx) + Math.abs(tune.dy)
                + Math.abs(tune.scale - 1f) * 100f + Math.abs(tune.speed - 1f) * 100f;
    }

    public static synchronized void save() {
        if (!dirty || Gdx.files == null) {
            return;
        }
        StringBuilder out = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, Tune> entry : TUNES.entrySet()) {
            Tune tune = entry.getValue();
            if (tune.isDefault()) {
                continue;
            }
            if (!first) {
                out.append(",\n");
            }
            first = false;
            out.append("  \"").append(entry.getKey()).append("\": {")
                    .append("\"dx\": ").append(round(tune.dx))
                    .append(", \"dy\": ").append(round(tune.dy))
                    .append(", \"scale\": ").append(round(tune.scale))
                    .append(", \"speed\": ").append(round(tune.speed))
                    .append(", \"dw\": ").append(round(tune.dw))
                    .append(", \"dh\": ").append(round(tune.dh))
                    .append("}");
        }
        out.append("\n}\n");
        try {
            Gdx.files.local("assets/" + FILE).writeString(out.toString(), false, "UTF-8");
            dirty = false;
            Log.info("gui", "Entity tuning saved to assets/" + FILE);
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not write " + FILE + ": " + e.getMessage());
        }
    }

    private static String round(float value) {
        return String.valueOf(Math.round(value * 1000f) / 1000f);
    }

}
