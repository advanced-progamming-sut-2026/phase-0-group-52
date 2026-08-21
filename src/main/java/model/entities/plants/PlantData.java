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
    public static final int SEED_PACKETS_PER_LEVEL = 5;
    public static final int COINS_PER_LEVEL = 500;

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

    public static String upgrade(User user, Plants type) {
        int level = user.getPlantLevel(type);
        if (level >= MAX_LEVEL) {
            return "Error: " + type.getName() + " is already at max level (" + MAX_LEVEL + ").";
        }
        int packets = SEED_PACKETS_PER_LEVEL * level;
        int coins = COINS_PER_LEVEL * level;
        if (user.getSeedPacket() < packets) {
            return "Error: Not enough seed packets. Upgrading to level " + (level + 1)
                    + " needs " + packets + " (you have " + user.getSeedPacket() + ").";
        }
        if (user.getCoins() < coins) {
            return "Error: Not enough coins. Upgrading to level " + (level + 1)
                    + " needs " + coins + " (you have " + user.getCoins() + ").";
        }
        user.setSeedPacket(user.getSeedPacket() - packets);
        user.setCoins(user.getCoins() - coins);
        user.setPlantLevel(type, level + 1);
        String effect = "";
        for (PlantUpgrade upgrade : getUpgrades(type)) {
            if (upgrade.getLevel() == level + 1) {
                effect = " (" + upgrade.describe() + ")";
            }
        }
        return type.getName() + " upgraded to level " + (level + 1) + effect
                + ". Spent " + packets + " seed packets and " + coins + " coins.";
    }
}
