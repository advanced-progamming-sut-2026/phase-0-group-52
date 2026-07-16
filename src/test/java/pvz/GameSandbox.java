package pvz;

import pvz.controller.menu.GameMenuController;
import pvz.model.App;
import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.GameField;
import pvz.model.User;
import pvz.model.entities.Cell;
import pvz.model.entities.CellType;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;
import pvz.model.mechanics.ChapterMechanics;
import pvz.view.MenuType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class GameSandbox {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Chapter? 1=ANCIENT_EGYPT 2=FROSTBITE_CAVES 3=BIG_WAVE_BEACH 4=DARK_AGES (default 2):");
        ChapterType chapter = readChapter(scanner);

        GameField field = new GameField(chapter);
        Game game = new Game(null, null, null, field, 200, new ArrayList<Plant>(), null);
        App app = new App(game, new ArrayList<User>(), null, MenuType.GAME_MENU, null);
        app.setGame(game);
        game.setApp(app);
        GameMenuController controller = new GameMenuController(app);
        ChapterMechanics mechanics = ChapterMechanics.forChapter(chapter);

        System.out.println("Sandbox started. Chapter: " + chapter + " | Sun: " + game.getSunAmount());
        System.out.println("Extra sandbox commands:");
        System.out.println("  tick [n]           -> advance time n seconds (default 1)");
        System.out.println("  wave               -> start next wave (chapter mechanics run)");
        System.out.println("  sun <n>            -> set sun amount");
        System.out.println("  tile <x> <y> <type>-> set tile type (NORMAL/TOMBSTONE/WATER/FROZEN/SLIPPERY_UP/SLIPPERY_DOWN/LOW_GROUND/NECROMANCY)");
        System.out.println("  exit               -> quit");
        System.out.println("Everything else goes to GameMenuController (plant/pluck/feed/cheat/show/zombies).");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equals("exit")) break;
            String[] parts = line.split("\\s+");
            switch (parts[0]) {
                case "tick":
                    int n = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    for (int i = 0; i < n; i++) tick(game, mechanics);
                    System.out.println("Advanced " + n + " second(s).");
                    break;
                case "wave":
                    game.setCurrentWaveIndex(game.getCurrentWaveIndex() + 1);
                    if (mechanics != null) mechanics.onWaveStart(game);
                    System.out.println("Wave " + (game.getCurrentWaveIndex() + 1) + " started.");
                    break;
                case "sun":
                    game.setSunAmount(Integer.parseInt(parts[1]));
                    System.out.println("Sun set to " + game.getSunAmount() + ".");
                    break;
                case "tile":
                    setTile(field, parts);
                    break;
                default:
                    controller.handleCommand(parts);
            }
        }
        System.out.println("Sandbox finished.");
    }

    private static ChapterType readChapter(Scanner scanner) {
        String choice = scanner.hasNextLine() ? scanner.nextLine().replaceAll("[^0-9]", "") : "";
        if (choice.equals("1")) return ChapterType.ANCIENT_EGYPT;
        if (choice.equals("3")) return ChapterType.BIG_WAVE_BEACH;
        if (choice.equals("4")) return ChapterType.DARK_AGES;
        return ChapterType.FROSTBITE_CAVES;
    }

    private static void tick(Game game, ChapterMechanics mechanics) {
        for (Plant p : new ArrayList<Plant>(game.getPlants()))
            p.onTick(game);
        for (Zombie z : new ArrayList<Zombie>(game.getZombies()))
            z.onTick(game);
        if (mechanics != null) mechanics.onTick(game);
        for (Map.Entry<Plants, Double> e : game.getCooldowns().entrySet())
            if (e.getValue() > 0) e.setValue(Math.max(0, e.getValue() - 1));
        for (int i = game.getZombies().size() - 1; i >= 0; i--) {
            Zombie z = game.getZombies().get(i);
            if (z.isDead()) {
                z.onDeath(game);
                game.getZombies().remove(i);
            }
        }
        game.setCurrentTick(game.getCurrentTick() + 1);
    }

    private static void setTile(GameField field, String[] parts) {
        if (parts.length < 4) {
            System.out.println("Usage: tile <x> <y> <type>");
            return;
        }
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        Cell cell = field.getCell(x - 1, y - 1);
        if (cell == null) {
            System.out.println("Out of bounds.");
            return;
        }
        try {
            CellType type = CellType.valueOf(parts[3].toUpperCase());
            cell.setType(type);
            if (type == CellType.NECROMANCY) cell.setNecromancy(true);
            System.out.println("Tile (" + x + ", " + y + ") is now " + type + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown tile type: " + parts[3]);
        }
    }
}
