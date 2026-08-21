package minigame;

import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vasebreaker {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int PACKET_LIFESPAN = 8;

    private enum Content { SEED_PACKET, ZOMBIE, RANDOM }

    private static final Plants[] VASE_PLANTS = {
            Plants.PEASHOOTER, Plants.WALL_NUT, Plants.SNOW_PEA, Plants.CHERRY_BOMB, Plants.SQUASH
    };

    private static final Pattern BREAK =
            Pattern.compile("^break\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern PLANT =
            Pattern.compile("^plant\\s+-t\\s+(.+?)\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");

    private static class Zombie { int row; double x; double hp; boolean giant; }
    private static class Plant { int row, col; Plants type; double hp; }
    private static class Packet { int row, col; Plants type; int ticksLeft; }

    private final Content[][] vase = new Content[ROWS][COLS];
    private final Plants[][] vasePlant = new Plants[ROWS][COLS];
    private final List<Zombie> zombies = new ArrayList<Zombie>();
    private final List<Plant> plants = new ArrayList<Plant>();
    private final List<Packet> ground = new ArrayList<Packet>();

    private final User user;
    private final int level;
    private int vasesLeft;
    private boolean over, won;

    public Vasebreaker(int level, User user) {
        this.level = Math.max(1, Math.min(3, level));
        this.user = user;
        setupVases();
    }

    private void setupVases() {
        int count = 6 + 3 * level;
        int placed = 0, tries = 0;
        while (placed < count && tries < 500) {
            tries++;
            int r = PlantCombat.RANDOM.nextInt(ROWS);
            int c = 2 + PlantCombat.RANDOM.nextInt(COLS - 2);
            if (vase[r][c] != null) continue;
            double roll = PlantCombat.RANDOM.nextDouble();
            if (roll < 0.45) {
                vase[r][c] = Content.SEED_PACKET;
                vasePlant[r][c] = VASE_PLANTS[PlantCombat.RANDOM.nextInt(VASE_PLANTS.length)];
            } else if (roll < 0.8) {
                vase[r][c] = Content.ZOMBIE;
            } else {
                vase[r][c] = Content.RANDOM;
            }
            placed++;
        }
        vasesLeft = placed;
    }

    public void run(Scanner scanner) {
        System.out.println("Vasebreaker level " + level + " started. Break all " + vasesLeft
                + " vases without letting a zombie reach your house.");
        System.out.println("Seed packets fall on the ground and vanish after " + PACKET_LIFESPAN
                + " ticks — pick one up by planting it right away (you cannot store it).");
        System.out.println("Commands: break (x, y) | plant -t <type> -l (x, y) | tick [n] | show | exit");
        render();
        while (!over) {
            System.out.print("vasebreaker> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equals("exit")) { System.out.println("Left Vasebreaker."); return; }
            else if (line.equals("show")) render();
            else if (line.equals("tick") || line.startsWith("tick ")) {
                int n = 1;
                String[] tp = line.split("\\s+");
                if (tp.length >= 2) {
                    try { n = Math.max(1, Integer.parseInt(tp[1])); } catch (NumberFormatException ignored) {} }
                for (int ti = 0; ti < n && !over; ti++) tick();
            }
            else handle(line);
            if (!over) checkEnd();
        }
        System.out.println(won ? "All vases broken and yard cleared. You win!"
                : "A zombie reached your house. You lose!");
        if (won && user != null) {
            user.setCoins(user.getCoins() + 300 * level);
            user.setMiniGamesPlayed(user.getMiniGamesPlayed() + 1);
            System.out.println("Reward: " + (300 * level) + " coins.");
        }
    }

    private void handle(String line) {
        Matcher m = BREAK.matcher(line);
        if (m.matches()) { breakVase(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); return; }
        m = PLANT.matcher(line);
        if (m.matches()) { plant(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))); return; }
        System.out.println("invalid command");
    }

    private void breakVase(int x, int y) {
        int c = x - 1, r = y - 1;
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS || vase[r][c] == null) {
            System.out.println("Error: No vase at (" + x + ", " + y + ").");
            return;
        }
        Content content = vase[r][c];
        Plants seed = vasePlant[r][c];
        vase[r][c] = null;
        vasePlant[r][c] = null;
        vasesLeft--;
        if (content == Content.RANDOM) {
            double roll = PlantCombat.RANDOM.nextDouble();
            if (roll < 0.45) {
                content = Content.SEED_PACKET; seed = VASE_PLANTS[PlantCombat.RANDOM.nextInt(VASE_PLANTS.length)];}
            else if (roll < 0.85) {
                spawnZombie(r, c, false); System.out.println("A zombie burst out of the random vase at (" + x + ", " + y + ")!"); return; }
            else {
                spawnZombie(r, c, true); System.out.println("A Gargantuar smashed out of the random vase at (" + x + ", " + y + ")!"); return; }
        }
        if (content == Content.ZOMBIE) {
            spawnZombie(r, c, false);
            System.out.println("A zombie burst out of the vase at (" + x + ", " + y + ")!");
        } else {
            Packet p = new Packet();
            p.row = r; p.col = c; p.type = seed; p.ticksLeft = PACKET_LIFESPAN;
            ground.add(p);
            System.out.println("A " + seed.getName() + " seed packet dropped at (" + x + ", " + y
                    + ") — plant it within " + PACKET_LIFESPAN + " ticks!");
        }
    }

    private void spawnZombie(int r, int c, boolean giant) {
        Zombie z = new Zombie();
        z.row = r; z.x = c; z.giant = giant; z.hp = giant ? 3000 : 190;
        zombies.add(z);
    }

    private void plant(String name, int x, int y) {
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        Packet packet = null;
        for (Packet p : ground) if (p.type == type) { packet = p; break; }
        if (packet == null) {
            System.out.println("Error: There is no " + type.getName()
                    + " seed packet on the ground. Break a seed-packet vase first.");
            return;
        }
        int c = x - 1, r = y - 1;
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS) { System.out.println("Error: Out of bounds."); return; }
        ground.remove(packet);
        Plant p = new Plant();
        p.row = r; p.col = c; p.type = type; p.hp = type.getBaseHP();
        plants.add(p);
        System.out.println(type.getName() + " planted at (" + x + ", " + y + ").");
        if (type == Plants.CHERRY_BOMB || type == Plants.SQUASH) {
            for (Zombie z : new ArrayList<Zombie>(zombies))
                if (Math.abs(z.row - r) <= 1 && Math.abs(z.x - c) <= 1) z.hp = 0;
            plants.remove(p);
            removeDead();
        }
    }

    private void tick() {
        for (int i = ground.size() - 1; i >= 0; i--) {
            Packet p = ground.get(i);
            p.ticksLeft--;
            if (p.ticksLeft <= 0) {
                System.out.println("The " + p.type.getName() + " seed packet at ("
                        + (p.col + 1) + ", " + (p.row + 1) + ") vanished.");
                ground.remove(i);
            }
        }
        for (Plant p : plants) {
            if (p.type.getDamage() <= 0) continue;
            Zombie target = null;
            for (Zombie z : zombies)
                if (z.row == p.row && z.x >= p.col && (target == null || z.x < target.x)) target = z;
            if (target != null) target.hp -= p.type.getDamage();
        }
        removeDead();
        for (Zombie z : zombies) {
            boolean blocked = false;
            for (Plant p : plants) if (p.row == z.row && Math.abs(p.col - z.x) < 0.6) blocked = true;
            if (!blocked) z.x -= z.giant ? 0.25 : 0.5;
        }
        for (Zombie z : zombies) if (z.x <= 0) { over = true; won = false; return; }
        render();
    }

    private void removeDead() {
        for (int i = zombies.size() - 1; i >= 0; i--) if (zombies.get(i).hp <= 0) zombies.remove(i);
    }

    private void checkEnd() {
        if (over) return;
        if (vasesLeft <= 0 && zombies.isEmpty()) { over = true; won = true; }
    }

    private Plants findPlant(String input) {
        String n = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values())
            if (p.getName().equalsIgnoreCase(input.trim()) || p.name().equalsIgnoreCase(n)) return p;
        return null;
    }

    private char vaseChar(Content content) {
        switch (content) {
            case SEED_PACKET: return 'p';
            case ZOMBIE:      return '#';
            default:          return '?';
        }
    }

    private void render() {
        System.out.println("Vases left: " + vasesLeft + " | Zombies: " + zombies.size()
                + " | Seed packets on ground: " + ground.size());
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder("  ");
            for (int c = 0; c < COLS; c++) {
                char ch = '.';
                if (vase[r][c] != null) ch = vaseChar(vase[r][c]);
                for (Packet p : ground) if (p.row == r && p.col == c) ch = '$';
                for (Plant p : plants)
                    if (p.row == r && p.col == c) ch = Character.toUpperCase(p.type.getName().charAt(0));
                for (Zombie z : zombies)
                    if (z.row == r && (int) Math.round(z.x) == c) ch = z.giant ? 'G' : 'Z';
                sb.append(ch).append(' ');
            }
            System.out.println(sb.toString());
        }
        System.out.println("  vases: p=seed-packet  #=zombie  ?=random  |  $=seed packet on ground"
                + "  |  Z=zombie  G=gargantuar  |  UPPERCASE=your plants");
        if (!ground.isEmpty()) {
            StringBuilder inv = new StringBuilder("  on ground: ");
            for (Packet p : ground) inv.append(p.type.getName()).append("(").append(p.ticksLeft).append("t)  ");
            System.out.println(inv.toString());
        }
    }
}
