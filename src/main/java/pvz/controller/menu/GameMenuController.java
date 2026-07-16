package pvz.controller.menu;

import pvz.model.App;
import pvz.model.Game;
import pvz.model.GameField;
import pvz.model.Vec2;
import pvz.model.entities.Cell;
import pvz.model.entities.CellType;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantData;
import pvz.model.entities.plants.PlantFactory;
import pvz.model.entities.plants.PlantTag;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.types.PeaPod;
import pvz.model.entities.zombies.Zombie;
import pvz.model.entities.zombies.ZombieFactory;
import pvz.model.entities.zombies.Zombies;
import pvz.model.level.ConveyorBeltLevel;
import pvz.model.level.PlantWhatYouGet;
import pvz.view.GameMenu;
import pvz.view.MenuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameMenuController {

    private static final Pattern PLANT_CMD =
            Pattern.compile("^plant\\s+plant\\s+-t\\s+(.+?)\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern PLUCK_CMD =
            Pattern.compile("^pluck\\s+plant\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern FEED_CMD =
            Pattern.compile("^feed\\s+plant\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern TILE_CMD =
            Pattern.compile("^show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern SPAWN_CMD =
            Pattern.compile("^cheat\\s+spawn-zombie\\s+-t\\s+(\\S+)\\s+-l\\s*\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?$");

    private final App app;
    private final GameMenu view;

    public GameMenuController(App app) {
        this.app = app;
        this.view = new GameMenu();
    }

    public GameMenu getView() { return view; }

    public void handleCommand(String[] parts) {
        if (parts == null || parts.length == 0) {
            view.showError("Invalid command.");
            return;
        }
        String command = String.join(" ", parts).trim();
        switch (parts[0]) {
            case "menu":
                handleMenu(parts);
                break;
            case "plant":
                handlePlant(command);
                break;
            case "pluck":
                handlePluck(command);
                break;
            case "feed":
                handleFeed(command);
                break;
            case "cheat":
                handleCheat(command, parts);
                break;
            case "show":
                handleShow(command, parts);
                break;
            case "zombies":
                if (parts.length >= 2 && parts[1].equals("info")) {
                    Game game = requireGame();
                    if (game != null) view.showZombiesInfo(game.getZombies());
                } else {
                    view.showError("Usage: zombies info");
                }
                break;
            case "start":
                handleStartWaves(command);
                break;
            default:
                view.showError("Unknown command: " + parts[0]);
        }
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            try {
                MenuType target = MenuType.fromName(parts[2]);
                if (target == null) throw new IllegalArgumentException();
                app.setCurrentmenu(target);
                if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
            } catch (IllegalArgumentException e) {
                view.showError("Unknown menu: " + parts[2]);
            }
            return;
        }
        view.showError("Usage: menu show current  |  menu enter <menu_name>");
    }

    private void handlePlant(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = PLANT_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: plant plant -t <type> -l (<x>, <y>)");
            return;
        }
        Plants type = findPlantType(m.group(1));
        if (type == null) {
            view.showError("Unknown plant: " + m.group(1));
            return;
        }
        if (game.getLevel() != null && !game.getLevel().isPlantAllowed(type)) {
            view.showError(type.getName() + " is not available in this level.");
            return;
        }
        int x = Integer.parseInt(m.group(2));
        int y = Integer.parseInt(m.group(3));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;

        if (type == Plants.PEA_POD) {
            for (Plant p : cell.getPlants()) {
                if (p instanceof PeaPod) {
                    handlePeaPodHead(game, (PeaPod) p, x, y);
                    return;
                }
            }
        }

        String plantError = plantError(cell, type);
        if (plantError != null) {
            view.showError(plantError);
            return;
        }
        boolean conveyor = game.getLevel() instanceof ConveyorBeltLevel;
        int plantLevel = app.getCurrentuser() != null ? app.getCurrentuser().getPlantLevel(type) : 1;
        int cost = PlantData.effectiveCost(type, plantLevel);
        if (conveyor && !((ConveyorBeltLevel) game.getLevel()).hasOnBelt(type)) {
            view.showError(type.getName() + " is not on the conveyor belt.");
            return;
        }
        if (!conveyor && !game.isCooldownsRemoved() && game.isOnCooldown(type)) {
            view.showError(type.getName() + " is recharging; ready in "
                    + String.format("%.1f", game.getRemainingCooldown(type)) + "s.");
            return;
        }
        if (!conveyor && game.getSunAmount() < cost) {
            view.showError("Not enough sun. Need " + cost
                    + ", have " + game.getSunAmount() + ".");
            return;
        }

        if (conveyor) ((ConveyorBeltLevel) game.getLevel()).takeFromBelt(type);
        else game.spendSun(cost);
        Plant plant = PlantFactory.create(type, new Vec2(x - 1, y - 1));
        PlantData.applyUpgrades(plant, plantLevel);
        cell.getPlants().add(plant);
        game.getPlants().add(plant);
        if (!conveyor && !game.isCooldownsRemoved()) game.startCooldown(type);
        plant.onPlanted(game);
        view.showPlanted(type.getName(), x, y);
        if (app.getCurrentuser() != null && app.getCurrentuser().getStoredBoosts().remove(type)) {
            plant.boost();
            plant.onPlantFood(game);
            System.out.println(type.getName() + " was boosted by your greenhouse harvest!");
        }
    }

    private void handlePeaPodHead(Game game, PeaPod pod, int x, int y) {
        Plants type = Plants.PEA_POD;
        if (pod.getHeads() >= PeaPod.MAX_HEADS) {
            view.showError("Pea Pod at (" + x + ", " + y + ") already has "
                    + PeaPod.MAX_HEADS + " heads.");
            return;
        }
        if (!game.isCooldownsRemoved() && game.isOnCooldown(type)) {
            view.showError(type.getName() + " is recharging; ready in "
                    + String.format("%.1f", game.getRemainingCooldown(type)) + "s.");
            return;
        }
        if (game.getSunAmount() < type.getCost()) {
            view.showError("Not enough sun. Need " + type.getCost()
                    + ", have " + game.getSunAmount() + ".");
            return;
        }
        game.spendSun(type.getCost());
        pod.addHead();
        if (!game.isCooldownsRemoved()) game.startCooldown(type);
        System.out.println("Pea Pod at (" + x + ", " + y + ") now has "
                + pod.getHeads() + " heads.");
    }

    private String plantError(Cell cell, Plants type) {
        CellType cellType = cell.getType();

        if (type == Plants.HOT_POTATO)
            return cellType == CellType.FROZEN
                    ? null : "Hot Potato can only be planted on a frozen tile.";
        if (type == Plants.GRAVE_BUSTER)
            return cellType == CellType.TOMBSTONE
                    ? null : "Grave Buster can only be planted on a tombstone.";

        if (cell.getPlants().size() >= 2)
            return "This tile is full.";

        if (cellType == CellType.WATER) {
            boolean isWaterPlant = type.getTags().contains(PlantTag.WATER);
            if (cell.getPlants().isEmpty())
                return isWaterPlant ? null : "Only water plants can be planted on water; plant a Lily Pad first.";
            return canStack(cell.getPlants().get(0), type)
                    ? null : "Cannot plant " + type.getName() + " on top of "
                    + cell.getPlants().get(0).getType().getName() + ".";
        }
        if (!cellType.isPlantable())
            return "Cannot plant on this tile (" + cellType + ").";
        if (cell.getPlants().isEmpty())
            return null;
        return canStack(cell.getPlants().get(0), type)
                ? null : "Cannot plant two plants on this tile.";
    }

    private boolean canStack(Plant below, Plants above) {
        return below.getType().getTags().contains(PlantTag.STACK)
                || above.getTags().contains(PlantTag.STACK);
    }

    private Plants findPlantType(String input) {
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values()) {
            if (p.getName().equalsIgnoreCase(input.trim())
                    || p.name().equalsIgnoreCase(normalized))
                return p;
        }
        return null;
    }

    private void handlePluck(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = PLUCK_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: pluck plant -l (<x>, <y>)");
            return;
        }
        int x = Integer.parseInt(m.group(1));
        int y = Integer.parseInt(m.group(2));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;
        if (cell.getPlants().isEmpty()) {
            view.showError("No plant at (" + x + ", " + y + ").");
            return;
        }
        Plant top = cell.getPlants().remove(cell.getPlants().size() - 1);
        game.getPlants().remove(top);
        view.showPlucked(top.getType().getName(), x, y);
    }

    private void handleFeed(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = FEED_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: feed plant -l (<x>, <y>)");
            return;
        }
        if (game.getPlantFoodCount() <= 0) {
            view.showError("You have no plant food.");
            return;
        }
        int x = Integer.parseInt(m.group(1));
        int y = Integer.parseInt(m.group(2));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;
        if (cell.getPlants().isEmpty()) {
            view.showError("No plant at (" + x + ", " + y + ").");
            return;
        }
        Plant top = cell.getPlants().get(cell.getPlants().size() - 1);
        game.setPlantFoodCount(game.getPlantFoodCount() - 1);
        top.onPlantFood(game);
        view.showFed(top.getType().getName(), x, y, game.getPlantFoodCount());
    }

    private void handleCheat(String command, String[] parts) {
        Game game = requireGame();
        if (game == null) return;
        if (parts.length < 2) {
            view.showError("Usage: cheat remove-cooldown  |  cheat add-plant-food  |  cheat spawn-zombie -t <type> -l <x, y>");
            return;
        }
        switch (parts[1]) {
            case "remove-cooldown":
                game.clearAllCooldowns();
                game.setCooldownsRemoved(true);
                view.showCooldownsRemoved();
                break;
            case "add-plant-food":
                game.setPlantFoodCount(game.getPlantFoodCount() + 1);
                view.showPlantFoodAdded(game.getPlantFoodCount());
                break;
            case "spawn-zombie":
                handleSpawnZombie(game, command);
                break;
            default:
                view.showError("Unknown cheat: " + parts[1]);
        }
    }

    private void handleSpawnZombie(Game game, String command) {
        Matcher m = SPAWN_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: cheat spawn-zombie -t <zombie-type> -l <x, y>");
            return;
        }
        Zombies data = findZombieType(m.group(1));
        if (data == null) {
            view.showError("Unknown zombie: " + m.group(1));
            return;
        }
        int x = Integer.parseInt(m.group(2));
        int y = Integer.parseInt(m.group(3));
        GameField field = game.getField();
        if (field == null || !field.inBounds(x - 1, y - 1)) {
            view.showError("Location out of bounds: (" + x + ", " + y + ").");
            return;
        }
        Zombie zombie = ZombieFactory.create(data, y - 1, new Vec2(x - 1, y - 1),
                field.getChapter(), null);
        game.getZombies().add(zombie);
        view.showZombieSpawned(zombie.getClass().getSimpleName(), x, y);
    }

    private Zombies findZombieType(String input) {
        String normalized = input.trim().replace('-', '_').replace(' ', '_');
        for (Zombies z : Zombies.values()) {
            if (z.name().equalsIgnoreCase(normalized)
                    || z.name().equalsIgnoreCase("ZOMBIE_" + normalized)
                    || z.getName().equalsIgnoreCase(input.trim())
                    || z.getName().equalsIgnoreCase("Zombie" + input.trim()))
                return z;
        }
        return null;
    }

    private void handleShow(String command, String[] parts) {
        Game game = requireGame();
        if (game == null) return;
        if (parts.length >= 2 && parts[1].equals("map")) {
            view.showMap(game);
            return;
        }
        if (parts.length >= 3 && parts[1].equals("plants") && parts[2].equals("status")) {
            List<Plants> types = game.getChosenPlants().isEmpty()
                    ? Arrays.asList(Plants.values())
                    : new ArrayList<Plants>(game.getChosenPlants());
            view.showPlantsStatus(types, game);
            return;
        }
        Matcher m = TILE_CMD.matcher(command);
        if (m.matches()) {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            Cell cell = cellAt(game, x, y);
            if (cell == null) return;
            List<Zombie> zombiesHere = new ArrayList<Zombie>();
            for (Zombie z : game.getZombies())
                if (z.getRow() == y - 1 && z.getCol() == x - 1) zombiesHere.add(z);
            view.showTileStatus(cell, zombiesHere, x, y);
            return;
        }
        view.showError("Usage: show map  |  show plants status  |  show tile status -l (<x>, <y>)");
    }

    private void handleStartWaves(String command) {
        if (!command.equals("start zombie waves")) {
            view.showError("Usage: start zombie waves");
            return;
        }
        Game game = requireGame();
        if (game == null) return;
        if (!(game.getLevel() instanceof PlantWhatYouGet)) {
            view.showError("This command is only available in Plant What You Get levels.");
            return;
        }
        PlantWhatYouGet level = (PlantWhatYouGet) game.getLevel();
        if (level.isWavesStarted()) {
            view.showError("Zombie waves have already started.");
            return;
        }
        level.startWaves();
        System.out.println("Zombie waves started!");
    }

    private Game requireGame() {
        Game game = app.getGame();
        if (game == null) view.showError("No game in progress.");
        return game;
    }

    private Cell cellAt(Game game, int x, int y) {
        GameField field = game.getField();
        if (field == null || !field.inBounds(x - 1, y - 1)) {
            view.showError("Location out of bounds: (" + x + ", " + y + ").");
            return null;
        }
        return field.getCell(x - 1, y - 1);
    }
}
