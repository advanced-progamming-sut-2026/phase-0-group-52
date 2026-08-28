package model.entities.plants;

import model.ChapterType;
import util.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PlantRecordBuilder {

    int id;
    String name;
    String codeName;
    PlantsCategory category = PlantsCategory.MAGIC;
    String mintFamily;
    List<PlantTag> tags = new ArrayList<PlantTag>();
    String damageText = "";
    String ability = "";
    String plantFoodEffect = "";
    PlantRecord.UnlockKind unlockKind = PlantRecord.UnlockKind.CHAPTER;
    ChapterType chapter;
    int chapterOrder;
    int seedPacketPrice;
    String originWorld;
    String packetIcon;
    String packetBackground;
    String description = "";
    String plantFoodDescription = "";
    String flavorText = "";
    List<PlantRecord.Stat> stats = new ArrayList<PlantRecord.Stat>();
    List<PlantRecord.Stat> details = new ArrayList<PlantRecord.Stat>();
    List<PlantUpgrade> upgrades = new ArrayList<PlantUpgrade>();
    PlantAnimations animations;
    PlantLeveling leveling;
    boolean boostable;
    int gemCost;
    String categoryBadge;
    int iconOffsetX;
    int iconOffsetY;
    int iconWidth;
    int iconHeight;

    static PlantRecord from(Map<?, ?> m) {
        PlantRecordBuilder b = new PlantRecordBuilder();
        b.id = Json.intOf(m, "id");
        b.name = Json.str(m, "name");
        b.codeName = Json.str(m, "codeName");
        b.category = enumOf(PlantsCategory.class, Json.str(m, "category"), PlantsCategory.MAGIC);
        b.mintFamily = Json.str(m, "mintFamily");
        b.damageText = text(Json.str(m, "damageText"));
        b.ability = text(Json.str(m, "ability"));
        b.plantFoodEffect = text(Json.str(m, "plantFoodEffect"));

        for (Object tag : list(m.get("tags"))) {
            PlantTag parsed = enumOf(PlantTag.class, String.valueOf(tag), null);
            if (parsed != null) {
                b.tags.add(parsed);
            }
        }

        b.readUnlock(map(m.get("unlock")));
        b.readAlmanac(map(m.get("almanac")), map(m.get("seedPacket")));

        for (Object raw : list(m.get("upgrades"))) {
            Map<?, ?> u = map(raw);
            b.upgrades.add(new PlantUpgrade(Json.intOf(u, "level"), text(Json.str(u, "type")),
                    Json.doubleOf(u, "value"), text(Json.str(u, "specialTag"))));
        }

        b.animations = animationsOf(map(m.get("animations")));
        b.categoryBadge = Json.str(m, "categoryBadge");

        Map<?, ?> boost = map(m.get("boost"));
        b.boostable = Json.boolOf(boost, "boostable");
        b.gemCost = Json.intOf(boost, "gemCost");

        Map<?, ?> lv = map(m.get("leveling"));
        b.leveling = new PlantLeveling(Json.intOf(lv, "maxLevel"),
                levels(lv.get("xpToLevel")), levels(lv.get("packetsToLevel")),
                levels(lv.get("coinsToLevel")));
        return new PlantRecord(b);
    }

    private void readUnlock(Map<?, ?> unlock) {
        unlockKind = enumOf(PlantRecord.UnlockKind.class, Json.str(unlock, "kind"),
                PlantRecord.UnlockKind.CHAPTER);
        chapter = enumOf(ChapterType.class, Json.str(unlock, "chapter"), null);
        chapterOrder = Json.intOf(unlock, "chapterOrder");
        seedPacketPrice = Json.intOf(unlock, "seedPacketPrice");
        originWorld = Json.str(unlock, "originWorld");
    }

    private void readAlmanac(Map<?, ?> almanac, Map<?, ?> packet) {
        packetIcon = Json.str(packet, "icon");
        packetBackground = Json.str(packet, "background");
        iconOffsetX = pair(packet.get("iconOffset"), 0);
        iconOffsetY = pair(packet.get("iconOffset"), 1);
        iconWidth = pair(packet.get("iconSize"), 0);
        iconHeight = pair(packet.get("iconSize"), 1);
        description = text(Json.str(almanac, "description"));
        plantFoodDescription = text(Json.str(almanac, "plantFood"));
        flavorText = text(Json.str(almanac, "flavorText"));
        stats = statsOf(almanac.get("stats"));
        details = statsOf(almanac.get("details"));
    }

    private static int pair(Object raw, int index) {
        if (!(raw instanceof List)) {
            return 0;
        }
        List<?> values = (List<?>) raw;
        if (index >= values.size() || !(values.get(index) instanceof Number)) {
            return 0;
        }
        return ((Number) values.get(index)).intValue();
    }

    private static PlantAnimations animationsOf(Map<?, ?> a) {
        List<Object> canvas = list(a.get("canvas"));
        int width = canvas.size() > 0 && canvas.get(0) instanceof Number
                ? ((Number) canvas.get(0)).intValue() : 0;
        int height = canvas.size() > 1 && canvas.get(1) instanceof Number
                ? ((Number) canvas.get(1)).intValue() : 0;

        Map<String, Double> clips = new LinkedHashMap<String, Double>();
        for (Map.Entry<?, ?> e : map(a.get("clips")).entrySet()) {
            if (e.getValue() instanceof Number) {
                clips.put(String.valueOf(e.getKey()), ((Number) e.getValue()).doubleValue());
            }
        }
        Map<String, String> effects = new LinkedHashMap<String, String>();
        for (Map.Entry<?, ?> e : map(a.get("effects")).entrySet()) {
            effects.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }
        return new PlantAnimations(Json.str(a, "plant"), width, height, clips, effects);
    }

    private static Map<Integer, Integer> levels(Object raw) {
        Map<Integer, Integer> out = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<?, ?> e : map(raw).entrySet()) {
            if (e.getValue() instanceof Number) {
                try {
                    out.put(Integer.valueOf(String.valueOf(e.getKey())),
                            ((Number) e.getValue()).intValue());
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }
        }
        return out;
    }

    private static List<PlantRecord.Stat> statsOf(Object raw) {
        List<PlantRecord.Stat> result = new ArrayList<PlantRecord.Stat>();
        for (Object item : list(raw)) {
            Map<?, ?> s = map(item);
            result.add(new PlantRecord.Stat(text(Json.str(s, "label")), text(Json.str(s, "value"))));
        }
        return result;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, String name, E fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static List<Object> list(Object raw) {
        return raw instanceof List ? castList(raw) : new ArrayList<Object>();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> castList(Object raw) {
        return (List<Object>) raw;
    }

    private static Map<?, ?> map(Object raw) {
        return raw instanceof Map ? (Map<?, ?>) raw : new LinkedHashMap<Object, Object>();
    }
}
