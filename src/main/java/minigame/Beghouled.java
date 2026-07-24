package minigame;

import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Beghouled {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int SUN_PER_MATCH_UNIT = 50;

    private static final Plants[] BOARD_TYPES = {
            Plants.PEASHOOTER, Plants.WALL_NUT, Plants.PUFF_SHROOM,
            Plants.CABBAGE_PULT, Plants.SNOW_PEA
    };

    private static final Plants[][] UPGRADES = {
            {Plants.PEASHOOTER, Plants.REPEATER},
            {Plants.REPEATER, Plants.MEGA_GATLING_PEA},
            {Plants.WALL_NUT, Plants.TALL_NUT},
            {Plants.PUFF_SHROOM, Plants.FUME_SHROOM},
            {Plants.CABBAGE_PULT, Plants.MELON_PULT},
            {Plants.MELON_PULT, Plants.WINTER_MELON}
    };
    private static final int[] UPGRADE_COSTS = {500, 1500, 500, 250, 1000, 750};

    private final Plants[][] board = new Plants[ROWS][COLS];
    private final boolean[][] crater = new boolean[ROWS][COLS];
    private final List<double[]> zombies = new ArrayList<double[]>();
    private final User user;
    private final int level;
    private final int targetMatches;

    private int sun = 150;
    private int matches = 0;
    private int turn = 0;
    private boolean over = false;
    private boolean won = false;

    public Beghouled(int level, User user) {
        this.level = Math.max(1, Math.min(3, level));
        this.user = user;
        this.targetMatches = 5 + 5 * this.level;
        fillBoard();
        while (!hasPossibleMove()) resetBoard();
    }

    public void run(Scanner scanner) {
        System.out.println("Beghouled level " + level + " started. Make " + targetMatches
                + " matches of 3+ to win. Sun: " + sun);
        System.out.println("Commands: show | swap <x1> <y1> <x2> <y2> | upgrades | upgrade <plant> | exit");
        render();
        while (!over) {
            System.out.print("beghouled> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (line.equals("exit")) {
                System.out.println("Left Beghouled.");
                return;
            } else if (line.equals("show")) {
                render();
            } else if (line.equals("upgrades")) {
                printUpgrades();
            } else if (parts[0].equals("upgrade") && parts.length >= 2) {
                doUpgrade(line.substring("upgrade".length()).trim());
            } else if (parts[0].equals("swap") && parts.length == 5) {
                doSwap(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
            } else {
                System.out.println("invalid command");
            }
        }
        if (won) {
            System.out.println("You made " + matches + " matches. You win!");
            if (user != null) {
                user.setCoins(user.getCoins() + 200 * level);
                user.setMiniGamesPlayed(user.getMiniGamesPlayed() + 1);
                System.out.println("Reward: " + (200 * level) + " coins.");
            }
        } else {
            System.out.println("A zombie reached your house. You lose!");
        }
    }

    private void fillBoard() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                board[r][c] = randomTypeAvoidingMatch(r, c);
    }

    private void resetBoard() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (!crater[r][c]) board[r][c] = null;
        fillGaps();
    }

    private Plants randomTypeAvoidingMatch(int r, int c) {
        while (true) {
            Plants t = BOARD_TYPES[PlantCombat.RANDOM.nextInt(BOARD_TYPES.length)];
            if (c >= 2 && t == board[r][c - 1] && t == board[r][c - 2]) continue;
            if (r >= 2 && t == board[r - 1][c] && t == board[r - 2][c]) continue;
            return t;
        }
    }

    private void doSwap(int x1, int y1, int x2, int y2) {
        int c1 = x1 - 1, r1 = y1 - 1, c2 = x2 - 1, r2 = y2 - 1;
        if (!inBounds(r1, c1) || !inBounds(r2, c2)) {
            System.out.println("Error: Out of bounds.");
            return;
        }
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) {
            System.out.println("Error: You can only swap adjacent plants.");
            return;
        }
        if (crater[r1][c1] || crater[r2][c2] || board[r1][c1] == null || board[r2][c2] == null) {
            System.out.println("Error: You cannot swap craters or empty tiles.");
            return;
        }
        swap(r1, c1, r2, c2);
        if (findMatches().isEmpty()) {
            swap(r1, c1, r2, c2);
            System.out.println("Error: That swap does not create a match of 3.");
            return;
        }
        resolveMatches(false);
        tickZombies();
        if (!over) render();
        if (!over && !hasPossibleMove()) {
            System.out.println("No more possible matches; the board resets!");
            resetBoard();
            render();
        }
    }

    private void swap(int r1, int c1, int r2, int c2) {
        Plants tmp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = tmp;
    }

    private void resolveMatches(boolean cascade) {
        List<int[]> matched = findMatches();
        if (matched.isEmpty()) return;
        List<List<int[]>> groups = groupMatches(matched);
        for (List<int[]> group : groups) {
            int size = group.size();
            int gained = (size - 2) * SUN_PER_MATCH_UNIT + (cascade ? SUN_PER_MATCH_UNIT : 0);
            sun += gained;
            matches++;
            System.out.println("Match of " + size + "! +" + gained + " sun (total " + sun
                    + "). Matches: " + matches + "/" + targetMatches);
        }
        for (int[] cell : matched)
            board[cell[0]][cell[1]] = null;
        fillGaps();
        if (matches >= targetMatches) {
            over = true;
            won = true;
            return;
        }
        resolveMatches(true);
    }

    private List<int[]> findMatches() {
        boolean[][] mark = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c + 2 < COLS; c++) {
                Plants t = board[r][c];
                if (t == null) continue;
                int len = 1;
                while (c + len < COLS && board[r][c + len] == t) len++;
                if (len >= 3)
                    for (int i = 0; i < len; i++) mark[r][c + i] = true;
            }
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r + 2 < ROWS; r++) {
                Plants t = board[r][c];
                if (t == null) continue;
                int len = 1;
                while (r + len < ROWS && board[r + len][c] == t) len++;
                if (len >= 3)
                    for (int i = 0; i < len; i++) mark[r + i][c] = true;
            }
        }
        List<int[]> result = new ArrayList<int[]>();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (mark[r][c]) result.add(new int[]{r, c});
        return result;
    }

    private List<List<int[]>> groupMatches(List<int[]> matched) {
        boolean[][] in = new boolean[ROWS][COLS];
        for (int[] cell : matched) in[cell[0]][cell[1]] = true;
        boolean[][] seen = new boolean[ROWS][COLS];
        List<List<int[]>> groups = new ArrayList<List<int[]>>();
        for (int[] cell : matched) {
            if (seen[cell[0]][cell[1]]) continue;
            List<int[]> group = new ArrayList<int[]>();
            List<int[]> stack = new ArrayList<int[]>();
            stack.add(cell);
            seen[cell[0]][cell[1]] = true;
            while (!stack.isEmpty()) {
                int[] cur = stack.remove(stack.size() - 1);
                group.add(cur);
                int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] d : deltas) {
                    int nr = cur[0] + d[0], nc = cur[1] + d[1];
                    if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS
                            && in[nr][nc] && !seen[nr][nc]) {
                        seen[nr][nc] = true;
                        stack.add(new int[]{nr, nc});
                    }
                }
            }
            groups.add(group);
        }
        return groups;
    }

    private void fillGaps() {
        for (int c = 0; c < COLS; c++) {
            for (int r = ROWS - 1; r >= 0; r--) {
                if (crater[r][c] || board[r][c] != null) continue;
                for (int above = r - 1; above >= 0; above--) {
                    if (crater[above][c]) continue;
                    if (board[above][c] != null) {
                        board[r][c] = board[above][c];
                        board[above][c] = null;
                        break;
                    }
                }
            }
            for (int r = 0; r < ROWS; r++)
                if (!crater[r][c] && board[r][c] == null)
                    board[r][c] = BOARD_TYPES[PlantCombat.RANDOM.nextInt(BOARD_TYPES.length)];
        }
    }

    private boolean hasPossibleMove() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (crater[r][c] || board[r][c] == null) continue;
                if (c + 1 < COLS && trySwapCheck(r, c, r, c + 1)) return true;
                if (r + 1 < ROWS && trySwapCheck(r, c, r + 1, c)) return true;
            }
        }
        return false;
    }

    private boolean trySwapCheck(int r1, int c1, int r2, int c2) {
        if (crater[r2][c2] || board[r2][c2] == null) return false;
        swap(r1, c1, r2, c2);
        boolean ok = !findMatches().isEmpty();
        swap(r1, c1, r2, c2);
        return ok;
    }

    private void tickZombies() {
        turn++;
        if (turn % 3 == 0)
            zombies.add(new double[]{PlantCombat.RANDOM.nextInt(ROWS), COLS - 1});
        for (double[] z : new ArrayList<double[]>(zombies)) {
            int r = (int) z[0];
            int c = (int) z[1];
            if (board[r][c] != null && !crater[r][c]) {
                System.out.println("A zombie ate the " + board[r][c].getName()
                        + " at (" + (c + 1) + ", " + (r + 1) + ") and left a crater!");
                board[r][c] = null;
                crater[r][c] = true;
                zombies.remove(z);
                continue;
            }
            z[1] = c - 1;
            if (z[1] < 0) {
                over = true;
                won = false;
                return;
            }
        }
    }

    private void printUpgrades() {
        System.out.println("Upgrades (upgrade <plant>):");
        for (int i = 0; i < UPGRADES.length; i++)
            System.out.println("  " + UPGRADES[i][0].getName() + " -> "
                    + UPGRADES[i][1].getName() + " | " + UPGRADE_COSTS[i] + " sun");
    }

    private void doUpgrade(String name) {
        String normalized = name.replace('-', '_').replace(' ', '_');
        for (int i = 0; i < UPGRADES.length; i++) {
            Plants from = UPGRADES[i][0];
            if (!from.getName().equalsIgnoreCase(name) && !from.name().equalsIgnoreCase(normalized))
                continue;
            if (sun < UPGRADE_COSTS[i]) {
                System.out.println("Error: Not enough sun. This upgrade costs " + UPGRADE_COSTS[i] + ".");
                return;
            }
            int count = 0;
            for (int r = 0; r < ROWS; r++)
                for (int c = 0; c < COLS; c++)
                    if (board[r][c] == from) {
                        board[r][c] = UPGRADES[i][1];
                        count++;
                    }
            if (count == 0) {
                System.out.println("Error: There is no " + from.getName() + " on the board.");
                return;
            }
            sun -= UPGRADE_COSTS[i];
            System.out.println("Upgraded " + count + " " + from.getName() + "(s) to "
                    + UPGRADES[i][1].getName() + ". Sun: " + sun);
            resolveMatches(false);
            return;
        }
        System.out.println("Error: No upgrade available for: " + name);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private void render() {
        System.out.println("Sun: " + sun + " | Matches: " + matches + "/" + targetMatches
                + " | Zombies: " + zombies.size());
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder("  ");
            for (int c = 0; c < COLS; c++) {
                char ch;
                if (crater[r][c]) ch = '#';
                else if (board[r][c] == null) ch = '.';
                else ch = symbol(board[r][c]);
                boolean zombieHere = false;
                for (double[] z : zombies)
                    if ((int) z[0] == r && (int) z[1] == c) {
                        zombieHere = true;
                        break;
                    }
                sb.append(ch).append(zombieHere ? '!' : ' ').append(' ');
            }
            System.out.println(sb);
        }
        System.out.println("  P=Peashooter R=Repeater G=MegaGatling W=Wall-nut T=Tall-nut"
                + " U=Puff-shroom F=Fume-shroom C=Cabbage M=Melon I=WinterMelon S=SnowPea # crater ! zombie");
    }

    private char symbol(Plants type) {
        switch (type) {
            case PEASHOOTER:
                return 'P';
            case REPEATER:
                return 'R';
            case MEGA_GATLING_PEA:
                return 'G';
            case WALL_NUT:
                return 'W';
            case TALL_NUT:
                return 'T';
            case PUFF_SHROOM:
                return 'U';
            case FUME_SHROOM:
                return 'F';
            case CABBAGE_PULT:
                return 'C';
            case MELON_PULT:
                return 'M';
            case WINTER_MELON:
                return 'I';
            case SNOW_PEA:
                return 'S';
            default:
                return '?';
        }
    }
}
