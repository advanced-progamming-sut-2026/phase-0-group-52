package model.entities.plants;

import java.util.Collections;
import java.util.Map;

public final class PlantLeveling {

    private final int maxLevel;
    private final Map<Integer, Integer> xp;
    private final Map<Integer, Integer> packets;
    private final Map<Integer, Integer> coins;

    public PlantLeveling(int maxLevel, Map<Integer, Integer> xp,
            Map<Integer, Integer> packets, Map<Integer, Integer> coins) {
        this.maxLevel = maxLevel;
        this.xp = Collections.unmodifiableMap(xp);
        this.packets = Collections.unmodifiableMap(packets);
        this.coins = Collections.unmodifiableMap(coins);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int xpToLevel(int level) {
        Integer value = xp.get(level);
        return value == null ? 0 : value;
    }

    public int packetsToLevel(int level) {
        Integer value = packets.get(level);
        return value == null ? 0 : value;
    }

    public int coinsToLevel(int level) {
        Integer value = coins.get(level);
        return value == null ? 0 : value;
    }
}
