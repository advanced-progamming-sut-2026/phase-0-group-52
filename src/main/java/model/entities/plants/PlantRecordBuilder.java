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
    PlantsCategory category = PlantsCategory.MODIFIER;
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

    static PlantRecord from(Map<?, ?> m) {
        PlantRecordBuilder b = new PlantRecordBuilder();
        b.id = Json.intOf(m, "id");
        b.name = Json.str(m, "name");
        b.codeName = Json.str(m, "codeName");
        b.category = enumOf(PlantsCategory.class, Json.str(m, "category"), PlantsCategory.MODIFIER);
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

        Map<?, ?> unlock = map(m.get("unlock"));
        b.unlockKind = enumOf(PlantRecord.UnlockKind.class, Json.str(unlock, "kind"),
                PlantRecord.UnlockKind.CHAPTER);
        b.chapter = enumOf(ChapterType.class, Json.str(unlock, "chapter"), null);
        b.chapterOrder = Json.intOf(unlock, "chapterOrder");
        b.seedPacketPrice = Json.intOf(unlock, "seedPacketPrice");
        b.originWorld = Json.str(unlock, "originWorld");

        Map<?, ?> packet = map(m.get("seedPacket"));
        b.packetIcon = Json.str(packet, "icon");
        b.packetBackground = Json.str(packet, "background");

        Map<?, ?> almanac = map(m.get("almanac"));
        b.description = text(Json.str(almanac, "description"));
        b.plantFoodDescription = text(Json.str(almanac, "plantFood"));
        b.flavorText = text(Json.str(almanac, "flavorText"));
        b.stats = statsOf(almanac.get("stats"));
        b.details = statsOf(almanac.get("details"));

        for (Object raw : list(m.get("upgrades"))) {
            Map<?, ?> u = map(raw);
            b.upgrades.add(new PlantUpgrade(Json.intOf(u, "level"), text(Json.str(u, "type")),
                    Json.doubleOf(u, "value"), text(Json.str(u, "specialTag"))));
        }

        b.animations = animationsOf(map(m.get("animations")));
        return new PlantRecord(b);
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
