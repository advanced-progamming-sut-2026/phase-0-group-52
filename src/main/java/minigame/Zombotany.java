package minigame;

import model.User;
import model.entities.plants.PlantCombat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Zombotany {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int PLANT_ZONE = 3;

    private enum ZType { PEASHOOTER, WALLNUT, JALAPENO, SQUASH }

    private static final Pattern PLANT =
            Pattern.compile("^plant\\s+-t\\s+(\\S+)\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");

    private static class Zombie { int row; double x; double hp; ZType type; double shotTimer; }
    private static class Plant { int row; int col; double hp; boolean shooter; double shotTimer; }

    private final List<Zombie> zombies = new ArrayList<Zombie>();
    private final List<Plant> plants = new ArrayList<Plant>();

    private final User user;
    private final int level;
    private final int targetKills;
    private int kills;
    private int spawnTimer;
    private boolean over, won;

    public Zombotany(int level, User user) {
        this.level = Math.max(1, Math.min(3, level));
        this.user = user;
        this.targetKills = 6 + 4 * this.level;
    }

    public void run(Scanner scanner) {
        System.out.println("Zombotany level " + level + " started. Destroy " + targetKills
                + " plant-headed zombies. Plant defenders in columns 1-" + PLANT_ZONE + ".");
        System.out.println("Zombies: Peashooter (shoots), Wall-nut (tanky), Jalapeno (blasts its row)," +
                " Squash (crushes).");
        System.out.println("Commands: plant -t <peashooter|wallnut> -l (x, y) | tick | show | exit");
        render();
        while (!over) {
            System.out.print("zombotany> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equals("exit")) { System.out.println("Left Zombotany."); return; }
            else if (line.equals("show")) render();
            else if (line.equals("tick") || line.startsWith("tick ")) {
                int n = 1;
                String[] tp = line.split("\\s+");
                if (tp.length >= 2)
                { try { n = Math.max(1, Integer.parseInt(tp[1])); } catch (NumberFormatException ignored) {} }
                for (int ti = 0; ti < n && !over; ti++) tick();
            }
            else {
                Matcher m = PLANT.matcher(line);
                if (m.matches()) plant(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                else System.out.println("invalid command");
            }
        }
        System.out.println(won ? "You cleared the plant-headed horde. You win!"
                : "A zombie reached your house. You lose!");
        if (won && user != null) {
            user.setCoins(user.getCoins() + 300 * level);
            user.setMiniGamesPlayed(user.getMiniGamesPlayed() + 1);
            System.out.println("Reward: " + (300 * level) + " coins.");
        }
    }

    private void plant(String type, int x, int y) {
        int c = x - 1, r = y - 1;
        if (r < 0 || r >= ROWS || c < 0 || c >= PLANT_ZONE) {
            System.out.println("Error: You can only plant in columns 1-" + PLANT_ZONE + ".");
            return;
        }
        for (Plant p : plants)
            if (p.row == r && p.col == c) { System.out.println("Error: That tile is occupied."); return; }
        boolean shooter = type.equalsIgnoreCase("peashooter");
        if (!shooter && !type.equalsIgnoreCase("wallnut")) {
            System.out.println("Error: Choose peashooter or wallnut.");
            return;
        }
        Plant p = new Plant();
        p.row = r; p.col = c; p.shooter = shooter; p.hp = shooter ? 300 : 1200;
        plants.add(p);
        System.out.println((shooter ? "Peashooter" : "Wall-nut") + " planted at (" + x + ", " + y + ").");
    }

    private void tick() {
        spawnTimer++;
        if (spawnTimer >= 3) {
            spawnTimer = 0;
            spawnZombie();
        }
        for (Plant p : plants) if (p.shooter) shootPlant(p);
        for (Zombie z : new ArrayList<Zombie>(zombies)) stepZombie(z);
        cleanup();
        for (Zombie z : zombies) if (z.x <= 0) { over = true; won = false; return; }
        if (kills >= targetKills) { over = true; won = true; return; }
        render();
    }

    private void spawnZombie() {
        Zombie z = new Zombie();
        z.row = PlantCombat.RANDOM.nextInt(ROWS);
        z.x = COLS - 1;
        ZType[] roster = { ZType.PEASHOOTER, ZType.WALLNUT, ZType.JALAPENO, ZType.SQUASH };
        z.type = roster[PlantCombat.RANDOM.nextInt(Math.min(roster.length, 1 + level))];
        z.hp = z.type == ZType.WALLNUT ? 800 : 190;
        zombies.add(z);
    }

    private void shootPlant(Plant p) {
        Zombie target = null;
        for (Zombie z : zombies)
            if (z.row == p.row && z.x >= p.col && (target == null || z.x < target.x)) target = z;
        if (target != null) target.hp -= 25;
    }

    private void stepZombie(Zombie z) {
        if (z.type == ZType.PEASHOOTER) {
            z.shotTimer += 1;
            if (z.shotTimer >= 2) {
                z.shotTimer = 0;
                for (Plant p : plants)
                    if (p.row == z.row && p.col < z.x) { p.hp -= 40; break; }
            }
        }
        Plant here = plantAt(z.row, (int) Math.round(z.x - 1));
        if (here != null) {
            if (z.type == ZType.SQUASH) { here.hp = 0; z.hp = 0; }
            else here.hp -= 50;
            return;
        }
        z.x -= 0.4;
    }

    private Plant plantAt(int row, int col) {
        for (Plant p : plants) if (p.row == row && p.col == col && p.hp > 0) return p;
        return null;
    }

    private void cleanup() {
        for (int i = plants.size() - 1; i >= 0; i--) if (plants.get(i).hp <= 0) plants.remove(i);
        for (int i = zombies.size() - 1; i >= 0; i--) {
            Zombie z = zombies.get(i);
            if (z.hp <= 0) {
                if (z.type == ZType.JALAPENO) {
                    for (Plant p : plants) if (p.row == z.row) p.hp = 0;
                    for (Zombie o : zombies) if (o != z && o.row == z.row) o.hp = 0;
                    System.out.println("A Jalapeno zombie blasted row " + (z.row + 1) + "!");
                }
                zombies.remove(i);
                kills++;
            }
        }
        for (int i = plants.size() - 1; i >= 0; i--) if (plants.get(i).hp <= 0) plants.remove(i);
    }

    private void render() {
        System.out.println("Kills: " + kills + "/" + targetKills + " | Zombies: " + zombies.size());
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder("  ");
            for (int c = 0; c < COLS; c++) {
                char ch = (c < PLANT_ZONE) ? '_' : '.';
                for (Plant p : plants) if (p.row == r && p.col == c) ch = p.shooter ? 'P' : 'W';
                for (Zombie z : zombies) if (z.row == r && (int) Math.round(z.x) == c) ch = zChar(z.type);
                sb.append(ch).append(' ');
            }
            System.out.println(sb.toString());
        }
        System.out.println("  P=peashooter W=wall-nut | S=shooter-Z N=nut-Z J=jalapeno-Z Q=squash-Z");
    }

    private char zChar(ZType t) {
        switch (t) {
            case PEASHOOTER: return 'S';
            case WALLNUT:    return 'N';
            case JALAPENO:   return 'J';
            default:         return 'Q';
        }
    }
}
