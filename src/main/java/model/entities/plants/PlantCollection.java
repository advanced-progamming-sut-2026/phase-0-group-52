package model.entities.plants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class PlantCollection {

    private static final Random RANDOM = new Random();

    private final Map<Plants, PlantProgress> states =
            new EnumMap<Plants, PlantProgress>(Plants.class);

    public PlantCollection() {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            if (record != null && record.getUnlockKind() == PlantRecord.UnlockKind.STARTER) {
                progress(plant).setUnlocked(true);
            }
        }
    }

    public PlantProgress progress(Plants plant) {
        PlantProgress state = states.get(plant);
        if (state == null) {
            state = new PlantProgress(plant);
            states.put(plant, state);
        }
        return state;
    }

    public Map<Plants, PlantProgress> all() {
        return states;
    }

    public boolean isUnlocked(Plants plant) {
        return progress(plant).isUnlocked();
    }

    public int getLevel(Plants plant) {
        return progress(plant).getLevel();
    }

    public void setLevel(Plants plant, int level) {
        progress(plant).setLevel(level);
    }

    public int totalPackets() {
        int total = 0;
        for (PlantProgress state : states.values()) {
            total += state.getPackets();
        }
        return total;
    }

    public int unlockedCount() {
        int total = 0;
        for (Plants plant : Plants.values()) {
            if (isUnlocked(plant)) {
                total++;
            }
        }
        return total;
    }

    public boolean grant(Plants plant, int count) {
        PlantProgress state = progress(plant);
        int given = Math.max(0, count);
        boolean newlyUnlocked = !state.isUnlocked();
        if (newlyUnlocked) {
            given = Math.max(0, given - 1);
            state.setUnlocked(true);
        }
        state.setPackets(state.getPackets() + given);
        return newlyUnlocked;
    }

    public List<Plants> grantRandom(int count) {
        List<Plants> pool = new ArrayList<Plants>();
        for (Plants plant : Plants.values()) {
            if (!progress(plant).isMaxLevel()) {
                pool.add(plant);
            }
        }
        List<Plants> granted = new ArrayList<Plants>();
        if (pool.isEmpty()) {
            return granted;
        }
        for (int i = 0; i < Math.max(0, count); i++) {
            Plants plant = pool.get(RANDOM.nextInt(pool.size()));
            grant(plant, 1);
            granted.add(plant);
        }
        return granted;
    }

    public void addXp(Plants plant, int amount) {
        PlantProgress state = progress(plant);
        if (!state.isMaxLevel()) {
            state.setXp(state.getXp() + Math.max(0, amount));
        }
    }
}
