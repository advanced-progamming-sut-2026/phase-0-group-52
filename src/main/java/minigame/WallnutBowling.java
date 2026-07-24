package minigame;

import model.User;
import model.entities.plants.PlantCombat;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WallnutBowling {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int PLANT_ZONE = 3;
    public static final double ZOMBIE_HP = 190;
    private static final Pattern PLANT =
            Pattern.compile("^plant\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private final List<Zombie> zombies = new ArrayList<Zombie>();
    private final List<Ball> balls = new ArrayList<Ball>();
    private final Queue<Nut> conveyor = new LinkedList<Nut>();
    private final User user;
    private final int level;
    private final int targetKills;
    private int kills;
    private int spawnTimer;
    private int conveyorTimer;
    private boolean over, won;
    public WallnutBowling(int level, User user) {
        this.level = Math.max(1, Math.min(3, level));
        this.user = user;
        this.targetKills = 8 + 4 * this.level;
        conveyor.add(Nut.BOWLING);
        conveyor.add(Nut.BOWLING);
    }

    public void run(Scanner scanner) {
        System.out.println("Wallnut Bowling level " + level + " started. Destroy " + targetKills
                + " zombies. Plant nuts in columns 1-" + PLANT_ZONE + "; they roll right and bounce.");
        System.out.println("Commands: plant -l (x, y) | tick | show | exit");
        render();
        while (!over) {
            System.out.print("bowling> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equals("exit")) {
                System.out.println("Left Wallnut Bowling.");
                return;
            } else if (line.equals("show")) render();
            else if (line.equals("tick")) tick();
            else {
                Matcher m = PLANT.matcher(line);
                if (m.matches()) plant(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
                else System.out.println("invalid command");
            }
        }
        System.out.println(won ? "You destroyed enough zombies. You win!"
                : "A zombie reached your house. You lose!");
        if (won && user != null) {
            user.setCoins(user.getCoins() + 300 * level);
            user.setMiniGamesPlayed(user.getMiniGamesPlayed() + 1);
            System.out.println("Reward: " + (300 * level) + " coins.");
        }
    }

    private void plant(int x, int y) {
        int c = x - 1, r = y - 1;
        if (r < 0 || r >= ROWS || c < 0 || c >= PLANT_ZONE) {
            System.out.println("Error: You can only plant in columns 1-" + PLANT_ZONE + ".");
            return;
        }
        if (conveyor.isEmpty()) {
            System.out.println("Error: The conveyor is empty. Wait (tick) for a nut.");
            return;
        }
        Nut kind = conveyor.poll();
        Ball b = new Ball();
        b.row = r;
        b.x = c;
        b.dir = 0;
        b.kind = kind;
        balls.add(b);
        System.out.println((kind == Nut.BOWLING ? "Bowling Wall-nut" : "Explode-o-nut")
                + " rolled onto row " + y + ".");
    }

    private void tick() {
        conveyorTimer++;
        if (conveyorTimer >= 2) {
            conveyorTimer = 0;
            conveyor.add(PlantCombat.RANDOM.nextInt(4) == 0 ? Nut.EXPLODE : Nut.BOWLING);
        }
        spawnTimer++;
        if (spawnTimer >= 3) {
            spawnTimer = 0;
            Zombie z = new Zombie();
            z.row = PlantCombat.RANDOM.nextInt(ROWS);
            z.x = COLS - 1;
            z.hp = ZOMBIE_HP;
            zombies.add(z);
        }
        for (Ball b : new ArrayList<Ball>(balls)) moveBall(b);
        for (Zombie z : zombies) z.x -= 0.4;
        removeDead();
        for (Zombie z : zombies)
            if (z.x <= 0) {
                over = true;
                won = false;
                return;
            }
        if (kills >= targetKills) {
            over = true;
            won = true;
            return;
        }
        render();
    }

    private void moveBall(Ball b) {
        b.x += 1;
        if (b.row + b.dir < 0 || b.row + b.dir >= ROWS) b.dir = -b.dir;
        Zombie hit = null;
        for (Zombie z : zombies)
            if (z.row == b.row && Math.abs(z.x - b.x) < 0.7 && (hit == null || z.x < hit.x)) hit = z;
        if (hit != null) {
            if (b.kind == Nut.EXPLODE) {
                for (Zombie z : new ArrayList<Zombie>(zombies))
                    if (Math.abs(z.row - b.row) <= 1 && Math.abs(z.x - b.x) <= 1) {
                        z.hp = 0;
                    }
                balls.remove(b);
                removeDead();
                return;
            }
            hit.hp -= ZOMBIE_HP;
            if (hit.hp <= 0) {
                zombies.remove(hit);
                kills++;
            }
            b.dir = (b.dir == 0) ? (PlantCombat.RANDOM.nextBoolean() ? 1 : -1) : -b.dir;
            b.row += b.dir;
            if (b.row < 0) b.row = 1;
            if (b.row >= ROWS) b.row = ROWS - 2;
        }
        if (b.x >= COLS) balls.remove(b);
    }

    private void removeDead() {
        for (int i = zombies.size() - 1; i >= 0; i--)
            if (zombies.get(i).hp <= 0) {
                zombies.remove(i);
                kills++;
            }
    }

    private void render() {
        System.out.println("Kills: " + kills + "/" + targetKills + " | Zombies: " + zombies.size()
                + " | Next nut: " + (conveyor.isEmpty() ? "-" : conveyor.peek()));
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder("  ");
            for (int c = 0; c < COLS; c++) {
                char ch = (c < PLANT_ZONE) ? '_' : '.';
                for (Ball b : balls)
                    if (b.row == r && (int) Math.round(b.x) == c) ch = (b.kind == Nut.EXPLODE) ? 'X' : 'O';
                for (Zombie z : zombies)
                    if (z.row == r && (int) Math.round(z.x) == c) {
                        ch = 'Z';
                        break;
                    }
                sb.append(ch).append(' ');
            }
            System.out.println(sb);
        }
        System.out.println("  _=plant zone  O=bowling nut  X=explode nut  Z=zombie");
    }

    private enum Nut {BOWLING, EXPLODE}

    private static class Zombie {
        int row;
        double x;
        double hp;
    }

    private static class Ball {
        int row;
        double x;
        int dir;
        Nut kind;
    }
}
