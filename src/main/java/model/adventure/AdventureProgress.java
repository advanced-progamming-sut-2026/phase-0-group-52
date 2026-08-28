package model.adventure;

import model.ChapterType;
import model.entities.plants.PlantCollection;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class AdventureProgress {

    private final Map<ChapterType, Map<Integer, Plants>> claimed =
            new EnumMap<ChapterType, Map<Integer, Plants>>(ChapterType.class);

    public Plants claimedPlant(ChapterType chapter, int slot) {
        return slots(chapter).get(Integer.valueOf(slot));
    }

    public boolean isClaimed(ChapterType chapter, int slot) {
        return claimedPlant(chapter, slot) != null;
    }

    public int claimedCount(ChapterType chapter) {
        return slots(chapter).size();
    }

    public Map<Integer, Plants> slots(ChapterType chapter) {
        Map<Integer, Plants> map = claimed.get(chapter);
        return map == null ? Collections.<Integer, Plants>emptyMap() : map;
    }

    public void record(ChapterType chapter, int slot, Plants plant) {
        if (chapter == null || plant == null
                || slot < 0 || slot >= ChapterMap.PLANT_ISLANDS) {
            return;
        }
        Map<Integer, Plants> map = claimed.get(chapter);
        if (map == null) {
            map = new HashMap<Integer, Plants>();
            claimed.put(chapter, map);
        }
        map.put(Integer.valueOf(slot), plant);
    }

    public List<Plants> remaining(ChapterType chapter) {
        List<Plants> pool = new ArrayList<Plants>(PlantData.ofChapter(chapter));
        pool.removeAll(slots(chapter).values());
        return pool;
    }

    public Plants nextPrize(PlantCollection owned, ChapterType chapter, Random random) {
        List<Plants> pool = remaining(chapter);
        if (pool.isEmpty()) {
            return null;
        }
        List<Plants> fresh = new ArrayList<Plants>();
        for (Plants plant : pool) {
            if (owned == null || !owned.isUnlocked(plant)) {
                fresh.add(plant);
            }
        }
        List<Plants> from = fresh.isEmpty() ? pool : fresh;
        return from.get(random.nextInt(from.size()));
    }
}
