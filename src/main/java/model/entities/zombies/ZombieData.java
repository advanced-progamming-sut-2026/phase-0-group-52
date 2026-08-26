package model.entities.zombies;

import model.User;
import util.Json;
import util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ZombieData {

    private static final String RESOURCE = "/zombies.json";

    private static List<ZombieRecord> records;
    private static Map<String, ZombieRecord> byAlias;
    private static Map<String, ZombieRecord> byEnumName;

    private ZombieData() {}

    public static synchronized List<ZombieRecord> all() {
        if (records == null) {
            load();
        }
        return records;
    }

    public static ZombieRecord byAlias(String alias) {
        all();
        return alias == null ? null : byAlias.get(alias);
    }

    public static ZombieRecord of(Zombies zombie) {
        all();
        return zombie == null ? null : byEnumName.get(zombie.name());
    }

    public static List<ZombieRecord> inChapter(String chapter) {
        List<ZombieRecord> out = new ArrayList<ZombieRecord>();
        for (ZombieRecord r : all()) {
            if (r.getChapter().equals(chapter)) {
                out.add(r);
            }
        }
        return out;
    }

    public static boolean isSeen(User user, ZombieRecord record) {
        return user != null && record != null
                && user.getSeenZombies().contains(record.getAlias());
    }

    public static int seenCount(User user) {
        int total = 0;
        for (ZombieRecord r : all()) {
            if (isSeen(user, r)) {
                total++;
            }
        }
        return total;
    }

    private static void load() {
        records = new ArrayList<ZombieRecord>();
        byAlias = new LinkedHashMap<String, ZombieRecord>();
        byEnumName = new LinkedHashMap<String, ZombieRecord>();
        String text = read();
        if (text == null) {
            Log.warn("model", "No " + RESOURCE + " on the classpath; the zombie almanac is empty");
            return;
        }
        Object parsed = Json.parse(text);
        if (!(parsed instanceof List)) {
            Log.warn("model", RESOURCE + " is not a list of records");
            return;
        }
        for (Object row : (List<?>) parsed) {
            if (row instanceof Map) {
                add((Map<?, ?>) row);
            }
        }
        records = Collections.unmodifiableList(records);
        Log.debug("model", "Loaded " + records.size() + " zombie records");
    }

    private static void add(Map<?, ?> row) {
        Map<?, ?> packet = child(row, "packet");
        Map<?, ?> animations = child(row, "animations");
        ZombieRecord record = new ZombieRecord(
                Json.intOf(row, "id"),
                Json.str(row, "alias"),
                Json.str(row, "enumName"),
                Json.str(row, "name"),
                Json.str(row, "chapter"),
                Json.str(row, "description"),
                Json.str(row, "flavor"),
                rating(child(row, "toughness")),
                rating(child(row, "speed")),
                Json.doubleOf(row, "hp"),
                Json.doubleOf(row, "eatDps"),
                Json.doubleOf(row, "moveSpeed"),
                Json.intOf(row, "waveCost"),
                Json.intOf(row, "weight"),
                Json.str(row, "armor"),
                Json.boolOf(row, "spawnable"),
                Json.str(packet, "background"),
                Json.str(packet, "icon"),
                placement(packet, "background"),
                placement(packet, "icon"),
                Json.str(animations, "name"),
                Json.str(animations, "zombie"),
                canvas(animations),
                armorParts(row),
                Json.str(child(row, "composite"), "body"),
                Json.str(child(row, "composite"), "head"),
                listOf(child(row, "composite"), "hide"),
                strings(animations));
        if (record.getAlias() == null) {
            return;
        }
        records.add(record);
        byAlias.put(record.getAlias(), record);
        if (record.getEnumName() != null) {
            byEnumName.put(record.getEnumName(), record);
        }
    }

    private static ZombieRecord.Rating rating(Map<?, ?> row) {
        return new ZombieRecord.Rating(Json.intOf(row, "index"),
                Json.str(row, "label"), Math.max(1, Json.intOf(row, "steps")));
    }

    private static float[] placement(Map<?, ?> packet, String prefix) {
        float[] out = new float[]{0f, 0f, 0f, 0f};
        copyPair(packet, prefix + "Offset", out, 0);
        copyPair(packet, prefix + "Size", out, 2);
        return out;
    }

    private static void copyPair(Map<?, ?> packet, String key, float[] out, int at) {
        Object raw = packet == null ? null : packet.get(key);
        if (!(raw instanceof List)) {
            return;
        }
        List<?> pair = (List<?>) raw;
        for (int i = 0; i < 2 && i < pair.size(); i++) {
            if (pair.get(i) instanceof Number) {
                out[at + i] = ((Number) pair.get(i)).floatValue();
            }
        }
    }

    private static List<String> armorParts(Map<?, ?> row) {
        List<String> out = new ArrayList<String>();
        Object raw = row == null ? null : row.get("armorParts");
        if (raw instanceof List) {
            for (Object part : (List<?>) raw) {
                if (part != null) {
                    out.add(part.toString());
                }
            }
        }
        return out;
    }

    private static float[] canvas(Map<?, ?> animations) {
        float[] out = new float[]{0f, 0f};
        copyPair(animations, "canvas", out, 0);
        return out;
    }

    private static List<String> listOf(Map<?, ?> row, String key) {
        List<String> out = new ArrayList<String>();
        Object raw = row == null ? null : row.get(key);
        if (raw instanceof List) {
            for (Object item : (List<?>) raw) {
                if (item != null) {
                    out.add(item.toString());
                }
            }
        }
        return out;
    }

    private static List<String> strings(Map<?, ?> animations) {
        List<String> out = new ArrayList<String>();
        if (animations == null) {
            return out;
        }
        Object clips = animations.get("clips");
        if (clips instanceof List) {
            for (Object clip : (List<?>) clips) {
                if (clip != null) {
                    out.add(clip.toString());
                }
            }
        }
        return out;
    }

    private static Map<?, ?> child(Map<?, ?> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value instanceof Map ? (Map<?, ?>) value : Collections.<String, Object>emptyMap();
    }

    private static String read() {
        InputStream in = ZombieData.class.getResourceAsStream(RESOURCE);
        if (in == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count = in.read(buffer);
            while (count > 0) {
                out.write(buffer, 0, count);
                count = in.read(buffer);
            }
            return new String(out.toByteArray(), "UTF-8");
        } catch (java.io.IOException e) {
            Log.warn("model", "Could not read " + RESOURCE + ": " + e.getMessage());
            return null;
        } finally {
            try {
                in.close();
            } catch (java.io.IOException ignored) {
                Log.debug("model", "Could not close " + RESOURCE);
            }
        }
    }
}
