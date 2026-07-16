package pvz.model.entities.plants;

import pvz.model.User;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantData {

    public static final int MAX_LEVEL = 4;
    public static final int SEED_PACKETS_PER_LEVEL = 5;
    public static final int COINS_PER_LEVEL = 500;

    private static final Pattern ENTRY = Pattern.compile(
            "\"name\":\\s*\"([^\"]+)\"[\\s\\S]*?\"upgrades\":\\s*\\[([\\s\\S]*?)\\]");
    private static final Pattern UPGRADE = Pattern.compile(
            "\\{\\s*\"level\":\\s*(\\d+),\\s*\"type\":\\s*\"([A-Z_]+)\",\\s*"
                    + "\"value\":\\s*(-?[\\d.]+),\\s*\"specialTag\":\\s*\"([^\"]*)\"\\s*\\}");

    private static Map<Plants, List<PlantUpgrade>> upgrades;

    private PlantData() {}

    private static synchronized Map<Plants, List<PlantUpgrade>> upgrades() {
        if (upgrades == null) {
            upgrades = new HashMap<Plants, List<PlantUpgrade>>();
            load();
        }
        return upgrades;
    }

    private static void load() {
        InputStream in = PlantData.class.getResourceAsStream("/plants.json");
        if (in == null) return;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        } catch (Exception e) {
            return;
        }
        Matcher entry = ENTRY.matcher(sb.toString());
        while (entry.find()) {
            Plants plant = findByName(entry.group(1));
            if (plant == null) continue;
            List<PlantUpgrade> list = new ArrayList<PlantUpgrade>();
            Matcher upgrade = UPGRADE.matcher(entry.group(2));
            while (upgrade.find()) {
                list.add(new PlantUpgrade(Integer.parseInt(upgrade.group(1)), upgrade.group(2),
                        Double.parseDouble(upgrade.group(3)), upgrade.group(4)));
            }
            upgrades.put(plant, list);
        }
    }

    private static Plants findByName(String name) {
        for (Plants plant : Plants.values())
            if (plant.getName().equalsIgnoreCase(name)) return plant;
        return null;
    }

    public static List<PlantUpgrade> getUpgrades(Plants type) {
        List<PlantUpgrade> list = upgrades().get(type);
        return list == null ? Collections.<PlantUpgrade>emptyList() : list;
    }

    public static int effectiveCost(Plants type, int level) {
        int cost = type.getCost();
        for (PlantUpgrade upgrade : getUpgrades(type))
            if (upgrade.getLevel() <= level && upgrade.getType().equals("BUFF_COST"))
                cost += (int) upgrade.getValue();
        return Math.max(0, cost);
    }

    public static void applyUpgrades(Plant plant, int level) {
        for (PlantUpgrade upgrade : getUpgrades(plant.getType())) {
            if (upgrade.getLevel() > level) continue;
            if (upgrade.getType().equals("BUFF_HP"))
                plant.setHp(plant.getHp() + upgrade.getValue());
            else if (upgrade.getType().equals("BUFF_DAMAGE"))
                plant.setAttackdamage(plant.getAttackdamage() + upgrade.getValue());
        }
    }

    public static String upgrade(User user, Plants type) {
        int level = user.getPlantLevel(type);
        if (level >= MAX_LEVEL)
            return "Error: " + type.getName() + " is already at max level (" + MAX_LEVEL + ").";
        int packets = SEED_PACKETS_PER_LEVEL * level;
        int coins = COINS_PER_LEVEL * level;
        if (user.getSeedPacket() < packets)
            return "Error: Not enough seed packets. Upgrading to level " + (level + 1)
                    + " needs " + packets + " (you have " + user.getSeedPacket() + ").";
        if (user.getCoins() < coins)
            return "Error: Not enough coins. Upgrading to level " + (level + 1)
                    + " needs " + coins + " (you have " + user.getCoins() + ").";
        user.setSeedPacket(user.getSeedPacket() - packets);
        user.setCoins(user.getCoins() - coins);
        user.setPlantLevel(type, level + 1);
        String effect = "";
        for (PlantUpgrade upgrade : getUpgrades(type))
            if (upgrade.getLevel() == level + 1) effect = " (" + upgrade.describe() + ")";
        return type.getName() + " upgraded to level " + (level + 1) + effect
                + ". Spent " + packets + " seed packets and " + coins + " coins.";
    }
}
