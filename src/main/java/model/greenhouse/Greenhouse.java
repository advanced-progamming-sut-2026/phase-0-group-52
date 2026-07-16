package model.greenhouse;

import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

public class Greenhouse {

    public static final int ROWS = 4;
    public static final int COLS = 5;
    public static final int MARIGOLD_REWARD = 500;
    public static final long HOUR_MILLIS = 3600L * 1000;

    private final Pot[][] pots = new Pot[ROWS][COLS];

    public Greenhouse() {
        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                pots[y][x] = new Pot(x + 1, y + 1, y == 0);
    }

    public Pot getPot(int x, int y) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) return null;
        return pots[y - 1][x - 1];
    }

    public boolean unlockNextPot() {
        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                if (!pots[y][x].isUnlocked()) {
                    pots[y][x].setUnlocked(true);
                    return true;
                }
        return false;
    }

    public String plantPot(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot == null) return "Error: Pot (" + x + ", " + y + ") does not exist.";
        if (!pot.isUnlocked()) return "Error: Pot (" + x + ", " + y + ") is locked.";
        if (pot.isOccupied()) return "Error: Pot (" + x + ", " + y + ") is already occupied.";
        if (PlantCombat.RANDOM.nextBoolean()) {
            pot.plantMarigold();
            return "Planted a Marigold at (" + x + ", " + y + "). Ready in 2 hours.";
        }
        Plants plant = randomUnlockedPlant();
        pot.plantSpecial(plant);
        return "Planted a " + plant.getName() + " at (" + x + ", " + y + "). Ready in 8 hours.";
    }

    private Plants randomUnlockedPlant() {
        Plants[] all = Plants.values();
        Plants plant;
        do {
            plant = all[PlantCombat.RANDOM.nextInt(all.length)];
        } while (plant.name().endsWith("MINT"));
        return plant;
    }

    public String collect(int x, int y, User user) {
        Pot pot = getPot(x, y);
        if (pot == null) return "Error: Pot (" + x + ", " + y + ") does not exist.";
        if (!pot.isUnlocked()) return "Error: Pot (" + x + ", " + y + ") is locked.";
        if (!pot.isOccupied()) return "Error: Pot (" + x + ", " + y + ") is empty.";
        if (!pot.isReady()) return "Error: The plant at (" + x + ", " + y + ") is not fully grown yet.";
        if (pot.isMarigold()) {
            pot.clear();
            user.setCoins(user.getCoins() + MARIGOLD_REWARD);
            return "Collected a Marigold: +" + MARIGOLD_REWARD + " coins. You have " + user.getCoins() + " coins.";
        }
        Plants plant = pot.getPlantType();
        pot.clear();
        if (user.getStoredBoosts().contains(plant))
            return "Collected " + plant.getName() + " but its boost was already stored; the pot is now empty.";
        user.getStoredBoosts().add(plant);
        return "Collected " + plant.getName() + ": a boost is stored for its next use in a level.";
    }

    public String grow(int x, int y, User user) {
        Pot pot = getPot(x, y);
        if (pot == null) return "Error: Pot (" + x + ", " + y + ") does not exist.";
        if (!pot.isOccupied()) return "Error: Pot (" + x + ", " + y + ") is empty.";
        if (pot.isReady()) return "Error: The plant at (" + x + ", " + y + ") is already ready to collect.";
        int cost = pot.remainingHoursCeil();
        if (user.getGems() < cost)
            return "Error: Not enough diamonds. Growing costs " + cost + " diamond(s).";
        user.setGems(user.getGems() - cost);
        pot.finishGrowth();
        return "Spent " + cost + " diamond(s). The plant at (" + x + ", " + y + ") is now fully grown.";
    }

    public void print() {
        System.out.println("Greenhouse (" + COLS + "x" + ROWS + "):");
        for (int y = 1; y <= ROWS; y++) {
            StringBuilder sb = new StringBuilder("  row " + y + ": ");
            for (int x = 1; x <= COLS; x++) {
                sb.append("(").append(x).append(",").append(y).append(") ")
                        .append(getPot(x, y).describe());
                if (x < COLS) sb.append("  |  ");
            }
            System.out.println(sb.toString());
        }
    }
}
