package model.entities.plants;

import util.Json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class MuzzleTiming {

    public static final String FILE = "assets/muzzle-timing.json";

    private static final Map<String, Double> FRAMES = new HashMap<String, Double>();
    private static boolean loaded;

    private MuzzleTiming() {
    }

    public static String key(Plants plant, String port, String state) {
        StringBuilder key = new StringBuilder(plant == null ? "ANY" : plant.name());
        key.append('|').append(port == null || port.isEmpty() ? Muzzle.MAIN : port);
        if (state != null && !state.isEmpty()) {
            key.append('|').append(state);
        }
        return key.toString();
    }

    public static synchronized double frameOf(Plants plant, String port, String state,
            double fallback) {
        load();
        Double exact = FRAMES.get(key(plant, port, state));
        if (exact != null) {
            return exact.doubleValue();
        }
        Double plain = FRAMES.get(key(plant, port, ""));
        return plain != null ? plain.doubleValue() : fallback;
    }

    public static synchronized void set(Plants plant, String port, String state, double frame) {
        load();
        FRAMES.put(key(plant, port, state), Double.valueOf(frame));
    }

    public static synchronized void reset() {
        FRAMES.clear();
        loaded = false;
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        File file = new File(FILE);
        if (!file.exists()) {
            return;
        }
        try {
            String text = new String(Files.readAllBytes(file.toPath()),
                    Charset.forName("UTF-8"));
            Object read = Json.parse(text);
            if (!(read instanceof Map)) {
                return;
            }
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) read).entrySet()) {
                FRAMES.put(String.valueOf(entry.getKey()),
                        Double.valueOf(String.valueOf(entry.getValue())));
            }
        } catch (IOException e) {
            util.Log.warn("model", "Could not read " + FILE + ": " + e.getMessage());
        } catch (RuntimeException e) {
            util.Log.warn("model", "Could not read " + FILE + ": " + e.getMessage());
        }
    }

    public static synchronized void save() {
        StringBuilder out = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, Double> entry : FRAMES.entrySet()) {
            if (!first) {
                out.append(",\n");
            }
            first = false;
            out.append("  \"").append(entry.getKey()).append("\": ")
                    .append(Math.round(entry.getValue().doubleValue() * 1000d) / 1000d);
        }
        out.append("\n}\n");
        try {
            Files.write(new File(FILE).toPath(),
                    out.toString().getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            util.Log.warn("model", "Could not write " + FILE + ": " + e.getMessage());
        }
    }
}
