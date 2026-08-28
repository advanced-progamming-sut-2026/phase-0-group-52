package model.entities.plants;

import model.ChapterType;
import model.User;
import util.Json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PlantData {

    public static final int MAX_LEVEL = 4;

    private static Map<Plants, PlantRecord> records;

    private PlantData() {}

    private static synchronized Map<Plants, PlantRecord> records() {
        if (records == null) {
            records = new EnumMap<Plants, PlantRecord>(Plants.class);
            load();
        }
        return records;
    }

    private static void load() {
        String text = read();
        if (text == null) {
            return;
        }
        Object parsed;
        try {
            parsed = Json.parse(text);
        } catch (RuntimeException e) {
            return;
        }
        if (!(parsed instanceof List)) {
            return;
        }
        for (Object item : (List<?>) parsed) {
            if (!(item instanceof Map)) {
                continue;
            }
            PlantRecord record = PlantRecordBuilder.from((Map<?, ?>) item);
            Plants plant = findByName(record.getName());
            if (plant != null) {
                records.put(plant, record);
            }
        }
    }

    private static String read() {
        InputStream in = PlantData.class.getResourceAsStream("/plants.json");
        if (in == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }

    private static Plants findByName(String name) {
        if (name == null) {
            return null;
        }
        for (Plants plant : Plants.values()) {
            if (plant.getName().equalsIgnoreCase(name)) {
                return plant;
            }
        }
        return null;
    }

    public static PlantRecord record(Plants type) {
        return records().get(type);
    }

    public static List<Plants> ofChapter(ChapterType chapter) {
        List<Plants> result = new ArrayList<Plants>();
        for (Map.Entry<Plants, PlantRecord> e : records().entrySet()) {
            if (e.getValue().getChapter() == chapter) {
                result.add(e.getKey());
            }
        }
        Collections.sort(result, new java.util.Comparator<Plants>() {
            @Override
            public int compare(Plants a, Plants b) {
                return record(a).getChapterOrder() - record(b).getChapterOrder();
            }
        });
        return result;
    }

    public static List<PlantUpgrade> getUpgrades(Plants type) {
        PlantRecord record = record(type);
        return record == null ? Collections.<PlantUpgrade>emptyList() : record.getUpgrades();
    }

    private static double buffTotal(Plants type, int level, String kind) {
        double total = 0;
        for (PlantUpgrade upgrade : getUpgrades(type)) {
            if (upgrade.getLevel() <= level && upgrade.getType().equals(kind)) {
                total += upgrade.getValue();
            }
        }
        return total;
    }

    public static int effectiveHp(Plants type, int level) {
        return (int) Math.max(0, type.getBaseHP() + buffTotal(type, level, "BUFF_HP"));
    }

    public static int effectiveDamage(Plants type, int level) {
        return (int) Math.max(0, type.getDamage() + buffTotal(type, level, "BUFF_DAMAGE"));
    }

    public static double effectiveInterval(Plants type, int level) {
        double base = type.getActionInterval()
                + buffTotal(type, level, "BUFF_ACTION_INTERVAL");
        return Math.max(0.1, base);
    }

    public static int effectiveCost(Plants type, int level) {
        int cost = type.getCost();
        for (PlantUpgrade upgrade : getUpgrades(type)) {
            if (upgrade.getLevel() <= level && upgrade.getType().equals("BUFF_COST")) {
                cost += (int) upgrade.getValue();
            }
        }
        return Math.max(0, cost);
    }

    public static void applyUpgrades(Plant plant, int level) {
        for (PlantUpgrade upgrade : getUpgrades(plant.getType())) {
            if (upgrade.getLevel() > level) {
                continue;
            }
            if (upgrade.getType().equals("BUFF_HP")) {
                plant.setHp(plant.getHp() + upgrade.getValue());
            } else if (upgrade.getType().equals("BUFF_DAMAGE")) {
                plant.setAttackdamage(plant.getAttackdamage() + upgrade.getValue());
            } else if (upgrade.getType().equals("BUFF_ACTION_INTERVAL")) {
                plant.setActionInterval(
                        Math.max(0.1, plant.getActionInterval() + upgrade.getValue()));
            }
        }
    }

    public static int effectiveRecharge(Plants type, int level) {
        double recharge = type.getRecharge();
        for (PlantUpgrade upgrade : getUpgrades(type)) {
            if (upgrade.getLevel() <= level && upgrade.getType().equals("BUFF_RECHARGE")) {
                recharge += upgrade.getValue();
            }
        }
        return (int) Math.max(0, recharge);
    }

    public static boolean canAfford(User user, Plants type) {
        PlantRecord record = record(type);
        PlantProgress state = user.getPlants().progress(type);
        if (record == null || state.isMaxLevel()) {
            return false;
        }
        int next = state.getLevel() + 1;
        return state.getPackets() >= record.getLeveling().packetsToLevel(next)
                || user.getCoins() >= record.getLeveling().coinsToLevel(next);
    }

    public static boolean canUpgrade(User user, Plants type) {
        PlantProgress state = user.getPlants().progress(type);
        if (!state.isUnlocked() || state.isMaxLevel()) {
            return false;
        }
        if (user.isDebugMode()) {
            return true;
        }
        return state.isXpFull() && canAfford(user, type);
    }

    public static String upgradeBlocker(User user, Plants type) {
        PlantRecord record = record(type);
        PlantProgress state = user.getPlants().progress(type);
        if (record == null) {
            return "No data for " + type.getName() + ".";
        }
        if (!state.isUnlocked()) {
            return type.getName() + " is still locked.";
        }
        if (state.isMaxLevel()) {
            return type.getName() + " is already at max level.";
        }
        if (user.isDebugMode()) {
            return null;
        }
        if (!state.isXpFull()) {
            return type.getName() + " needs " + (state.xpNeeded() - state.getXp())
                    + " more planting(s) before it can level up.";
        }
        if (!canAfford(user, type)) {
            int next = state.getLevel() + 1;
            return "Needs " + record.getLeveling().packetsToLevel(next)
                    + " seed packets or " + record.getLeveling().coinsToLevel(next) + " coins.";
        }
        return null;
    }

    public static String upgrade(User user, Plants type) {
        PlantRecord record = record(type);
        PlantProgress state = user.getPlants().progress(type);
        if (record == null) {
            return "Error: No data for " + type.getName() + ".";
        }
        if (!state.isUnlocked()) {
            return "Error: " + type.getName() + " is still locked.";
        }
        if (state.isMaxLevel()) {
            return "Error: " + type.getName() + " is already at max level ("
                    + record.getLeveling().getMaxLevel() + ").";
        }
        int next = state.getLevel() + 1;
        boolean forced = user.isDebugMode();
        if (!forced && !state.isXpFull()) {
            return "Error: " + type.getName() + " needs " + state.xpNeeded()
                    + " plantings to reach level " + next + " (it has " + state.getXp() + ").";
        }
        int packets = record.getLeveling().packetsToLevel(next);
        int coins = record.getLeveling().coinsToLevel(next);
        String spent;
        if (forced) {
            spent = "nothing (cheat mode)";
        } else if (state.getPackets() >= packets) {
            state.setPackets(state.getPackets() - packets);
            spent = packets + " seed packet(s)";
        } else if (user.getCoins() >= coins) {
            user.setCoins(user.getCoins() - coins);
            spent = coins + " coins";
        } else {
            return "Error: Upgrading " + type.getName() + " to level " + next + " needs "
                    + packets + " seed packets or " + coins + " coins.";
        }
        state.setLevel(next);
        state.setXp(0);
        String effect = "";
        for (PlantUpgrade upgrade : getUpgrades(type)) {
            if (upgrade.getLevel() == next) {
                effect = " (" + upgrade.describe() + ")";
            }
        }
        return type.getName() + " upgraded to level " + next + effect + ". Spent " + spent + ".";
    }

    public static boolean canBoost(User user, Plants type) {
        PlantRecord record = record(type);
        if (record == null || !record.isBoostable()) {
            return false;
        }
        if (!user.getPlants().progress(type).isUnlocked()
                || user.getStoredBoosts().contains(type)) {
            return false;
        }
        return user.isDebugMode() || user.getGems() >= record.getGemCost();
    }

    public static String boost(User user, Plants type) {
        PlantRecord record = record(type);
        if (record == null || !record.isBoostable()) {
            return "Error: " + type.getName() + " cannot be boosted.";
        }
        if (!user.getPlants().progress(type).isUnlocked()) {
            return "Error: " + type.getName() + " is still locked.";
        }
        if (user.getStoredBoosts().contains(type)) {
            return "Error: " + type.getName() + " is already boosted.";
        }
        if (!user.isDebugMode() && user.getGems() < record.getGemCost()) {
            return "Error: Boosting " + type.getName() + " needs "
                    + record.getGemCost() + " diamonds.";
        }
        if (!user.isDebugMode()) {
            user.setGems(user.getGems() - record.getGemCost());
        }
        user.getStoredBoosts().add(type);
        return type.getName() + " boosted for " + record.getGemCost() + " diamonds.";
    }
}
