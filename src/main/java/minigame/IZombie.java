package minigame;

import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IZombie {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int RED_LINE = 3;

    private static final Zombies[] ROSTER = {
            Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ARMOR1, Zombies.ZOMBIE_ARMOR2,
            Zombies.ZOMBIE_IMP, Zombies.ZOMBIE_GARGANTUAR
    };
    private static final int[] COST = { 50, 75, 125, 25, 150 };

    private static final Pattern PLACE =
            Pattern.compile("^place\\s+-t\\s+(\\S+)\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");

    private static class Plant { int row, col; Plants type; double hp; }
    private static class Zombie { int row; double x; double hp; double dmg; }

    private final List<Plant> plants = new ArrayList<Plant>();
    private final List<Zombie> zombies = new ArrayList<Zombie>();
    private final boolean[] brainEaten = new boolean[ROWS];

    private final User user;
    private final int level;
    private int sun = 150;
    private boolean over, won;

    public IZombie(int level, User user) {
        this.level = Math.max(1, Math.min(3, level));
        this.user = user;
        setupPlants();
    }

    private void setupPlants() {
        Plants[] pool = { Plants.PEASHOOTER, Plants.WALL_NUT, Plants.SNOW_PEA };
        int count = 4 + level;
        for (int i = 0; i < count; i++) {
            int r = PlantCombat.RANDOM.nextInt(ROWS);
            int c = PlantCombat.RANDOM.nextInt(RED_LINE);
            Plant p = new Plant();
            p.row = r; p.col = c; p.type = pool[PlantCombat.RANDOM.nextInt(pool.length)];
            p.hp = p.type.getBaseHP();
            plants.add(p);
        }
    }

    public void run(Scanner scanner) {
        System.out.println("I, Zombie level " + level + " started. You are the zombies! Sun: " + sun);
        System.out.println("Place zombies right of the red line (column " + (RED_LINE + 1)
                + "+) and eat the brain at the left of every row.");
        System.out.println("Roster: default(50) armor1(75) armor2(125) imp(25) gargantuar(150)");
        System.out.println("Commands: place -t <default|armor1|armor2|imp|gargantuar> -l (x, y) | tick | show | exit");
        render();
        while (!over) {
            System.out.print("izombie> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equals("exit")) { System.out.println("Left I, Zombie."); return; }
            else if (line.equals("show")) render();
            else if (line.equals("tick")) tick();
            else {
                Matcher m = PLACE.matcher(line);
                if (m.matches()) place(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                else System.out.println("invalid command");
            }
        }
        System.out.println(won ? "You ate every brain. You win!" : "Your zombies are gone. You lose!");
        if (won && user != null) {
            user.setCoins(user.getCoins() + 250 * level);
            user.setMiniGamesPlayed(user.getMiniGamesPlayed() + 1);
            System.out.println("Reward: " + (250 * level) + " coins.");
        }
    }

    private void place(String name, int x, int y) {
        int idx = rosterIndex(name);
        if (idx < 0) { System.out.println("Error: Unknown zombie: " + name); return; }
        int c = x - 1, r = y - 1;
        if (r < 0 || r >= ROWS || c < RED_LINE || c >= COLS) {
            System.out.println("Error: Place zombies right of the red line (column " + (RED_LINE + 1) + "+).");
            return;
        }
        if (sun < COST[idx]) { System.out.println("Error: Not enough sun (need " + COST[idx] + ")."); return; }
        sun -= COST[idx];
        Zombies data = ROSTER[idx];
        Zombie z = new Zombie();
        z.row = r; z.x = c; z.hp = data.getHp() + data.getArmor().ordinal() * 200; z.dmg = 100;
        zombies.add(z);
        System.out.println("Placed " + data.getName() + " at (" + x + ", " + y + "). Sun: " + sun);
    }

    private void tick() {
        for (Plant p : plants) {
            if (p.type.getDamage() <= 0) continue;
            Zombie target = null;
            for (Zombie z : zombies)
                if (z.row == p.row && z.x >= p.col && (target == null || z.x < target.x)) target = z;
            if (target != null) target.hp -= p.type.getDamage();
        }
        removeDeadZombies();
        for (Zombie z : zombies) {
            Plant blocker = null;
            for (Plant p : plants) if (p.row == z.row && Math.abs(p.col - z.x) < 0.6) blocker = p;
            if (blocker != null) {
                blocker.hp -= z.dmg;
                if (blocker.hp <= 0) plants.remove(blocker);
            } else {
                z.x -= 0.5;
                if (z.x <= 0 && !brainEaten[z.row]) {
                    brainEaten[z.row] = true;
                    System.out.println("A zombie ate the brain in row " + (z.row + 1) + "!");
                }
            }
        }
        boolean allEaten = true;
        for (int r = 0; r < ROWS; r++) if (!brainEaten[r]) allEaten = false;
        if (allEaten) { over = true; won = true; return; }
        if (zombies.isEmpty() && sun < minCost()) { over = true; won = false; return; }
        render();
    }

    private int minCost() {
        int min = Integer.MAX_VALUE;
        for (int c : COST) min = Math.min(min, c);
        return min;
    }

    private void removeDeadZombies() {
        for (int i = zombies.size() - 1; i >= 0; i--) if (zombies.get(i).hp <= 0) zombies.remove(i);
    }

    private int rosterIndex(String name) {
        switch (name.toLowerCase()) {
            case "default": return 0;
            case "armor1": return 1;
            case "armor2": return 2;
            case "imp": return 3;
            case "gargantuar": return 4;
            default: return -1;
        }
    }

    private void render() {
        System.out.println("Sun: " + sun + " | Zombies: " + zombies.size()
                + " | Brains left: " + brainsLeft());
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder("  ");
            sb.append(brainEaten[r] ? ' ' : 'B').append('|');
            for (int c = 0; c < COLS; c++) {
                char ch = (c == RED_LINE) ? ':' : '.';
                for (Plant p : plants) if (p.row == r && p.col == c) ch = Character.toUpperCase(p.type.getName().charAt(0));
                for (Zombie z : zombies) if (z.row == r && (int) Math.round(z.x) == c) ch = 'Z';
                sb.append(ch).append(' ');
            }
            System.out.println(sb.toString());
        }
        System.out.println("  B=brain  :=red line  Z=zombie  letters=plants");
    }

    private int brainsLeft() {
        int n = 0;
        for (boolean b : brainEaten) if (!b) n++;
        return n;
    }
}
